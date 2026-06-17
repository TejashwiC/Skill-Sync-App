package com.skillsync.app.ui.notes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.skillsync.app.databinding.FragmentUploadPdfBinding
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class UploadPdfFragment : Fragment() {

    private var _binding: FragmentUploadPdfBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private var selectedFileUri: Uri? = null
    private var originalFileName: String = ""

    // Activity Result Launcher for PDF file selection
    private val selectPdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                originalFileName = getFileNameFromUri(uri)
                binding.tvChosenFileName.text = "Selected: $originalFileName"
                if (binding.etPdfFileName.text.isNullOrEmpty()) {
                    binding.etPdfFileName.setText(originalFileName.substringBeforeLast("."))
                }
                binding.btnUploadSubmit.isEnabled = true
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadPdfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            selectPdfLauncher.launch(intent)
        }

        binding.btnUploadSubmit.setOnClickListener {
            val uri = selectedFileUri ?: return@setOnClickListener
            var customName = binding.etPdfFileName.text.toString().trim()
            if (customName.isEmpty()) {
                customName = originalFileName.substringBeforeLast(".")
            }
            val finalFileName = "$customName.pdf"

            binding.btnChooseFile.isEnabled = false
            binding.btnUploadSubmit.isEnabled = false
            binding.layoutUploadProgress.show()

            viewModel.uploadPdfFile(finalFileName, uri)
        }

        viewModel.uploadProgress.observe(viewLifecycleOwner) { progress ->
            progress?.let {
                binding.uploadProgressBar.progress = it.toInt()
                binding.tvUploadPercentage.text = "Uploading: ${it.toInt()}%"
            }
        }

        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.btnChooseFile.isEnabled = true
                binding.btnUploadSubmit.isEnabled = true
                binding.layoutUploadProgress.hide()

                if (it.isSuccess) {
                    showToast("PDF Uploaded Successfully!")
                    viewModel.resetUploadResult()
                    findNavController().popBackStack()
                } else {
                    showToast("Upload Failed: ${it.exceptionOrNull()?.message}")
                    viewModel.resetUploadResult()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = ""
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        if (name.isEmpty()) {
            name = uri.lastPathSegment ?: "document.pdf"
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
