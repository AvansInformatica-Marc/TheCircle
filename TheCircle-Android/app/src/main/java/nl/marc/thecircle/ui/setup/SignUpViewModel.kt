package nl.marc.thecircle.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nl.marc.thecircle.data.UserRepository

class SignUpViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val mutableHasSignedUp = MutableStateFlow(false)

    val hasSignedUp: StateFlow<Boolean>
        get() = mutableHasSignedUp

    fun signup(name: String) {
        viewModelScope.launch {
            userRepository.register(name)
            mutableHasSignedUp.value = true
        }
    }
}
