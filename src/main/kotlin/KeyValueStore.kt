import androidx.compose.runtime.Stable
import com.russhwolf.settings.*
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

interface KeyValueStore {

    var path: String?
    val observablePath: Flow<String>

    var userInfo: UserInfo?

    var tokenCard: TokenCard?

    var pipelines: Pipelines?
    fun reset()

    companion object {
        operator fun invoke(provider: () -> Settings = { Settings() }): KeyValueStore = KeyValueStoreImpl(provider)
    }
}

@Serializable
data class UserInfo(val userId: Int, val username: String, val clientId: Int)

@Serializable
data class Pipelines(val map: Map<String, Int>) {
    operator fun plus(pipelines: Pipelines): Pipelines {
        return Pipelines(map + pipelines.map)
    }

    operator fun get(key: String): Int? {
        return map[key]
    }
}

@Serializable
@Stable
data class Pipeline(
    val pipeline_id: Int,
    val pipeline_name: String,
    val analysis_type: String,
    val analysis_type_id: Int,
    val kit: String,
    val sequencer_id: Int,
    val sequencer: String,
    val experiment_type: String,
    val pairend: Boolean
)

@OptIn(ExperimentalSettingsApi::class, ExperimentalSerializationApi::class)
private class KeyValueStoreImpl(provider: () -> Settings) : KeyValueStore {
    private val settings: Settings by lazy { provider() }
    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    override var path: String?
        get() = settings["ObserverPath"]
        set(value) {
            if (value != null) {
                settings["ObserverPath"] = value
            } else {
                settings.remove("ObserverPath")
            }
        }

    override val observablePath: Flow<String>
        get() = observableSettings.getStringFlow("ObserverPath", "")

    override var userInfo: UserInfo?
        get() = settings.decodeValueOrNull(UserInfo.serializer(), "Userinfo")
        set(value) {
            if (value != null) {
                settings.encodeValue(UserInfo.serializer(), "Userinfo", value)
            } else {
                settings.remove("Userinfo")
            }
        }
    override var tokenCard: TokenCard?
        get() = settings.decodeValueOrNull(TokenCard.serializer(), "TokenCard")
        set(value) {
            if (value != null) {
                settings.encodeValue(TokenCard.serializer(), "TokenCard", value)
            } else {
                settings.remove("TokenCard")
            }
        }
    override var pipelines: Pipelines?
        get() = settings.decodeValueOrNull(Pipelines.serializer(), "Pipelines")
        set(value) {
            if (value != null) {
                settings.encodeValue(Pipelines.serializer(), "Pipelines", value)
            } else {
                settings.remove("Pipelines")
            }
        }

    override fun reset() {
        settings.clear()
    }
}