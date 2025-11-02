package dev.kura.net.mod.module.impl.render

import dev.kura.net.event.eventListener
import dev.kura.net.event.impl.EventHeldItemRenderer
import dev.kura.net.mod.module.Category
import dev.kura.net.mod.module.Module
import net.minecraft.item.PotionItem
import net.minecraft.util.Hand
import net.minecraft.util.Hand.*
import net.minecraft.util.math.RotationAxis


object ViewModel : Module("ViewModel", Category.Render) {
    // Scale
    val positionMainX by setting("positionMainX", 0.0, -3.0..2.0, 0.1)
    val positionMainY by setting("positionMainY", 0.0, -3.0..2.0, 0.1)
    val positionMainZ by setting("positionMainZ", 0.0, -3.0..2.0, 0.1)
    val scale by setting("scaleMainX", 1.0f, 0.1f..1.5f, 0.1f)

    // Rotation
    val rotationMainX by setting("rotMainX", 0, -180..180, 1)
    val rotationMainY by setting("rotMainY", 0, -180..180, 1)
    val rotationMainZ by setting("rotMainZ", 0, -180..180, 1)


    val rotationOffX by setting("rotationOffX", 0, -180..180, 1)
    val rotationOffY by setting("rotationOffY", 0, -180..180, 1)
    val rotationOffZ by setting("rotationOffZ", 0, -180..180, 1)
    val speedAnimateOff by setting("speedAnimateOff", 1.0, 1.0..5.0, 0.1)
    val speedAnimateMain by setting("speedAnimateMain", 1.0, 1.0..5.0, 0.1)

    // Eat
    val eatMainX by setting("eatMainX", 0.0, -2.0..2.0, 0.1)
    val eatMainY by setting("eatMainY", 0.0, -2.0..2.0, 0.1)


    private var mainHand by setting("mainHand", false)
    private var rotationMain by setting("rotationMain", false){mainHand}
    private var animateMain by setting("animateMain", false){mainHand}
    private var animateMainX by setting("animateMainX", false){animateMain}
    private var animateMainY by setting("animateMainY", false){animateMain}
    private var animateMainZ by setting("animateMainZ", false){animateMain}
    private var offHand by setting("offHand", false)
    private var animateOff by setting("animateOff", false){offHand}
    private var animateOffX by setting("animateOffX", false){offHand}
    private var animateOffY by setting("animateOffY", false){offHand}
    private var animateOffZ by setting("animateOffZ", false){offHand}








    init {

        eventListener<EventHeldItemRenderer> { event ->
            if (true) {
                if (animateMainX) rotationMainX
                    changeRotate(
                        rotationMainX,
                        speedAnimateMain
                    ).toFloat()

                if (animateMainY) rotationMainY
                    changeRotate(
                        rotationMainY,
                        speedAnimateMain
                    ).toFloat()

                if (animateMainZ) rotationMainZ
                    changeRotate(
                        rotationMainZ,
                        speedAnimateMain
                    ).toFloat()
                event.getStack().translate(positionMainX.toDouble(), positionMainY.toDouble(), positionMainZ.toDouble())
                event.getStack().scale(scale, scale, scale)
                event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationMainX.toFloat()))
                event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationMainY.toFloat()))
                event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationMainZ.toFloat()))
            } else {
                if (animateOffX) rotationOffX
                    changeRotate(
                        rotationOffX,
                        speedAnimateOff
                    ).toFloat()

                if (animateOffY) rotationOffY
                    changeRotate(
                        rotationOffY,
                        speedAnimateOff
                    ).toFloat()

                if (animateOffZ) rotationOffZ
                    changeRotate(
                        rotationOffZ,
                        speedAnimateOff
                    ).toFloat()

                event.getStack()
                    .translate((-positionMainX).toDouble(), positionMainY.toDouble(), positionMainZ.toDouble())
                event.getStack().scale(scale, scale, scale)
                event.getStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotationOffX.toFloat()))
                event.getStack().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotationOffY.toFloat()))
                event.getStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationOffZ.toFloat()))
            }
        }
    }

    private fun changeRotate(value: Int, speed: Double): Double {
        return if (value - speed <= 180.0 && value - speed > -180.0) value - speed else 180.0
    }

    fun isUsingItem(hand: Hand): Boolean {
            return when (hand) {
                MAIN_HAND -> mc.player!!.isUsingItem && (mc.player!!.mainHandStack.item is PotionItem || mc.player!!.mainHandStack.item.isFood)
                OFF_HAND -> mc.player!!.isUsingItem && (mc.player!!.offHandStack.item is PotionItem || mc.player!!.offHandStack.item.isFood)
            }
        }

}