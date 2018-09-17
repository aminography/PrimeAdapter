package com.aminography.primeadapter.tools

import java.lang.reflect.InvocationTargetException

internal object PrimeAdapterUtils {

    var viewTypeMap: MutableMap<Class<*>, Int> = mutableMapOf()
    var reverseViewTypeMap: MutableMap<Int, Class<*>> = mutableMapOf()

    fun instantiateViewTypeManager() {
        var clazz: Class<*>? = null
        try {
            clazz = Class.forName("com.aminography.primeadapter.ViewTypeManager")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        clazz?.apply {
            val className = clazz.name
            try {
                val classConstructor = clazz.getConstructor()
                try {
                    classConstructor.newInstance()
                } catch (e: IllegalAccessException) {
                    throw RuntimeException("Unable to invoke $classConstructor", e)
                } catch (e: InstantiationException) {
                    throw RuntimeException("Unable to invoke $classConstructor", e)
                } catch (e: InvocationTargetException) {
                    val cause = e.cause
                    if (cause is RuntimeException) throw cause
                    if (cause is Error) throw cause
                    throw RuntimeException("Unable to create instance.", cause)
                }
            } catch (e: ClassNotFoundException) {
                throw RuntimeException("Unable to find Class for $className", e)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Unable to find constructor for $className", e)
            }

            val map = clazz.getDeclaredField("map")
            map.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            viewTypeMap = map.get(null) as MutableMap<Class<*>, Int>

            val reverseMap = clazz.getDeclaredField("reverseMap")
            reverseMap.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            reverseViewTypeMap = reverseMap.get(null) as MutableMap<Int, Class<*>>
        }
    }
}
