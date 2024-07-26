package com.euntaek.mymusic.utility

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