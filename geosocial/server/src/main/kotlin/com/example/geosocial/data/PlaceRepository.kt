package com.example.geosocial.data

import com.example.geosocial.db.DatabaseFactory.dbQuery
import com.example.geosocial.db.Places
import com.example.geosocial.db.Posts
import com.example.geosocial.db.Users
import com.example.geosocial.domain.AppException
import com.example.geosocial.domain.GeoUtils
import com.example.geosocial.dto.CreatePlaceRequest
import com.example.geosocial.dto.PlaceDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.deleteWhere
import java.time.LocalDateTime

class PlaceRepository {

    private val joined = Places.innerJoin(Users, { Places.authorId }, { Users.id })

    /**
     * Поиск мест. Если переданы координаты — возвращаются места в радиусе
     * (сначала грубый отбор по прямоугольнику в SQL, затем точное расстояние).
     */
    suspend fun search(
        lat: Double?,
        lon: Double?,
        radiusKm: Double,
        query: String?,
        category: String?,
        limit: Int,
        offset: Int
    ): List<PlaceDto> = dbQuery {
        var condition: Op<Boolean> = Op.TRUE

        if (lat != null && lon != null) {
            val box = GeoUtils.boundingBox(lat, lon, radiusKm)
            condition = condition and
                    (Places.latitude greaterEq box.minLat) and
                    (Places.latitude lessEq box.maxLat) and
                    (Places.longitude greaterEq box.minLon) and
                    (Places.longitude lessEq box.maxLon)
        }
        if (!query.isNullOrBlank()) {
            condition = condition and (
                    (Places.title.lowerCase() like "%${query.lowercase()}%") or
                            (Places.description.lowerCase() like "%${query.lowercase()}%")
                    )
        }
        if (!category.isNullOrBlank()) {
            condition = condition and (Places.category eq category)
        }

        val rows = joined.selectAll().where { condition }
            .orderBy(Places.createdAt to SortOrder.DESC)
            .limit(if (lat != null) 500 else limit)
            .offset(offset.toLong())
            .toList()

        val counts = postsCountFor(rows.map { it[Places.id].value })
        var result = rows.map { toDto(it, counts, lat, lon) }

        if (lat != null && lon != null) {
            result = result
                .filter { (it.distanceKm ?: Double.MAX_VALUE) <= radiusKm }
                .sortedBy { it.distanceKm }
                .take(limit)
        }
        result
    }

    suspend fun findById(id: Long, lat: Double? = null, lon: Double? = null): PlaceDto = dbQuery {
        val row = joined.selectAll().where { Places.id eq id }.singleOrNull()
            ?: throw AppException.NotFound("Место")
        toDto(row, postsCountFor(listOf(id)), lat, lon)
    }

    suspend fun findByAuthor(authorId: Long): List<PlaceDto> = dbQuery {
        val rows = joined.selectAll().where { Places.authorId eq authorId }
            .orderBy(Places.createdAt to SortOrder.DESC)
            .toList()
        val counts = postsCountFor(rows.map { it[Places.id].value })
        rows.map { toDto(it, counts, null, null) }
    }

    suspend fun create(authorId: Long, request: CreatePlaceRequest): PlaceDto {
        validate(request)
        val newId = dbQuery {
            Places.insertAndGetId {
                it[title] = request.title.trim()
                it[description] = request.description.trim()
                it[category] = request.category.trim()
                it[latitude] = request.latitude
                it[longitude] = request.longitude
                it[address] = request.address?.trim()
                it[Places.authorId] = authorId
                it[createdAt] = LocalDateTime.now()
            }.value
        }
        return findById(newId)
    }

    suspend fun delete(id: Long, requesterId: Long) = dbQuery {
        val row = Places.selectAll().where { Places.id eq id }.singleOrNull()
            ?: throw AppException.NotFound("Место")
        if (row[Places.authorId].value != requesterId) {
            throw AppException.Forbidden("Удалять место может только его автор")
        }
        Places.deleteWhere { Places.id eq id }
    }

    private fun validate(request: CreatePlaceRequest) {
        if (request.title.isBlank()) throw AppException.BadRequest("Название не может быть пустым")
        if (request.latitude !in -90.0..90.0) throw AppException.BadRequest("Некорректная широта")
        if (request.longitude !in -180.0..180.0) throw AppException.BadRequest("Некорректная долгота")
    }

    private fun postsCountFor(placeIds: List<Long>): Map<Long, Long> {
        if (placeIds.isEmpty()) return emptyMap()
        val countCol = Posts.id.count()
        return Posts
            .select(Posts.placeId, countCol)
            .where { Posts.placeId inList placeIds }
            .groupBy(Posts.placeId)
            .associate { it[Posts.placeId].value to it[countCol] }
    }

    companion object {
        fun toDto(
            row: ResultRow,
            counts: Map<Long, Long> = emptyMap(),
            lat: Double? = null,
            lon: Double? = null
        ): PlaceDto {
            val id = row[Places.id].value
            val placeLat = row[Places.latitude]
            val placeLon = row[Places.longitude]
            return PlaceDto(
                id = id,
                title = row[Places.title],
                description = row[Places.description],
                category = row[Places.category],
                latitude = placeLat,
                longitude = placeLon,
                address = row[Places.address],
                author = UserRepository.toShortDto(row),
                postsCount = counts[id] ?: 0,
                distanceKm = if (lat != null && lon != null)
                    GeoUtils.distanceKm(lat, lon, placeLat, placeLon) else null,
                createdAt = row[Places.createdAt].toString()
            )
        }
    }
}
