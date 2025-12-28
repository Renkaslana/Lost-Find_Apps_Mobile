package com.campus.lostfound.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campus.lostfound.data.model.Category
import com.campus.lostfound.data.model.ItemType
import com.campus.lostfound.data.model.LostFoundItem
import com.campus.lostfound.data.repository.LostFoundRepository
import com.campus.lostfound.util.WhatsAppUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

data class AddReportUiState(
    val itemType: ItemType = ItemType.LOST,
    val itemName: String = "",
    val category: Category = Category.OTHER,
    val location: String = "",
    val description: String = "",
    val whatsappNumber: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AddReportViewModel(
    private val context: Context,
    private val repository: LostFoundRepository = LostFoundRepository(context)
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddReportUiState())
    val uiState: StateFlow<AddReportUiState> = _uiState.asStateFlow()
    
    fun setItemType(type: ItemType) {
        _uiState.value = _uiState.value.copy(itemType = type)
    }
    
    fun setItemName(name: String) {
        _uiState.value = _uiState.value.copy(itemName = name)
    }
    
    fun setCategory(category: Category) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    fun setLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }
    
    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(
            description = description.take(500)
        )
    }
    
    fun setWhatsAppNumber(number: String) {
        // Auto-format nomor saat user input
        // Biarkan user mengetik bebas, format akan dilakukan saat submit
        _uiState.value = _uiState.value.copy(whatsappNumber = number)
    }
    
    fun setImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }
    
    fun validateForm(): Boolean {
        val state = _uiState.value
        val cleanNumber = state.whatsappNumber.replace(Regex("[^0-9]"), "")
        return state.itemName.isNotBlank() &&
                state.location.isNotBlank() &&
                cleanNumber.isNotBlank() &&
                WhatsAppUtil.isValidIndonesianPhoneNumber(state.whatsappNumber)
    }
    
    fun submitReport(onSuccess: () -> Unit) {
        if (!validateForm()) {
            val cleanNumber = _uiState.value.whatsappNumber.replace(Regex("[^0-9]"), "")
            val errorMsg = when {
                _uiState.value.itemName.isBlank() -> "Nama barang wajib diisi"
                _uiState.value.location.isBlank() -> "Lokasi wajib diisi"
                cleanNumber.isBlank() -> "Nomor WhatsApp wajib diisi"
                !WhatsAppUtil.isValidIndonesianPhoneNumber(_uiState.value.whatsappNumber) -> 
                    "Format nomor WhatsApp tidak valid. Gunakan format: 08123456789 atau 628123456789"
                else -> "Harap lengkapi semua field yang wajib"
            }
            _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Format nomor ke format internasional sebelum disimpan
            val formattedNumber = WhatsAppUtil.formatPhoneNumber(_uiState.value.whatsappNumber)

            val item = LostFoundItem(
                type = _uiState.value.itemType,
                itemName = _uiState.value.itemName,
                category = _uiState.value.category,
                location = _uiState.value.location,
                description = _uiState.value.description,
                whatsappNumber = formattedNumber
            )

            Log.d("AddReportViewModel", "Submitting report. Current auth uid=${FirebaseAuth.getInstance().currentUser?.uid}")
            val result = try {
                repository.addItem(item, _uiState.value.imageUri)
            } catch (ex: Exception) {
                Log.e("AddReportViewModel", "addItem threw", ex)
                Result.failure<String>(ex)
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    Log.e("AddReportViewModel", "addItem failed: ${error.message}", error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Gagal mengirim laporan"
                    )
                }
            )
        }
    }
    
    fun resetState() {
        _uiState.value = AddReportUiState()
    }
}

