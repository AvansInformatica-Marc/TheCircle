package nl.marc.thecircle.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.marc.thecircle.data.UserRepository

class PermissionsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val mutableHasSignedUp = MutableStateFlow<Boolean?>(null)

    val hasSignedUp: StateFlow<Boolean?>
        get() = mutableHasSignedUp

    init {
        viewModelScope.launch {
            mutableHasSignedUp.value = userRepository.isRegistered()
        }
    }
}
