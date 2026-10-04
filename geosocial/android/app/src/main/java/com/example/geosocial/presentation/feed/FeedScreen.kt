package com.example.geosocial.presentation.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onPlaceClick: (Long) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Лента") },
                actions = {
                    IconButton(onClick = viewModel::load) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Обновить")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when {
                state.isLoading -> LoadingBox()
                state.error != null -> MessageBox(state.error!!)
                state.posts.isEmpty() -> MessageBox(
                    "Пока нет ни одного поста.\nОткройте карту и добавьте первое место!"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.posts, key = { it.id }) { post ->
                        PostCard(post = post, onPlaceClick = onPlaceClick)
                    }
                }
            }
        }
    }
}
