package io.zensoft.config

import org.mockito.Mockito

fun <T> any(clazz: Class<T>): T = Mockito.any<T>(clazz)

fun anyLong(): Long = Mockito.anyLong()

fun anyString(): String = Mockito.anyString()

fun anyObject(): Any = Mockito.anyObject()

fun <T> eq(any: T): T = Mockito.eq<T>(any)