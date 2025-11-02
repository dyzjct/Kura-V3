package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.client.util.math.MatrixStack

class Render3DEvent(val matrices: MatrixStack, val tickDelta: Float): Event()