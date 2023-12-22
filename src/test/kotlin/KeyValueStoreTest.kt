import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyValueStoreTest {
    private val provider = { MapSettings() }
    private val settings = KeyValueStore(provider)

    private val tokenMap: Map<String, List<String>> = mapOf(
        "A" to "iuh7 9iik lkmd ssuu sbna wyyh mkki aayh".split(" "),
        "B" to "nnhs yyhh aamj wpod mcjy aakl qldo siah".split(" "),
        "C" to "amaj weks 99jd ala9 smn6 aqq8 ssm8 xxuu".split(" "),
        "D" to "8a9a aakk 9w8d vmvu xxn7 65ee ddl9 xnh8".split(" "),
        "E" to "xjs7 aak9 vv87 z765 c765 xx92 117h sslk".split(" "),
        "F" to "mvnu xxb3 84fw ww66 aa83 sw78 11jr 725e".split(" "),
        "G" to "a9au a7ue ve84 slxo x763 d7d7 l93w u756".split(" "),
        "H" to "am12 lw92 ie7x m7tg 9hg6 7gbc cu72 si82".split(" "),
    )
    private val tokenCard = TokenCard(14567, "Test Peter Testesen", "Feb 28, 2025", tokenMap)

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

    @Test
    fun `default tokencard should be null`() {
        assertThat(settings.tokenCard).isNull()
    }

    @Test
    fun `should contain TokenCard when added`() {
        val expectedTokenCard = tokenCard
        settings.tokenCard = expectedTokenCard
        assertThat(settings.tokenCard).isEqualTo(expectedTokenCard)
    }
}