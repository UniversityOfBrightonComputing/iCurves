package icurves.util

import java.util.*

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
object Profiler {

    private val map = LinkedHashMap<String, Long>()

    fun reset() {
        map.clear()
    }

    fun start(name: String) {
        println("Starting $name")

        map[name] = System.nanoTime()
    }

    fun end(name: String) {
        val time = System.nanoTime() - map[name]!!

        println("%s took: %.3f sec".format(name, time / 1000000000.0))
        //map[name] = System.nanoTime() - map[name]!!
    }

    fun print() {
        map.forEach { name, time ->
            println("$name took: ${time / 1000000000.0} sec")
        }
    }
}