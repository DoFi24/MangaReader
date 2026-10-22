package com.nilemnod.mangareader.core.common.util

fun <T> Set<T>.mutate(action: (MutableSet<T>) -> Unit): Set<T> {
    return toMutableSet().apply(action)
}
