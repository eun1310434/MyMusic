package com.euntaek.mymusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import com.euntaek.mymusic.utility.toPx
import kotlinx.coroutines.launch

@Composable
fun TopDrawerColumn(
    modifier: Modifier = Modifier,
    topBar: @Composable BoxScope.(scale: Float) -> Unit,
    drawer: @Composable BoxScope.(scale: Float) -> Unit,
    content: @Composable (scale: Float, lazyListState: LazyListState) -> Unit
) {
    val topBarHeight = TopBarMenuDefaults.Height.toPx()
    val scope = rememberCoroutineScope()
    var scrollScale by remember { mutableFloatStateOf(1f) }
    val listState = rememberLazyListState()
    val drawerScrollState = rememberScrollState()
    var drawerHeight by remember { mutableIntStateOf(0) }

    val columnScrollableState = rememberScrollableState { delta ->
        scope.launch {
            if (delta > 0) { // Column Scroll down
                if (listState.canScrollBackward) {
                    listState.dispatchRawDelta(-delta)
                } else {
                    drawerScrollState.dispatchRawDelta(-delta)
                }
            } else { // Column Scroll up
                if (drawerHeight - drawerScrollState.value - topBarHeight > 0) {
                    drawerScrollState.dispatchRawDelta(-delta)
                } else {
                    listState.dispatchRawDelta(-delta)
                }
            }
        }
        delta
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .scrollable(
                state = columnScrollableState,
                orientation = Orientation.Vertical,
                flingBehavior = ScrollableDefaults.flingBehavior()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer)) {
            topBar(scrollScale)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val offsetValue =
                            IntOffset.Zero.copy(y = -drawerScrollState.value)

                        drawerHeight = placeable.height

                        layout(
                            width = (placeable.width + offsetValue.x).coerceAtLeast(0),
                            height = (placeable.height + offsetValue.y).coerceAtLeast(0),
                            placementBlock = {
                                placeable.placeRelativeWithLayer(
                                    position = offsetValue,
                                    zIndex = -1f,
                                    layerBlock = {
                                        val scale =
                                            1 + (offsetValue.y.toFloat() / (placeable.height.toFloat() - topBarHeight))

                                        scrollScale = scale.coerceIn(0.001f, 1f)

                                        this.alpha = scale
                                    }
                                )
                            }
                        )
                    },
                content = { drawer(scrollScale) }
            )
        }
        content(scrollScale, listState)
    }
}

