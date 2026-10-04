package com.example.geosocial.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.geosocial.presentation.components.LoadingBox
import com.example.geosocial.presentation.components.MessageBox
import com.example.geosocial.presentation.components.PlaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onPlaceClick: (Long) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(Modifier.padding(padding))
            state.user == null -> MessageBox(state.error ?: "Не удалось загрузить профиль",
                Modifier.padding(padding))
            else -> {
                val user = state.user!!
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                if (state.isEditing) {
                                    OutlinedTextField(
                                        value = state.editName,
                                        onValueChange = viewModel::onNameChange,
                                        label = { Text("Имя") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = state.editBio,
                                        onValueChange = viewModel::onBioChange,
                                        label = { Text("О себе") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = viewModel::saveProfile) { Text("Сохранить") }
                                } else {
                                    Text(user.displayName, style = MaterialTheme.typography.headlineSmall)
                                    Text(user.email, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    user.bio?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(8.dp))
                                        Text(it, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = viewModel::startEditing) {
                                        Text("Редактировать")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text("Мои места (${state.places.size})",
                            style = MaterialTheme.typography.titleMedium)
                    }
                    items(state.places, key = { it.id }) { place ->
                        PlaceCard(place = place, onClick = { onPlaceClick(place.id) })
                    }
                }
            }
        }
    }
}
