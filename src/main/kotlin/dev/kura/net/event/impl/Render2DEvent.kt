package dev.kura.net.event.impl

import dev.kura.net.event.Event
import net.minecraft.client.gui.DrawContext

class Render2DEvent(val context: DrawContext): Event()