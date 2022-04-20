package us.dragonma.minecraft.server.launcher

import java.io.PrintStream

class Logger(private val out: PrintStream = System.out, private val err: PrintStream = System.err) {
    fun info(message: String) {
        out.println("### $message ###")
    }

    fun info2(message: String) {
        out.println("    - $message")
    }

    fun warn(message: String) {
        err.println("!!! $message !!!")
    }
}
