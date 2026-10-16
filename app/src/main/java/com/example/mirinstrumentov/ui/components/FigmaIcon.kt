package com.example.mirinstrumentov.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp


@Composable
fun FigmaIcon(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    fallback: String = ""
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val drawable = remember(context, name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    if (drawable != 0) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            if (fallback.isNotEmpty()) {
                Text(
                    text = fallback,
                    color = if (tint == Color.Unspecified) Color.Gray else tint,
                    fontSize = 19.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
