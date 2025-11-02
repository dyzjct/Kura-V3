package dev.kura.net.command.impl

import dev.kura.net.command.Command


//  定义一个名为HelpCommand的类，继承自Command类
class HelpCommand  //  定义HelpCommand类的构造函数
    : Command(arrayOf("h", "help")) {
    //  重写父类Command的run方法，接收一个字符串数组作为参数
    override fun run(args: Array<String>) {
        //  打印字符串数组args的内容
        println(args.contentToString())
    }
}
