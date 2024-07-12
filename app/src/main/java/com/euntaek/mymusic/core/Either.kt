package com.euntaek.mymusic.core

sealed class Either<out R> {
    data class Success<out T>(val data: T) : Either<T>()
    data class Error(val exception: Exception) : Either<Nothing>()
    //    object Loading : Either<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            //            Loading -> "Loading"
        }
    }
}


suspend fun <T> execUsesCase(
    load: suspend () -> Either<T>,
    success: (T) -> Unit,
    error: (Exception) -> Unit = {}
) {
    when (val result = load()) {
        is Either.Success -> success(result.data)
        is Either.Error -> error(result.exception)
    }
}
