package dev.m7thh4ck.net.mod.module.impl.client

import com.mojang.blaze3d.systems.RenderSystem
import dev.m7thh4ck.net.event.eventListener
import dev.m7thh4ck.net.event.impl.Render2DEvent
import dev.m7thh4ck.net.mod.gui.screen.ClickGUIScreen
import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.client.HUD.height
import dev.m7thh4ck.net.mod.module.impl.client.HUD.width
import dev.m7thh4ck.net.mod.module.impl.render.SytRender
import dev.m7thh4ck.net.util.graphics.Render2DEngine
import dev.m7thh4ck.net.util.helper.ChatUtil
import net.minecraft.util.Identifier
import java.awt.Color

object ClickGUI : Module("ClickGUI", Category.Client) {
    private val mode = setting("Mode", Mode.Mahiro)
    private val waterX by setting("X", 280, 0..500, 1)
    private val waterY by setting("Y", 290, 0..500, 1)
    private val waterMarkX by setting("WaterMarkX", 630, 0..1000, 1)
    private val waterMarkY by setting("WaterMarkY", 215, 0..500, 1)
    val QaQ by setting("BackShadow", true)
    val Red by setting("BackRed", 255, 0..255, 1) { QaQ }
    val Green by setting("BackGreen", 73, 0..255, 1) { QaQ }
    val Blue by setting("BackBlue", 255, 0..255, 1) { QaQ }
    val A by setting("BackAlpha", 255, 0..255, 1) { QaQ }
    val shadow by setting("Shadow", true)
    val background by setting("Background", true)
    val backRed by setting("BackgroundRed", 0, 0..255, 1) { background }
    val backGreen by setting("BackgroundGreen", 0, 0..255, 1) { background }
    val backBlue by setting("BackgroundBlue", 0, 0..255, 1) { background }
    val backA by setting("BackgroundAlpha", 30, 0..255, 1) { background }
    val outline by setting("OutLine", true)
    val panelLine by setting("panelLine", true)
    private val rainbow by setting("Rainbow", false)
    val colorRed by setting("Red", 150, 0..255, 1) { !rainbow }
    val colorGreen by setting("Green", 25, 0..255, 1) { !rainbow }
    val colorBlue by setting("Blue", 255, 0..255, 1) { !rainbow }
    private val rectRainbow by setting("RectRainbow", false)
    private val rectRed by setting("RectRed", 35, 0..255, 1) { !rectRainbow }
    private val rectGreen by setting("RectGreen", 35, 0..255, 1) { !rectRainbow }
    private val rectBlue by setting("RectBlue", 35, 0..255, 1) { !rectRainbow }
    private val lineRainbow by setting("LineRainbow", false)
    private val lineRed by setting("LineRed", 102, 0..255, 1) { !lineRainbow && outline }
    private val lineGreen by setting("LineGreen", 0, 0..255, 1) { !lineRainbow && outline }
    private val lineBlue by setting("LineBlue", 255, 0..255, 1) { !lineRainbow && outline }
    private val panelRainbow by setting("PanelRainbow", false)
    private val panelRed by setting("PanelRed", 160, 0..255, 1) { !panelRainbow }
    private val panelGreen by setting("PanelGreen", 45, 0..255, 1) { !panelRainbow }
    private val panelBlue by setting("PanelBlue", 252, 0..255, 1) { !panelRainbow }
    private val speed by setting(
        "Speed",
        18,
        2..54,
        1
    ) { rainbow || rectRainbow || (lineRainbow && outline) || panelRainbow }
    private val saturation by setting(
        "Saturation",
        0.65f,
        0.0f..1.0f,
        0.01f
    ) { rainbow || rectRainbow || (lineRainbow && outline) }
    private val brightness by setting(
        "Brightness",
        1.0f,
        0.0f..1.0f,
        0.01f
    ) { rainbow || rectRainbow || (lineRainbow && outline) }
    private val astolfo2 = Identifier("textures/astolfo2.png")
    private val shana = Identifier("textures/037.png")
    private val mahiro = Identifier("textures/mahiro.png")
    private val qianshu = Identifier("textures/039.png")
    private val roxy = Identifier("textures/roxy.png")
    private val sexy = Identifier("textures/038.png")

    init {
        eventListener<Render2DEvent> { event ->
            if(QaQ) {
                val factor: Float = 1f - SytRender.clamp(player.health, 0f, 12f) / 12f
                val red = Color(Red, Green, Blue, A)
                if (factor < 1f)
                    Render2DEngine.draw2DGradientRect(
                        event.context.matrices, 0.0f, 0.0f,
                        mc.window.scaledWidth.toFloat(), mc.window.scaledHeight.toFloat(),
                        Render2DEngine.injectAlpha(red, (factor * 170f).toInt()), red,
                        Render2DEngine.injectAlpha(red, (factor * 170f).toInt()), red
                    )
            }

            val img = when (mode.value) {
                Mode.Shana -> shana
                Mode.Qianshu -> qianshu
                Mode.Astolfo2 -> astolfo2
                Mode.Roxy -> roxy
                Mode.Sexy -> sexy
                else -> mahiro
            }
            try {
                RenderSystem.disableBlend()
                width = 302f
                height = 460f
                event.context.drawTexture(
                    img,
                    waterMarkX,
                    waterMarkY,
                    0F,
                    0F,
                    waterX,
                    waterY,
                    waterX,
                    waterY
                )
                RenderSystem.enableBlend()
            } catch (e: Exception) {
                ChatUtil.sendRawMessage("Image failed!!")
            }
        }
    }

    init {
        onEnable {
            if (mc.currentScreen == null) mc.setScreen(ClickGUIScreen)
            else disable()
        }

        onDisable {
            if (mc.currentScreen == ClickGUIScreen) mc.setScreen(null)
        }
    }

    fun getColor(): Color {
        return if (rainbow) {
            Render2DEngine.rainbow(speed, 0, saturation, brightness, 1f)
        } else {
            Color(colorRed, colorGreen, colorBlue)
        }
    }

    fun getColorRect(): Color {
        return if (rectRainbow) {
            Render2DEngine.rainbow(speed, 0, saturation, brightness, 1f)
        } else {
            Color(rectRed, rectGreen, rectBlue)
        }
    }

    fun getColorLine(): Color {
        return if (lineRainbow) {
            Render2DEngine.rainbow(speed, 0, saturation, brightness, 1f)
        } else {
            Color(lineRed, lineGreen, lineBlue)
        }
    }

    fun getColorPanel(): Color {
        return if (panelRainbow) {
            Render2DEngine.rainbow(speed, 0, saturation, brightness, 1f)
        } else {
            Color(panelRed, panelGreen, panelBlue)
        }
    }

    enum class Mode {
        Shana, Mahiro, Qianshu, Astolfo2, Roxy, Sexy
    }
}