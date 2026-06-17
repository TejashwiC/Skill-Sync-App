package com.skillsync.app.ui.notes

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skillsync.app.data.model.PdfNote
import com.skillsync.app.data.repository.PdfRepository
import com.skillsync.app.util.FirebaseUtil
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {

    private val pdfRepository = PdfRepository()

    private val _pdfsList = MutableLiveData<List<PdfNote>>()
    val pdfsList: LiveData<List<PdfNote>> = _pdfsList

    private val _uploadProgress = MutableLiveData<Double>(0.0)
    val uploadProgress: LiveData<Double> = _uploadProgress

    private val _uploadResult = MutableLiveData<Result<Unit>?>()
    val uploadResult: LiveData<Result<Unit>?> = _uploadResult

    init {
        loadPdfs()
    }

    private fun loadPdfs() {
        viewModelScope.launch {
            pdfRepository.observeAllPdfs().collectLatest { list ->
                _pdfsList.postValue(list)
            }
        }
    }

    fun uploadPdfFile(fileName: String, fileUri: Uri) {
        val userName = FirebaseUtil.currentUser?.displayName ?: "Student"
        
        _uploadProgress.value = 0.0
        viewModelScope.launch {
            val result = pdfRepository.uploadPdf(fileName, fileUri, userName) { progress ->
                _uploadProgress.postValue(progress)
            }
            _uploadResult.postValue(result)
        }
    }

    fun resetUploadResult() {
        _uploadResult.value = null
        _uploadProgress.value = 0.0
    }
}
