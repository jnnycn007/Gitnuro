package com.jetpackduba.gitnuro.extensions

inline fun <T> T.nullIf(predicate: (T) -> Boolean): T? {
    return if (predicate(this)) {
        null
    } else {
        this
    }
}