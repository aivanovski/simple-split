package com.github.ai.simplesplit.android.utils

import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty

class AtomicReferenceDelegate<T>(
    private val reference: AtomicReference<T>
) {

    operator fun getValue(
        ref: Any?,
        property: KProperty<*>
    ): T = reference.get()

    operator fun setValue(
        ref: Any?,
        property: KProperty<*>,
        value: T
    ) = reference.set(value)
}

fun <T> atomicReference(initialValue: T): AtomicReferenceDelegate<T> =
    AtomicReferenceDelegate(AtomicReference(initialValue))