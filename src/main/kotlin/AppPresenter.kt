import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class AppPresenter(val scope: CoroutineScope) {

    @Composable
    fun models(events: Flow<AppEvent>): Model {
        val modelState: MutableState<Model> = remember { mutableStateOf(Model.Initializing) }


        return modelState.value
    }
}

sealed interface AppEvent {
    data object Initialize
    data object OpenSettings
    data object OpenLog
}
