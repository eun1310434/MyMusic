package com.euntaek.mymusic.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Contains the default values used by [ExpandableText].
 */
object ExpandableTextDefaults {
    const val MAX_LINES = 3
}

/**
 * This [ExpandableText] is a text view component for when the text is too long and affects the screen's layout.
 * It includes a function that can fold the text when it exceeds a certain number of lines defined by [maxLines].
 *
 * @param text The text to be displayed.
 * @param modifier The [Modifier] used to adjust the layout or draw decoration content.
 * @param color The [Color] to apply to the text. If [Color.Unspecified], and [style] has no color set,
 * it will use [LocalContentColor].
 * @param maxLines An optional maximum number of lines for the text to span, wrapping if necessary.
 * If the text exceeds this number of lines, it will be truncated.
 * @param style The style configuration for the text, such as color, font, line height, etc.
 * @param onExpandStateChange A callback function that will be called when the expand state changes.
 */
@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = ExpandableTextDefaults.MAX_LINES,
    style: TextStyle = LocalTextStyle.current,
    onExpandStateChange: ((isExpanded: Boolean) -> Unit)? = null
) {
    var isExpanded by rememberSaveable(key = "isExpanded") { mutableStateOf(false) }
    var expandable by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            overflow = TextOverflow.Ellipsis,
            maxLines = if (isExpanded && expandable) Int.MAX_VALUE else maxLines,
            onTextLayout = { textLayoutResult ->
                if (!expandable) {
                    expandable = textLayoutResult.hasVisualOverflow
                }
            }
        )

        if (expandable) {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                content = { Text(text = stringResource(if (isExpanded) R.string.show_less else R.string.show_more)) },
                onClick = {
                    isExpanded = !isExpanded
                    if (onExpandStateChange != null) {
                        onExpandStateChange(isExpanded)
                    }
                }
            )
        }
    }
}