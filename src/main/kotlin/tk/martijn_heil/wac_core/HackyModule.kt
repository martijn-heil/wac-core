/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.martijn_heil.wac_core

import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Level
import java.util.logging.Logger


object HackyModule : Closeable {
    var p: Process? = null
    lateinit var logger: Logger
    lateinit var scriptFile: File
    lateinit var rhinoFile: File
    val t: Thread = Thread {
        p = ProcessBuilder().directory(WacCore.plugin.dataFolder.absoluteFile).command("java", "-jar", "trireme.jar", "master.js").start()
        StreamHandlerThread(LoggerOutputStream(logger, Level.INFO), p!!.inputStream).start()
        StreamHandlerThread(LoggerOutputStream(logger, Level.WARNING), p!!.errorStream).start()
    }

    fun init(logger: Logger) {
        this.logger = logger
        scriptFile = File(WacCore.plugin.dataFolder.path + "/master.js")
        if(!scriptFile.exists()) throw Exception("Could not find master.js at " + WacCore.plugin.dataFolder.path + "/master.js")

        rhinoFile = File(WacCore.plugin.dataFolder.path + "/rhino.jar")
        if(!rhinoFile.exists()) throw Exception("Could not find rhino.js at " + WacCore.plugin.dataFolder.path + "/master.js")

        t.start()
    }

    override fun close() {
        t.interrupt()
        p?.destroyForcibly()
    }

    class StreamHandlerThread(private val out: OutputStream, private val `in`: InputStream) : Thread() {
        override fun run() {
            val buffer = ByteArray(1024)
            var len = `in`.read(buffer)
            while (len != -1) {
                out.write(buffer, 0, len)
                out.flush()
                len = `in`.read(buffer)
                if (Thread.interrupted()) {
                    throw InterruptedException()
                }
            }
        }
    }
}