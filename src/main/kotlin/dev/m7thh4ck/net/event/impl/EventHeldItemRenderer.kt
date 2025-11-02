package dev.m7thh4ck.net.event.impl

import dev.m7thh4ck.net.event.Event
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand

class EventHeldItemRenderer(val hand: Hand, val item: ItemStack, private val ep: Float, private val stack: MatrixStack) :

    Event(){

    fun getStack(): MatrixStack {

        return stack
    }

    }