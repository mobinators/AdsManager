package com.mobinators.ads.manager.ui.compose

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils


@Composable
fun RateUsDialog(context: Context, showDialog: MutableState<Boolean>) {
    val animateIn = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        animateIn.value = true
    }
    AnimatedVisibility(
        visible = animateIn.value && showDialog.value,
        enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(
            initialScale = .8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = slideOutVertically { it / 8 } + fadeOut() + scaleOut(targetScale = .95f)
    ) {
        Box(
            Modifier
                .shadow(8.dp, shape = RoundedCornerShape(16.dp))
                .width(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    MaterialTheme.colorScheme.surface,
                ),
            contentAlignment = Alignment.Center
        ) {
            showDialog.value.then {
                RateUsDialogContent(context = context) {
                    showDialog.value = false
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                showDialog.value = false
            }
        }
    }
}

@Composable
private fun RateUsDialogContent(
    context: Context,
    onDismiss: () -> Unit
) {
    var ratingOne: Float by rememberSaveable { mutableFloatStateOf(1.4f) }
    Dialog(
        onDismissRequest = { onDismiss() }, properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                Image(
                    painter = painterResource(id = R.drawable.rating),
                    contentDescription = "Rate US",
                    modifier = Modifier.size(85.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Enjoying the App?",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "We're working hard for a better experience.\nWe'd greatly appreciate it if you could rate us.",
                    style = TextStyle(
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light
                    ),
                    color = Color.Black,
                    modifier = Modifier.padding(5.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                RatingStarBar(
                    value = ratingOne,
                    stepSize = StepSize.HALF,
                    style = RatingBarStyle.Fill(),
                    onValueChange = {
                        ratingOne = it
                    },
                    onRatingChanged = {
                        Log.d("TAG", "onRatingChanged: $it")
                        if (it >= 1) {
                            AdsUtils.openPlayStore(context, context.packageName)
                            onDismiss()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "The best we can get is 5 stars )",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.padding(top = 5.dp)
                )
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = {
                        AdsUtils.openPlayStore(context, context.packageName)
                        onDismiss()
                    },
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(
                            ContextCompat.getColor(
                                LocalContext.current,
                                R.color.rateColor
                            )
                        )
                    ),
                ) {
                    Text(text = "Rate Us")
                }
            }
        }
    }
}