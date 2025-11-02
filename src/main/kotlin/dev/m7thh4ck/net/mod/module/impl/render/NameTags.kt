package dev.m7thh4ck.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.Render2DEvent
import dev.kura.net.event.impl.RenderLivingLabelEvent
import dev.kura.net.manager.impl.FriendManager
import dev.m7thh4ck.net.mod.module.Category
import dev.kura.net.mod.module.Module
import dev.m7thh4ck.net.util.graphics.Render2DEngine
import dev.m7thh4ck.net.util.graphics.Render3DEngine
import dev.m7thh4ck.net.util.graphics.TextUtil
import dev.m7thh4ck.net.util.graphics.color.ColorRGB
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Formatting
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.max

object NameTags : Module("NameTags", Category.Render) {

    private val range by setting("Range", 256, 0..512, 16)
    private val shadow by setting("Shadow", false)
    private val rect by setting("Rect", true)


    init {

        eventListener<Render2DEvent> {
            if (fullNullCheck()) return@eventListener

            for (entity in world.entities) {
                if (entity == null) continue
                if (entity == player) continue
                if (entity !is PlayerEntity) continue
                if (mc.cameraEntity?.distanceTo(entity)!! > range) continue


                val text =
                    (if (FriendManager.isFriend(entity)) "${Formatting.GREEN}" else "${Formatting.WHITE}") +
                            ("${entity.name.string} ${Formatting.GREEN}Health ${Formatting.WHITE}${entity.health}")

                drawText(it.context, text, entity.pos.add(Vec3d(0.0, entity.height + 0.5, 0.0)))
            }
        }

        eventListener<RenderLivingLabelEvent> {
            if (it.entity is PlayerEntity) it.cancel()
        }

    }

    private fun drawText(context: DrawContext, text: String, vec: Vec3d) {
        val vector = Render3DEngine.worldSpaceToScreenSpace(Vec3d(vec.x, vec.y, vec.z))

        if (vector.z > 0 && vector.z < 1) {
            val posX = vector.x
            val posY = vector.y
            val endPosX = max(vector.x, vector.z)

            val scale = 1.0f
            val diff = ((endPosX - posX) / 2).toFloat()
            val textWidth = mc.textRenderer.getWidth(text) * scale
            val tagX = (posX + diff - textWidth / 2).toFloat()
            context.matrices.push()
            context.matrices.scale(scale, scale, scale)

            val y = ((posY - 11 + mc.textRenderer.fontHeight * 1.2) / scale).toFloat()
            if (rect) Render2DEngine.drawRect(
                context.matrices, ((tagX / scale) - 4),
                y - 4, mc.textRenderer.getWidth(text) + 8.0f,
                mc.textRenderer.fontHeight + 7.0f, Color(0, 0, 0, 140)
            )

            TextUtil.drawString(context, text, (tagX / scale), y, ColorRGB(255, 255, 255), shadow)

            context.matrices.pop()

        }

    }

}