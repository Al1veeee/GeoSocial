package com.example.geosocial.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

private val MOSCOW = LatLng(55.7558, 37.6173)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onPlaceClick: (Long) -> Unit,
    onAddPlace: (Double, Double) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasLocationPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(MOSCOW, 11f)
    }

    // Определяем текущее местоположение и подгружаем места рядом
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            runCatching {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
            }.getOrNull()?.let { location ->
                viewModel.onUserLocation(location.latitude, location.longitude)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(location.latitude, location.longitude), 13f
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Места на карте") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val target = cameraPositionState.position.target
                    onAddPlace(target.latitude, target.longitude)
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Добавить место") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Поиск по названию") },
                singleLine = true,
                trailingIcon = {
                    TextButton(onClick = viewModel::load) { Text("Найти") }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(Modifier.weight(1f)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapLongClick = { latLng -> onAddPlace(latLng.latitude, latLng.longitude) }
                ) {
                    state.places.forEach { place ->
                        Marker(
                            state = rememberMarkerState(
                                key = place.id.toString(),
                                position = LatLng(place.latitude, place.longitude)
                            ),
                            title = place.title,
                            snippet = "${place.category} · постов: ${place.postsCount}",
                            onInfoWindowClick = { onPlaceClick(place.id) }
                        )
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    ) {
                        Text(it, Modifier.padding(12.dp))
                    }
                }
            }

            Text(
                "Долгое нажатие на карте — добавить место в этой точке",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}
