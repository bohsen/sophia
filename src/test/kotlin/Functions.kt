import okio.buffer
import okio.source

interface Functions {

    companion object {
        internal fun readFile(filename: String): String {
            val classLoader = this::class.java
            val file = classLoader.getResourceAsStream(filename).source().buffer()
            return file.readUtf8()
        }
    }
}