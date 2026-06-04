package com.project.sharist.ui.util

import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

@Composable
fun AuthenticatedImage(
    path: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    loadBytes: suspend (String) -> ByteArray,
    placeholder: @Composable () -> Unit
) {
    if (path.isNullOrBlank()) {
        placeholder()
        return
    }

    if (path.startsWith("content://")) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                }
            },
            update = { imageView ->
                imageView.setImageURI(Uri.parse(path))
            }
        )
        return
    }

    val context = LocalContext.current
    val isOnWifi = remember { context.isOnWifi() }
    var mobileDataDownloadRequested by remember(path) { mutableStateOf(false) }
    val shouldLoad = isOnWifi || mobileDataDownloadRequested

    if (!shouldLoad) {
        Box(
            modifier = modifier.clickable {
                mobileDataDownloadRequested = true
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Tap to load",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    val bytes by produceState<ByteArray?>(initialValue = null, key1 = path, key2 = shouldLoad) {
        value = try {
            loadBytes(path)
        } catch (_: Exception) {
            null
        }
    }
    val bitmap = remember(bytes) {
        bytes
            ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            ?.asImageBitmap()
    }

    if (bitmap == null) {
        placeholder()
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

private fun android.content.Context.isOnWifi(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}
