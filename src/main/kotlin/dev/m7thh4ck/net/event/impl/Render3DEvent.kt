package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event
import net.minecraft.client.util.math.MatrixStack

class Render3DEvent(val matrices: MatrixStack, val tickDelta: Float): Event()