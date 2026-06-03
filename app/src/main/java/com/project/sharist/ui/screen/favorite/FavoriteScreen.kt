package com.project.sharist.ui.screen.favorite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.model.favorite.FavoriteLocationEntity
@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()

    LaunchedEffect(Unit) {
     //   viewModel.loadFavorites()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "My Favorites",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (favorites.isEmpty()) {
            Text("No favorites yet.")
            return
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { fav ->
                FavoriteItem(
                    favorite = fav,
                    onRemove = {
                        fav.id?.let {
                            //viewModel.removeFavorite(it)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    favorite: FavoriteLocationEntity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = favorite.name ?: "Unnamed",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Location ID: ${favorite.id}")

            favorite.latitude?.let { lat ->
                favorite.longitude?.let { lng ->
                    Text("Lat: $lat, Lng: $lng")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onRemove) {
                Text("Remove")
            }
        }
    }
}