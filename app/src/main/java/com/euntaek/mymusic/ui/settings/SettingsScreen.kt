package com.euntaek.mymusic.ui.settings

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.euntaek.mymusic.BuildConfig
import com.euntaek.mymusic.R
import com.euntaek.mymusic.data.entities.AppInfo
import com.euntaek.mymusic.viewmodels.MainViewModel
import com.euntaek.uicomponent.icon.Icon
import com.euntaek.uicomponent.progressindicator.ProgressIndicatorScreen
import com.euntaek.uicomponent.topbar.TopBarMenu


@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val appId = stringResource(id = R.string.app_id)
    LaunchedEffect(Unit) { viewModel.getAppInfo(appId = appId) }
    val appInfo by viewModel.appInfo.collectAsStateWithLifecycle()

    if (appInfo != null) {
        SettingsPage(appInfo = appInfo!!)
    } else {
        ProgressIndicatorScreen()
    }
}

@Composable
private fun SettingsPage(appInfo: AppInfo) {

    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        TopBarMenu(
            title = stringResource(id = R.string.settings),
            contentColor = MaterialTheme.colorScheme.onBackground,
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                SettingItem(
                    title = stringResource(R.string.account),
                    subTitle = stringResource(R.string.no_account),
                    iconRes = R.drawable.ic_user,
                    enabled = false
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.language),
                    subTitle = stringResource(R.string.default_system_language),
                    iconRes = R.drawable.ic_language,
                    enabled = false
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.share),
                    iconRes = R.drawable.ic_share,
                    onClick = {
                        context.startActivity(getAppShareIntent())
                    }
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.tickets),
                    iconRes = R.drawable.ic_ticket,
                    enabled = false
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.news),
                    iconRes = R.drawable.ic_announcement,
                    enabled = false
                )
            }
            item {
                SettingItem(
                    title = stringResource(R.string.app_version),
                    subTitle = BuildConfig.VERSION_NAME,
                    iconRes = R.drawable.ic_information,
                    isUpdateMarkEnabled = appInfo.version != BuildConfig.VERSION_NAME,
                    onClick = {
                        uriHandler.openUri("market://details?id=${BuildConfig.APPLICATION_ID}")
                    }
                )
            }
            //val drawable = context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID)
        }

    }
}

private fun getAppShareIntent(): Intent {
    return Intent.createChooser(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        type = "text/plain"
    }, null)
}

@Composable
private fun SettingItem(
    title: String,
    subTitle: String? = null,
    @DrawableRes iconRes: Int,
    isUpdateMarkEnabled: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .height(65.dp)
            .fillMaxSize()
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .alpha(if (enabled) 1f else 0.25f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                iconRes = iconRes,
                markAlign = Alignment.TopStart,
                isUpdateMarkEnabled = isUpdateMarkEnabled
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (!subTitle.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
    }
}
