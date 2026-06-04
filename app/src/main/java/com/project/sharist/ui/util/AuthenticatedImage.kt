package com.project.sharist.ui.util

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView

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

    val bytes by produceState<ByteArray?>(initialValue = null, key1 = path) {
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
