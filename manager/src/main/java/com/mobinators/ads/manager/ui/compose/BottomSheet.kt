package com.mobinators.ads.manager.ui.compose

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mobinators.ads.manager.R
import com.mobinators.ads.manager.applications.AdsApplication
import com.mobinators.ads.manager.extensions.then
import com.mobinators.ads.manager.ui.commons.models.PanelModel
import com.mobinators.ads.manager.ui.commons.utils.AdsUtils
import pak.developer.app.managers.extensions.logD


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    activity: Activity,
    isBottomSheetVisible: Boolean,
    sheetState: SheetState,
    panelModel: PanelModel? = null,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    var ratingOne: Float by rememberSaveable { mutableFloatStateOf(1.4f) }
    val isRateShow: Boolean = AdsApplication.getAdsModel()!!.isRateUsDialog
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RectangleShape,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = .5f),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            AdsApplication.getAdsModel()!!.isRateUsDialog
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(10.dp) // Outer padding
                    .clip(shape = RoundedCornerShape(18.dp))
                    .background(color = panelModel?.panelBackgroundColor?.let {
                        Color(
                            ContextCompat.getColor(
                                LocalContext.current, it
                            )
                        )
                    } ?: MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .padding(20.dp) // Inner padding
            ) {
                Text(
                    text = panelModel?.title ?: ContextCompat.getString(
                        LocalContext.current,
                        R.string.exit_app
                    ),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(
                        ContextCompat.getColor(
                            LocalContext.current,
                            panelModel?.titleColor ?: R.color.menuColor
                        )
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                isRateShow.not().then {
                    Box(
                        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = panelModel?.desc ?: ContextCompat.getString(
                                LocalContext.current,
                                R.string.sure_you_want_to_exit
                            ),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(
                                ContextCompat.getColor(
                                    LocalContext.current,
                                    panelModel?.descColor ?: R.color.menuColor
                                )
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                isRateShow.then {
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
                                    AdsUtils.openPlayStore(activity, activity.packageName)
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
                    }
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        ShowBannerAds(
                            modifier = Modifier.height(50.dp),
                            isPurchased = false,
                            listener = object : BannerAdsListener {
                                override fun onBannerAdsState(adsState: AdsState) {
                                    logD("Banner Ads State : ${adsState.name} ")
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(38.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(0.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                ContextCompat.getColor(
                                    activity,
                                    panelModel?.cancelBgColor ?: R.color.menuColor
                                )
                            )
                        ),
                        onClick = { onDismiss() },
                        content = {
                            Text(
                                text = ContextCompat.getString(
                                    activity,
                                    R.string.cancel
                                ),
                                color = Color(
                                    ContextCompat.getColor(
                                        activity,
                                        panelModel?.cancelButtonTitleColor ?: R.color.menuColor
                                    )
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.weight(0.1f))
                    Button(
                        modifier = Modifier.weight(0.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(
                                ContextCompat.getColor(
                                    activity,
                                    panelModel?.exitButtonBgColor ?: R.color.menuColor
                                )
                            )
                        ),
                        onClick = { onExit() },
                        content = {
                            Text(
                                text = ContextCompat.getString(
                                    activity,
                                    R.string.exit_
                                ),
                                color = Color(
                                    ContextCompat.getColor(
                                        activity,
                                        panelModel?.exitButtonTextColor ?: R.color.menuColor
                                    )
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}