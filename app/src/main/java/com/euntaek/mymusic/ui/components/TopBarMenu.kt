package com.euntaek.mymusic.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


object TopBarMenuDefaults {
    val Height = 64.dp
}

@Composable
fun TopBarMenu(
    title: String,
    alpha: Float = 1f,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    @DrawableRes startIconResId: Int? = null,
    @DrawableRes endIconResId: Int? = null,
    onStartIconClick: () -> Unit = {},
    onEndIconClick: () -> Unit = {},
) {
    TopBarLayout(
        modifier = Modifier
            .height(TopBarMenuDefaults.Height)
            .padding(start = 16.dp)
            .fillMaxWidth()
            .alpha(alpha),
        startIcon = {
            if (startIconResId != null) {
                IconButton(onClick = onStartIconClick) {
                    Image(
                        painter = painterResource(id = startIconResId),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        },
        endIcon = {
            if (endIconResId != null) {
                IconButton(
                    iconRes = endIconResId,
                    onClick = onEndIconClick
                )
            }
        }
    )
}

@Composable
private fun TopBarLayout(
    modifier: Modifier = Modifier,
    startIcon: @Composable RowScope.() -> Unit,
    title: @Composable () -> Unit,
    endIcon: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        startIcon()
        Box(modifier = Modifier.weight(1f), content = { title() })
        endIcon()
    }
}