package com.mobinators.ads.manager.ui.compose

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mobinators.ads.manager.ui.compose.extensions.addStar
import com.mobinators.ads.manager.ui.compose.utils.RatingBarUtils


private val StarRatingKey = SemanticsPropertyKey<Float>("StarRating")
private var SemanticsPropertyReceiver.starRating by StarRatingKey
private var starActiveColor: Color? = null
private var startInActiveColor: Color? = null
private var starActiveStrokeColor: Color? = null
private var startInActiveStrokeColor: Color? = null


sealed interface StepSize {
    data object ONE : StepSize
    data object HALF : StepSize
}

sealed class RatingBarStyle(open val activeColor: Color) {
    companion object {
        val Default = Stroke()
    }

    open class Fill(
        override val activeColor: Color = Color(0xFFFF9800),
        val inActiveColor: Color = Color(0xFF808080),
    ) : RatingBarStyle(activeColor)

    class Stroke(
        val width: Float = 1f,
        override val activeColor: Color = Color(0xFFFF9800),
        val strokeColor: Color = Color(0xFF808080)
    ) : RatingBarStyle(activeColor)
}

@Composable
fun RatingStarBar(
    value: Float,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 42.dp,
    spaceBetween: Dp = 6.dp,
    isIndicator: Boolean = false,
    stepSize: StepSize = StepSize.ONE,
    hideInactiveStars: Boolean = false,
    activeStartColor: Color? = null,
    inActiveStartColor: Color? = null,
    activeStartStrokeColor: Color? = null,
    inActiveStartStrokeColor: Color? = null,
    style: RatingBarStyle,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit
) {
    starActiveColor = activeStartColor
    startInActiveColor = inActiveStartColor
    starActiveStrokeColor = activeStartStrokeColor
    startInActiveStrokeColor = inActiveStartStrokeColor
    RatingBar(
        value = value,
        modifier = modifier,
        numOfStars = numOfStars,
        size = size,
        spaceBetween = spaceBetween,
        isIndicator = isIndicator,
        stepSize = stepSize,
        hideInactiveStars = hideInactiveStars,
        style = style,
        onValueChange = onValueChange,
        onRatingChanged = onRatingChanged
    )
}

@Composable
private fun RatingBar(
    value: Float,
    modifier: Modifier = Modifier,
    numOfStars: Int = 5,
    size: Dp = 32.dp,
    spaceBetween: Dp = 6.dp,
    isIndicator: Boolean = false,
    stepSize: StepSize = StepSize.ONE,
    hideInactiveStars: Boolean = false,
    style: RatingBarStyle = RatingBarStyle.Default,
    onValueChange: (Float) -> Unit,
    onRatingChanged: (Float) -> Unit
) {
    var rowSize by remember { mutableStateOf(Size.Zero) }
    var lastDraggedValue by remember { mutableFloatStateOf(0f) }
    val direction = LocalLayoutDirection.current
    val density = LocalDensity.current
    val paddingInPx = remember {
        with(density) { spaceBetween.toPx() }
    }
    val starSizeInPx = remember {
        with(density) { size.toPx() }
    }

    Row(modifier = modifier
        .onSizeChanged { rowSize = it.toSize() }
        .pointerInput(
            onValueChange
        ) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (isIndicator || hideInactiveStars)
                        return@detectHorizontalDragGestures
                    onRatingChanged(lastDraggedValue)
                },
                onDragCancel = {

                },
                onDragStart = {

                },
                onHorizontalDrag = { change, _ ->
                    if (isIndicator || hideInactiveStars)
                        return@detectHorizontalDragGestures
                    change.consume()
                    val dragX = change.position.x.coerceIn(-1f, rowSize.width)
                    var calculatedStars =
                        RatingBarUtils.calculateStars(
                            dragX,
                            paddingInPx,
                            numOfStars, stepSize, starSizeInPx
                        )
                    if (direction == LayoutDirection.Rtl) {
                        // calculatedStars -> reversed
                        // 1 -> 5, 2 -> 4, 3 -> 3, 4 -> 2,5 -> 1
                        calculatedStars = (numOfStars - calculatedStars)
                    }
                    onValueChange(calculatedStars)
                    lastDraggedValue = calculatedStars
                }
            )
        }
        .pointerInput(onValueChange) {
            //handling when click events
            detectTapGestures(onTap = {
                if (isIndicator || hideInactiveStars)
                    return@detectTapGestures
                val dragX = it.x.coerceIn(-1f, rowSize.width)
                var calculatedStars =
                    RatingBarUtils.calculateStars(
                        dragX,
                        paddingInPx,
                        numOfStars, stepSize, starSizeInPx
                    )
                if (direction == LayoutDirection.Rtl) {
                    // calculatedStars -> reversed
                    // 1 -> 5, 2 -> 4, 3 -> 3, 4 -> 2,5 -> 1
                    calculatedStars = (numOfStars - calculatedStars) + 1
                }
                onValueChange(calculatedStars)
                onRatingChanged(calculatedStars)
            })
        }) {
        ComposeStars(
            value,
            numOfStars,
            size,
            spaceBetween,
            hideInactiveStars,
            style = style
        )
    }
}


@Composable
private fun ComposeStars(
    value: Float,
    numOfStars: Int,
    size: Dp,
    spaceBetween: Dp,
    hideInactiveStars: Boolean,
    style: RatingBarStyle
) {

    val ratingPerStar = 1f
    var remainingRating = value

    Row(modifier = Modifier
        .semantics { starRating = value }) {
        for (i in 1..numOfStars) {
            val starRating = when {
                remainingRating == 0f -> {
                    0f
                }

                remainingRating >= ratingPerStar -> {
                    remainingRating -= ratingPerStar
                    1f
                }

                else -> {
                    val fraction = remainingRating / ratingPerStar
                    remainingRating = 0f
                    fraction
                }
            }
            if (hideInactiveStars && starRating == 0.0f)
                break

            RatingStarFillOrNot(
                fraction = starRating,
                style = style,
                modifier = Modifier
                    .padding(
                        start = if (i > 1) spaceBetween else 0.dp,
                        end = if (i < numOfStars) spaceBetween else 0.dp
                    )
                    .size(size = size)
                    .testTag("RatingStar"),

                )
        }
    }
}


@Composable
private fun RatingStarFillOrNot(
    @FloatRange(from = 0.0, to = 1.0) fraction: Float,
    modifier: Modifier = Modifier,
    style: RatingBarStyle
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(modifier = modifier) {
        FilledStar(
            fraction,
            style,
            isRtl
        )
        EmptyStar(fraction, style, isRtl)
    }
}


@Composable
private fun FilledStar(
    fraction: Float, style: RatingBarStyle, isRtl: Boolean
) = Canvas(
    modifier = Modifier
        .fillMaxSize()
        .clip(
            if (isRtl) rtlFilledStarFractionalShape(fraction = fraction)
            else FractionalRectangleShape(0f, fraction)
        )
) {
    val path = Path().addStar(size)

    drawPath(path, color = starActiveColor ?: style.activeColor, style = Fill) // Filled Star
    drawPath(
        path,
        color = starActiveColor ?: style.activeColor,
        style = Stroke(width = if (style is RatingBarStyle.Stroke) style.width else 1f)
    ) // Border

}

@Composable
private fun EmptyStar(
    fraction: Float,
    style: RatingBarStyle,
    isRtl: Boolean
) =
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(
                if (isRtl) rtlEmptyStarFractionalShape(fraction = fraction)
                else FractionalRectangleShape(fraction, 1f)
            )
    ) {
        val path = Path().addStar(size)
        if (style is RatingBarStyle.Fill) drawPath(
            path,
            color = startInActiveColor ?: style.inActiveColor,
            style = Fill
        ) // Border
        else if (style is RatingBarStyle.Stroke) drawPath(
            path,
            color = startInActiveStrokeColor ?: style.strokeColor,
            style = Stroke(width = style.width)
        ) // Border
    }

private fun rtlEmptyStarFractionalShape(fraction: Float): FractionalRectangleShape {
    return if (fraction == 1f || fraction == 0f)
        FractionalRectangleShape(fraction, 1f)
    else FractionalRectangleShape(0f, 1f - fraction)
}

private fun rtlFilledStarFractionalShape(fraction: Float): FractionalRectangleShape {
    return if (fraction == 0f || fraction == 1f)
        FractionalRectangleShape(0f, fraction)
    else FractionalRectangleShape(1f - fraction, 1f)
}