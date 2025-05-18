package com.euntaek.mymusic.ui.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.euntaek.mymusic.R
import com.euntaek.uicomponent.button.iconbutton.IconButton


@Composable
fun HeaderMenu(
    modifier: Modifier,
    youTubeMusicLink: String? = null,
    spotifyLink: String? = null,
    instagramLink: String? = null,
    whatsappLink: String? = null,
    onAboutButtonClick: () -> Unit,
    onMailIconButtonClick: (() -> Unit)? = null,
    onSettingIconClick: () -> Unit,
    isSettingUpdateMarkEnabled: Boolean,
) {
    Row(modifier = modifier) {
        LinkButtons(
            modifier = Modifier.align(Alignment.CenterVertically),
            youTubeMusicLink = youTubeMusicLink,
            spotifyLink = spotifyLink,
            whatsappLink = whatsappLink,
            instagramLink = instagramLink,
            navigateToMailBox = onMailIconButtonClick,
            onSettingIconClick = onSettingIconClick,
            isSettingUpdateMarkEnabled = isSettingUpdateMarkEnabled,
        )
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onAboutButtonClick
        ) {
            Text(
                text = stringResource(id = R.string.about),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun LinkButtons(
    modifier: Modifier,
    youTubeMusicLink: String? = null,
    spotifyLink: String? = null,
    instagramLink: String? = null,
    whatsappLink: String? = null,
    navigateToMailBox: (() -> Unit)? = null,
    onSettingIconClick: () -> Unit,
    isSettingUpdateMarkEnabled: Boolean
) {
    val uriHandler = LocalUriHandler.current
    val openUrl: (String) -> Unit = { uri ->
        uriHandler.openUri(uri)
    }

    Row(modifier = modifier) {
        if (!youTubeMusicLink.isNullOrEmpty()) {
            IconButton(
                iconRes = R.drawable.ic_logo_youtube_music,
                onClick = { openUrl(youTubeMusicLink) }
            )
        }

        if (!spotifyLink.isNullOrEmpty()) {
            IconButton(
                iconRes = R.drawable.ic_logo_spotify,
                onClick = { openUrl(spotifyLink) }
            )
        }

        if (!instagramLink.isNullOrEmpty()) {
            IconButton(
                iconRes = R.drawable.ic_logo_instagram,
                onClick = { openUrl(instagramLink) }
            )
        }

        if (!whatsappLink.isNullOrEmpty()) {
            IconButton(
                iconRes = R.drawable.ic_logo_whatsapp,
                onClick = { openUrl(whatsappLink) }
            )
        }

        if (navigateToMailBox != null) {
            IconButton(
                iconRes = R.drawable.ic_mail,
                isUpdateMarkEnabled = true,
                onClick = navigateToMailBox
            )
        }

        IconButton(
            iconRes = R.drawable.ic_more,
            isUpdateMarkEnabled = isSettingUpdateMarkEnabled,
            onClick = onSettingIconClick
        )
    }
}