package me.yuugiri.mod.inject.hooks

import me.yuugiri.hutil.processor.hook.*
import me.yuugiri.hutil.processor.hook.point.HookPointEnter
import me.yuugiri.hutil.processor.hook.point.HookPointExit
import me.yuugiri.hutil.processor.hook.point.HookPointThrow
import me.yuugiri.hutil.processor.hook.point.IHookPoint
import java.lang.reflect.Method
import java.lang.reflect.Modifier


annotation class Hook(val method: String, val desc: String = "*", val type: EnumHookType, val shift: EnumHookShift = EnumHookShift.BEFORE, val ordinal: Int = -1)

enum class EnumHookType {
    ENTER,
    EXIT,
    THROW;

    fun toHookPoint(): IHookPoint {
        return when(this) {
            ENTER -> HookPointEnter()
            EXIT -> HookPointExit()
            THROW -> HookPointThrow()
        }
    }
}

abstract class AbstractHookProvider(val className: String) {

    open fun applyHook(mhp: MethodHookProcessor) {
        this.javaClass.declaredMethods.forEach { method ->
            if (!method.isAnnotationPresent(Hook::class.java)) return@forEach
            val annotation = method.getDeclaredAnnotation(Hook::class.java)!!
            mhp.addHookInfo(HookInfo(HookTargetImpl(className.replace('.', '/'), annotation.method, annotation.desc),
                annotation.type.toHookPoint(), annotation.shift, annotation.ordinal, genFunctionCallback(method)))
        }
    }

    private fun genFunctionCallback(method: Method): (MethodHookParam) -> Unit {
        method.isAccessible = true
        val isStatic = Modifier.isStatic(method.modifiers)
        return if (method.parameterCount == 0) {
            if (isStatic) ({ method.invoke(null) }) else ({ method.invoke(this) })
        } else if (method.parameterCount == 1) {
            if (isStatic) ({ method.invoke(null, it) }) else ({ method.invoke(this, it) })
        } else throw IllegalArgumentException("Unsupported parameter count: $method")
    }
}