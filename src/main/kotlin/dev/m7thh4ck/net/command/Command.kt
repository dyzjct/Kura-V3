package dev.m7thh4ck.net.command

abstract class Command(
    val key: Array<String>
) {
    abstract fun run(args: Array<String>)
}
