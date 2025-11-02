package dev.m7thh4ck.net.mod.module.impl.render

import dev.m7thh4ck.net.mod.module.Category
import dev.m7thh4ck.net.mod.module.Module
import dev.m7thh4ck.net.mod.module.impl.combat.M7thAura
import dev.m7thh4ck.net.mod.module.impl.combat.M7thAura.setting
import dev.m7thh4ck.net.settings.Setting

object Animation : Module("Animation", Category.Render) {
     val slowAnimation by setting("SlowAnimation", true)
     val slowAnimationVal by setting("SlowValue", 10, 0..50, 1)






}