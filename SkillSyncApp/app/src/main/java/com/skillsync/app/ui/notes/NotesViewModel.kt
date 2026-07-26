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
    private val userRepository = com.skillsync.app.data.repository.UserRepository()

    private val _pdfsList = MutableLiveData<List<PdfNote>>()
    val pdfsList: LiveData<List<PdfNote>> = _pdfsList

    private val _uploadProgress = MutableLiveData<Double>(0.0)
    val uploadProgress: LiveData<Double> = _uploadProgress

    private val _uploadResult = MutableLiveData<Result<Unit>?>()
    val uploadResult: LiveData<Result<Unit>?> = _uploadResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private var pdfsJob: kotlinx.coroutines.Job? = null

    init {
        loadPdfs()
    }

    private fun loadPdfs() {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return

        viewModelScope.launch {
            userRepository.observeUserProfile(uid).collectLatest { user ->
                val following = user?.following ?: emptyList()
                val followers = user?.followers ?: emptyList()
                val mutualConnections = following.filter { followers.contains(it) }

                pdfsJob?.cancel()
                pdfsJob = viewModelScope.launch {
                    pdfRepository.observeAllPdfs().collectLatest { list ->
                        // Filter: uploader is self OR uploader is a mutual connection
                        val filtered = list.filter { pdf ->
                            pdf.uploaderId == uid || mutualConnections.contains(pdf.uploaderId)
                        }
                        _pdfsList.postValue(filtered)
                    }
                }
            }
        }
    }

    fun uploadPdfFile(fileName: String, fileBytes: ByteArray) {
        val uid = FirebaseUtil.currentUid
        if (uid.isEmpty()) return
        val userName = FirebaseUtil.currentUser?.displayName ?: "Student"
        val userEmail = FirebaseUtil.currentUser?.email ?: ""
        
        viewModelScope.launch {
            val result = pdfRepository.uploadPdf(fileName, fileBytes, userName, userEmail, uid)
            _uploadResult.postValue(result)
        }
    }

    fun deletePdfFile(pdfId: String) {
        viewModelScope.launch {
            val result = pdfRepository.deletePdf(pdfId)
            _deleteResult.postValue(result)
        }
    }

    fun resetUploadResult() {
        _uploadResult.value = null
        _uploadProgress.value = 0.0
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }
}
