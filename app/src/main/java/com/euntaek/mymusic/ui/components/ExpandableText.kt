package com.euntaek.mymusic.ui.components

import androidx.compose.foundation.layout.Column
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
import com.euntaek.mymusic.R


@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = 3,
    onExpandStateChange: ((isExpanded: Boolean) -> Unit)? = null
) {
    var isExpanded by rememberSaveable(key = "isExpanded") { mutableStateOf(false) }
    var expandable by remember { mutableStateOf(false) } // This needs to use "remember" for checking the overflow text when rotating the device.

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            overflow = TextOverflow.Ellipsis,
            maxLines = if (isExpanded && expandable) Int.MAX_VALUE else maxLines,
            onTextLayout = { textLayoutResult ->
                if (!expandable) {
                    //Checking whether the text has more than three lines, but before that, make sure the "maxLines" should be set by "collapsedMaxLine"
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