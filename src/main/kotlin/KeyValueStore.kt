import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

interface KeyValueStore {

    val observablePath: Flow<String>
    fun setPath(path: String)

    var userInfo: UserInfo?

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

    override fun setPath(path: String) {
        settings["ObserverPath"] = path
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