package com.example.attendify.ui.theme.admin


import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SelfieThumbnail(path: String, size: androidx.compose.ui.unit.Dp = 56.dp) {
    var bitmap by remember(path) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(path) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        }
    }

    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Attendance selfie",
                contentScale = ContentScale.Crop
            )
        }
    }
}