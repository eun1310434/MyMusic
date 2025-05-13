package com.euntaek.mymusic.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn


open class BaseViewModel : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    internal fun <T, K> StateFlow<T>.mapState(
        transform: (data: T) -> K
    ): StateFlow<K> {
        return mapLatest {
            transform(it)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, transform(value))
    }
}
