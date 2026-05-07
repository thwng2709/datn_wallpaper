package com.itsthwng.twallpaper.ui.component.zipper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.repository.ZipperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZipperViewModel @Inject constructor(
    private val zipperRepository: ZipperRepository
) : ViewModel() {

    private val _zippers = MutableLiveData<List<ZipperImageEntity>>()
    val zippers: LiveData<List<ZipperImageEntity>> = _zippers

    private val _zippersByType = MutableLiveData<List<ZipperImageEntity>>()
    val zippersByType: LiveData<List<ZipperImageEntity>> = _zippersByType

    private val _zippersByTypeAndAccessType = MutableLiveData<List<ZipperImageEntity>>()
    val zippersByTypeAndAccessType: LiveData<List<ZipperImageEntity>> = _zippersByTypeAndAccessType

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadZippers() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                val zipperList = zipperRepository.getZippers()
                _zippers.postValue(zipperList)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun saveZipper(zipper: ZipperImageEntity) {
        viewModelScope.launch {
            zipperRepository.saveZipper(zipper)
        }
    }

    fun updateZipperAccessType(zipperId: Int){
        viewModelScope.launch {
            zipperRepository.updateZipperAccessType(zipperId)
        }
    }

    fun loadZippersByCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                val zipperList = zipperRepository.getZippersByCategory(categoryId)
                _zippers.postValue(zipperList)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadZippersByAccessType(accessType: Int) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                val zipperList = zipperRepository.getZippersByAccessType(accessType)
                _zippers.postValue(zipperList)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadZippersByType(type: String) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                val zipperList = zipperRepository.getZippersByType(type)
                _zippersByType.postValue(zipperList)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun loadZippersByTypeAndAccessType(type: String, accessType: Int) {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)
                val zipperList = zipperRepository.getZippersByTypeAndAccessType(type, accessType)
                _zippersByTypeAndAccessType.postValue(zipperList)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun getZipperById(id: Int): ZipperImageEntity? {
        return _zippers.value?.find { it.id == id }
    }

    fun clearError() {
        _error.postValue(null)
    }

    fun refresh() {
        // Refresh with current type if available, otherwise load all
        val currentType = _zippersByType.value?.firstOrNull()?.type
        if (currentType != null) {
            loadZippersByType(currentType)
        } else {
            loadZippers()
        }
    }
}
