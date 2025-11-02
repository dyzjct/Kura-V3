package dev.kura.net.utils.graphics

import com.mojang.blaze3d.systems.RenderSystem
import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.Render2DEvent
import dev.kura.net.utils.Util
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import java.awt.Color

object Render2DEngine: Util() {

    init {
        eventListener<Render2DEvent>(-114514, true) {
        }
    }


    fun rainbow(speed: Int, index: Int, saturation: Float, brightness: Float, opacity: Float): Color {
        val angle = ((System.currentTimeMillis() / speed + index) % 360).toInt()
        val hue = angle / 360f
        val color = Color(Color.HSBtoRGB(hue, saturation, brightness))
        return Color(
            color.red, color.green, color.blue,
            0.coerceAtLeast(255.coerceAtMost((opacity * 255).toInt()))
        )
    }
    fun injectAlpha(color: Color, alpha: Int): Color {
        return Color(color.red, color.green, color.blue, MathHelper.clamp(alpha, 0, 255))
    }
    fun setupRender() {
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    fun endRender() {
        RenderSystem.disableBlend()
    }
    fun draw2DGradientRect(
        matrices: MatrixStack,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        leftBottomColor: Color,
        leftTopColor: Color,
        rightBottomColor: Color,
        rightTopColor: Color
    ) {
        val matrix = matrices.peek().positionMatrix
        val bufferBuilder = Tessellator.getInstance().buffer
        setupRender()
        RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        bufferBuilder.vertex(matrix, right, top, 0.0f).color(rightTopColor.rgb).next()
        bufferBuilder.vertex(matrix, left, top, 0.0f).color(leftTopColor.rgb).next()
        bufferBuilder.vertex(matrix, left, bottom, 0.0f).color(leftBottomColor.rgb).next()
        bufferBuilder.vertex(matrix, right, bottom, 0.0f).color(rightBottomColor.rgb).next()
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end())
        endRender()
    }

    fun drawRectOutline(matrices: MatrixStack, x: Float, y: Float, width: Float, height: Float, c: Color) {
        matrices.push()

        val matrix4 = matrices.peek().positionMatrix

        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer

        RenderSystem.lineWidth(2f)

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR)

        bufferBuilder.vertex(matrix4, x, y, 3f)
            .color(c.red, c.green, c.blue, 255).next()
        bufferBuilder.vertex(matrix4, x + width, y, 3f)
            .color(c.red, c.green, c.blue, 255).next()

        bufferBuilder.vertex(matrix4, x, y, 3f)
            .color(c.red, c.green, c.blue, 255).next()
        bufferBuilder.vertex(matrix4, x, y + height, 3f)
            .color(c.red, c.green, c.blue, 255).next()

        bufferBuilder.vertex(matrix4, x + width, y + height, 3f)
            .color(c.red, c.green, c.blue, 255).next()
        bufferBuilder.vertex(matrix4, x + width, y + height, 3f)
            .color(c.red, c.green, c.blue, 255).next()

        bufferBuilder.vertex(matrix4, x + width, y + height, 3f)
            .color(c.red, c.green, c.blue, 255).next()
        bufferBuilder.vertex(matrix4, x + width, y, 3f)
            .color(c.red, c.green, c.blue, 255).next()

        RenderSystem.disableDepthTest()
        RenderSystem.disableBlend()

        tessellator.draw()

        matrices.pop()
    }

    fun drawRect(matrices: MatrixStack, x: Float, y: Float, width: Float, height: Float, c: Color) {
        val color = c.rgb
        val matrix = matrices.peek().positionMatrix
        val f = (color shr 24 and 255).toFloat() / 255.0f
        val g = (color shr 16 and 255).toFloat() / 255.0f
        val h = (color shr 8 and 255).toFloat() / 255.0f
        val k = (color and 255).toFloat() / 255.0f
        val bufferBuilder = Tessellator.getInstance().buffer
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.setShader { GameRenderer.getPositionColorProgram() }
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        bufferBuilder.vertex(matrix, x, y + height, 0.0f).color(g, h, k, f).next()
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0f).color(g, h, k, f).next()
        bufferBuilder.vertex(matrix, x + width, y, 0.0f).color(g, h, k, f).next()
        bufferBuilder.vertex(matrix, x, y, 0.0f).color(g, h, k, f).next()
        Tessellator.getInstance().draw()
        RenderSystem.disableBlend()
    }

}