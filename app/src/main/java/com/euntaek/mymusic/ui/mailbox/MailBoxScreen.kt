package com.euntaek.mymusic.ui.mailbox

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.euntaek.mymusic.viewmodels.MainViewModel


@Composable
fun MailBoxScreen(viewModel: MainViewModel, backDispatcher: OnBackPressedDispatcher) {
    Column(modifier = Modifier.fillMaxSize()) {
        // TODO This will be used for showing the Mail list.
    }
}