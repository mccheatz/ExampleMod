package me.yuugiri.mod.util

import org.apache.logging.log4j.core.config.plugins.ResolverUtil
import java.lang.reflect.Modifier

/**
 * scan classes with specified superclass like what Reflections do but with log4j [ResolverUtil]
 * @author liulihaocai
 */
fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {
    // use resolver in log4j to scan classes in target package
    val resolver = ResolverUtil()

    // set class loader
    resolver.classLoader = klass.classLoader

    // set package to scan
    resolver.findInPackage(object : ResolverUtil.ClassTest() {
        override fun matches(type: Class<*>): Boolean {
            return true
        }
    }, packagePath)

    // use a list to cache classes
    val list = mutableListOf<Class<out T>>()

    for(resolved in resolver.classes) {
        // check if class is assignable from target class
        if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
            // add to list
            list.add(resolved as Class<out T>)
        }
    }

    return list
}

fun <T : Any> resolveInstances(packagePath: String, klass: Class<T>): List<T> {
    return resolvePackage(packagePath, klass).map {
        try {
            it.newInstance()
        } catch (e: IllegalAccessException) {
            getObjectInstance(it)
        }
    }
}

fun <T> getObjectInstance(clazz: Class<T>): T {
    clazz.declaredFields.forEach {
        if (it.name == "INSTANCE") {
            return it.get(null) as T
        }
    }
    throw IllegalAccessException("This class not a kotlin object")
}