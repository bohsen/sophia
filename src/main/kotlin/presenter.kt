import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

class MainPresenter

data class MainViewModel(val loading: Boolean)

sealed interface MainScreenEvent
sealed class Initializing : MainScreenEvent {
    sealed class LoginResult {
        data object Success
        data object Failure
    }

}


interface MoleculePresenter<Event, Model> {

    @Composable
    fun present(events: Flow<Event>): Model

}
