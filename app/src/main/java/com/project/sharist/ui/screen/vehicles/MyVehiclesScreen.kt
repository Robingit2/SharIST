package com.project.sharist.ui.screen.vehicles

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.model.user.Vehicle

@Composable
fun MyVehiclesScreen(
    viewModel: MyVehiclesViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val errorMessage = uiState.errorMessage

    LaunchedEffect(Unit) {
        viewModel.loadVehicles()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Text("My vehicles", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddDialog = true },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add vehicle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.vehicles.isEmpty() && !uiState.isLoading) {
                Text("No vehicles added.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.vehicles) { vehicle ->
                        VehicleItem(
                            vehicle = vehicle,
                            photoBytes = uiState.vehiclePhotoBytes[vehicle.id],
                            onDeleteClick = {
                                viewModel.deleteVehicle(vehicle.id)
                            }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onAddClick = { plate, photoPath ->
                viewModel.addVehicle(context = context, plate = plate, photoPath = photoPath)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun VehicleItem(
    vehicle: Vehicle,
    photoBytes: ByteArray?,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VehiclePhoto(
                photoPath = vehicle.photoPath,
                photoBytes = photoBytes,
                modifier = Modifier.size(72.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.plate,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(onClick = onDeleteClick) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun VehiclePhoto(
    photoPath: String?,
    photoBytes: ByteArray?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(photoBytes) {
        photoBytes
            ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            ?.asImageBitmap()
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Vehicle photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    if (photoPath.isNullOrBlank()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No photo", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            imageView.setImageURI(Uri.parse(photoPath))
        }
    )
}

@Composable
private fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onAddClick: (plate: String, photoPath: String?) -> Unit
) {
    val context = LocalContext.current
    var plate by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            photoPath = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add vehicle") },
        text = {
            Column {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Plate") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { photoPicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (photoPath.isBlank()) "Choose photo" else "Change photo")
                }

                if (photoPath.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Photo selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddClick(plate, photoPath.takeIf { it.isNotBlank() })
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
