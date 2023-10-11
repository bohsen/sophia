import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import org.apache.logging.log4j.kotlin.logger
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchService
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.*


/**
 * Creates a WatchService for watching directories for filechanges.
 */
class WatchService(private val watcher: WatchService = FileSystems.getDefault().newWatchService()) {
    private val logger = logger()
    private val keys: MutableMap<WatchKey, Path> = HashMap()

    init {
        logger.info("Initializing WatchService")
    }

    /**
     * Register the given directory with the WatchService
     */
    fun register(dir: Path) {
        logger.info("Register: $dir")
        val key = dir.register(watcher, ENTRY_CREATE)
        keys[key] = dir
    }

    /**
     * Register the given directory, and all its subdirectories, with the
     * WatchService.
     */
    @OptIn(ExperimentalPathApi::class)
    fun registerAll(start: Path) {
        logger.info("RegisterAll started.")
        start.walk(PathWalkOption.INCLUDE_DIRECTORIES).forEach {
            if (it.isDirectory()) {
                register(it)
            }
        }
        logger.info("RegisterAll finished.")
    }

    /**
     * Process all events for keys queued to the watcher
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    @OptIn(DelicateCoroutinesApi::class)
    @Throws(FileNotFoundException::class, IOException::class)
    fun processEvents() = channelFlow {
        logger.info("Listening for new files started.")
        while (isActive && !isClosedForSend) {
            // wait for key to be signalled
            val key: WatchKey = try {
                watcher.take()
            } catch (ex: InterruptedException) {
                logger.error("Watcher.take() threw InterruptedException", ex)
                throw ex
            } catch (ex: ClosedWatchServiceException) {
                logger.warn("Watcher was closed", ex)
                throw ex
            }

            val dir = keys[key]
            if (dir == null) {
                logger.warn("WatchKey not recognized!!")
                continue
            }
            for (event in key.pollEvents()) {
                val kind = event.kind()
                val fileName = event.context() as Path
                val path = dir.resolve(fileName)

                when (kind) {
                    OVERFLOW -> {
                        logger.warn("OVERFLOW Event: This indicates that events may have been lost or discarded")
                        continue
                    }

                    ENTRY_CREATE -> {
                        logger.info(kind.name() + ": " + path)
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                            register(path)
                        }
                        val result = trySend(path)
                        logger.info { "Send to channel was successful: ${result.isSuccess}" }

                    }

                    ENTRY_MODIFY -> {
                        logger.info(kind.name() + ": " + path)
                    }

                    ENTRY_DELETE -> {
                        logger.info(kind.name() + ": " + path)
                    }

                    else -> {
                        logger.error("Unknown kind: ${kind.name()}")
                    }
                }
            }

            val valid = key.reset()
            if (!valid) {
                keys.remove(key)
                if (keys.isEmpty()) {
                    val ex = IllegalStateException("No keys registered")
                    logger.error(ex)
                    throw ex
                }
            }
        }
    }.flowOn(Dispatchers.IO).buffer()
}