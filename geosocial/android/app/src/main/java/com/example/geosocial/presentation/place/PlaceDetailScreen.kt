package com.example.geosocial.presentation.place

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geosocial.presentation.components.LoadingBox
import com.example.geosocial.presentation.components.MessageBox
import com.example.geosocial.presentation.components.PostCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaceDetailScreen(
    onBack: () -> Unit,
    viewModel: PlaceDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.place?.title ?: "Место") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(Modifier.padding(padding))
            state.place == null -> MessageBox(state.error ?: "Место не найдено", Modifier.padding(padding))
            else -> {
                val place = state.place!!
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(place.title, style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(place.category, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                Text(place.description, style = MaterialTheme.typography.bodyMedium)
                                place.address?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Координаты: ${"%.5f".format(place.latitude)}, ${"%.5f".format(place.longitude)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Добавил: ${place.authorName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Оставить отзыв", style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.newPostText,
                                    onValueChange = viewModel::onPostTextChange,
                                    label = { Text("Что здесь интересного?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text("Оценка:", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.width(8.dp))
                                    (1..5).forEach { value ->
                                        FilterChip(
                                            selected = state.newPostRating == value,
                                            onClick = {
                                                viewModel.onRatingChange(
                                                    if (state.newPostRating == value) null else value
                                                )
                                            },
                                            label = { Text("$value") },
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = viewModel::sendPost,
                                    enabled = !state.isSending && state.newPostText.isNotBlank()
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = null,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Опубликовать")
                                }
                                state.error?.let {
                                    Spacer(Modifier.height(8.dp))
                                    Text(it, color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            "Отзывы (${state.posts.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(state.posts, key = { it.id }) { post ->
                        PostCard(post = post, onPlaceClick = {})
                    }
                }
            }
        }
    }
}
