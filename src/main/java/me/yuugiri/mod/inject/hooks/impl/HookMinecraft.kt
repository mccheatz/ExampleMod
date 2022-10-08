package me.yuugiri.mod.inject.hooks.impl

import me.yuugiri.mod.inject.hooks.AbstractHookProvider
import me.yuugiri.mod.inject.hooks.EnumHookType
import me.yuugiri.mod.inject.hooks.Hook
import org.lwjgl.opengl.Display

class HookMinecraft : AbstractHookProvider("net.minecraft.client.Minecraft") {

    @Hook(method = "createDisplay", type = EnumHookType.EXIT)
    fun createDisplay() {
        Display.setTitle("ExampleMod :)")
    }
}