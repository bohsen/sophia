import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyValueStoreTest {
    private val provider = { MapSettings() }
    private val settings = KeyValueStore(provider)

    @Test
    fun `default path should be emitted when not set`() = runTest {
        settings.observablePath.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `keyValueStore should contain directory path setting`() = runTest {
        val userHome = "/Users/sandy"
        settings.setPath(userHome)

        settings.observablePath.test {
            assertEquals(userHome, awaitItem())
        }
    }

    @Test
    fun `directoryPath change should emit new path`() = runTest {
        settings.observablePath.test {
            assertEquals("", awaitItem())
            val userHome = "/Users/sandy"
            settings.setPath(userHome)
            assertEquals(userHome, awaitItem())
        }
    }

    @Test
    fun `reset should clear KeyValueStore`() = runTest {
        val userHome = "/Users/sandy"
        settings.setPath(userHome)
        settings.observablePath.test {
            assertEquals(userHome, awaitItem())
            settings.reset()
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun `default UserInfo should be null`() {
        assertThat(settings.userInfo).isNull()
    }

    @Test
    fun `keyValueStore should contain UserInfo when set`() {
        val expected = UserInfo(405, "dnoble", 12)
        settings.userInfo = expected
        assertThat(settings.userInfo).isEqualTo(expected)
    }

    @Test
    fun `default Pipelines should be null`() {
        assertThat(settings.pipelines).isNull()
    }

    @Test
    fun `keyValueStore should contain Pipelines when set`() {
        val expected = Pipelines(mapOf("SST" to 1811, "STS" to 42))
        settings.pipelines = expected
        assertThat(settings.pipelines).isEqualTo(expected)
    }

    @Test
    fun `keyValueStore should contain new Pipelines when added`() {
        val expected = Pipelines(mapOf("SST" to 1811, "STS" to 42)) + Pipelines(mapOf("SIS" to 65))
        settings.pipelines = expected
        assertThat(settings.pipelines).isEqualTo(expected)
    }

    @Test
    fun `Pipelines should return pipelinenumber`() {
        val pipelines = Pipelines(mapOf("SST" to 1811, "STS" to 42))
        assertThat(pipelines["SST"]).isEqualTo(1811)
    }
}