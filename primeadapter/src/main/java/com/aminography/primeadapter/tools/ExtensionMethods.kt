package com.aminography.primeadapter.tools

/**
 * Created by aminography on 6/6/2018.
 */

inline fun Any.consume(f: () -> Unit): Boolean {
    f()
    return true
}