package com.example.geosocial.presentation.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed"
    const val MAP = "map"
    const val PROFILE = "profile"
    const val CREATE_PLACE = "create_place?lat={lat}&lng={lng}"
    const val PLACE_DETAIL = "place/{placeId}"

    fun createPlace(lat: Double, lng: Double) = "create_place?lat=$lat&lng=$lng"
    fun placeDetail(placeId: Long) = "place/$placeId"
}
