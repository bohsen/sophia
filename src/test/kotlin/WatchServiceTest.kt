import app.cash.turbine.turbineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import kotlin.io.use

@OptIn(ExperimentalCoroutinesApi::class)
class WatchServiceTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    private val watchService = WatchService()

    @Test
    @Throws(IOException::class)
    fun `test file write read setup`() {

        // Create a temporary file.
        val tempFile = tempFolder.newFile("tempFile.txt")

        // Write something to it.
        tempFile.sink().buffer().use { sink ->
            sink.writeUtf8("hello world")
        }

        val output = tempFile.source().buffer().use {
            it.readUtf8Line()
        }
        // Verify the content
        assertEquals("hello world", output)
    }

    @Test
    @Throws(IOException::class)
    fun `register registers creation of new file and emits an event`() = runTest(UnconfinedTestDispatcher()) {
        val folder = tempFolder.root.toPath()

        watchService.register(folder)
        turbineScope {
            val turbine = watchService.processEvents().testIn(backgroundScope)
            val expectedPath = tempFolder.newFolder("test").toPath()
            val item = turbine.awaitItem()
            assertEquals(expectedPath, item)
        }
    }

    @Test
    @Throws(IOException::class)
    fun `registerAll registers creation of new file in subdirectory and emits correct events`() = runTest(
        UnconfinedTestDispatcher()
    ) {
        val folder = tempFolder.root.toPath()
        repeat(3) {
            tempFolder.newFolder("test$it")
        }

        watchService.registerAll(folder)
        turbineScope {
            val turbine = watchService.processEvents().testIn(backgroundScope)

            val expectedFolder = tempFolder.newFolder("test5").toPath()

            assertEquals(expectedFolder, turbine.awaitItem())

            val expectedPath = expectedFolder.resolve("CopyComplete.txt")
            assertTrue(expectedPath.toFile().createNewFile())
            assertEquals(expectedPath, turbine.awaitItem())
        }
    }
}