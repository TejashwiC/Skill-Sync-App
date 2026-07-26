package com.skillsync.app.ui.notes

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.databinding.FragmentNotesBinding
import com.skillsync.app.ui.adapter.PdfAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import java.io.File
import java.io.FileOutputStream

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: PdfAdapter

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = getFileName(it)
            binding.tvChosenFileName.text = selectedFileName
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup File Chooser
        binding.btnChooseFile.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }

        // Setup Upload Button
        binding.btnUploadPdfSubmit.setOnClickListener {
            val uri = selectedFileUri
            if (uri == null) {
                showToast("Please choose a PDF file first")
                return@setOnClickListener
            }
            if (selectedFileName.isEmpty()) {
                showToast("Invalid file name")
                return@setOnClickListener
            }

            try {
                // Read the selected PDF file's bytes immediately on main thread
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.use { it.readBytes() }

                if (fileBytes == null || fileBytes.isEmpty()) {
                    showToast("Failed to read selected PDF file")
                    return@setOnClickListener
                }

                // Check file size (max 700 KB for Firestore limits)
                if (fileBytes.size > 700 * 1024) {
                    showToast("File is too large (Max 700 KB limit)")
                    return@setOnClickListener
                }

                viewModel.uploadPdfFile(selectedFileName, fileBytes)

            } catch (e: Exception) {
                showToast("Failed to process file: ${e.message}")
            }
        }

        // Setup RecyclerView with View, Download, and Delete actions
        adapter = PdfAdapter(
            onViewClick = { pdf -> openPdfInViewer(pdf.fileName, pdf.fileURL) },
            onDownloadClick = { pdf -> downloadPdf(pdf.fileName, pdf.fileURL) },
            onDeleteClick = { pdf ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete \"${pdf.fileName}\"?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deletePdfFile(pdf.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvPdfs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPdfs.adapter = adapter

        // Observers
        viewModel.pdfsList.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyPdfs.show()
                binding.rvPdfs.hide()
            } else {
                binding.tvEmptyPdfs.hide()
                binding.rvPdfs.show()
                adapter.submitList(list)
            }
        }

        // Hide old progress bar since base64 upload is instantaneous (no Firestore Storage step)
        binding.pbUploadProgress.hide()

        viewModel.uploadResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("PDF Note uploaded successfully!")
                    // Clear inputs
                    selectedFileUri = null
                    selectedFileName = ""
                    binding.tvChosenFileName.text = "No file chosen"
                } else {
                    showToast("Upload failed: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetUploadResult()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("PDF Note deleted")
                } else {
                    showToast("Delete failed: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetDeleteResult()
            }
        }
    }

    private fun getFileName(uri: Uri): String {
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
            name = uri.lastPathSegment ?: "untitled.pdf"
        }
        return name
    }

    private fun openPdfInViewer(fileName: String, url: String) {
        if (url.isEmpty()) {
            showToast("Error: Document link is empty")
            return
        }

        if (url.startsWith("data:")) {
            try {
                // Decode PDF bytes from base64
                val base64Data = url.substringAfter("base64,")
                val pdfBytes = Base64.decode(base64Data, Base64.DEFAULT)

                // Save to a temporary file in the cache directory
                val tempFile = File(requireContext().cacheDir, fileName)
                FileOutputStream(tempFile).use { it.write(pdfBytes) }

                // Get content URI via FileProvider
                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.skillsync.app.fileprovider",
                    tempFile
                )

                // Open with a system PDF reader chooser
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(intent, "Open PDF with..."))

            } catch (e: Exception) {
                showToast("Cannot view document: ${e.message}")
            }
        } else {
            // Fallback for standard HTTP links
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                showToast("Cannot view link: ${e.message}")
            }
        }
    }

    private fun downloadPdf(fileName: String, url: String) {
        if (url.isEmpty()) {
            showToast("Error: Document link is empty")
            return
        }

        if (url.startsWith("data:")) {
            try {
                // Decode PDF bytes from base64
                val base64Data = url.substringAfter("base64,")
                val pdfBytes = Base64.decode(base64Data, Base64.DEFAULT)

                val resolver = requireContext().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                }

                val downloadUri = resolver.insert(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI
                    } else {
                        @Suppress("DEPRECATION")
                        Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                    },
                    contentValues
                )

                if (downloadUri != null) {
                    resolver.openOutputStream(downloadUri)?.use { it.write(pdfBytes) }
                    showToast("Downloaded to Downloads folder successfully!")
                } else {
                    // Fallback to direct app external cache directory if MediaStore insert failed
                    val downloadDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val localFile = File(downloadDir, fileName)
                    FileOutputStream(localFile).use { it.write(pdfBytes) }
                    showToast("Saved to App Downloads: ${localFile.absolutePath}")
                }

            } catch (e: Exception) {
                showToast("Download failed: ${e.message}")
            }
        } else {
            // Fallback for standard HTTP links
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
                showToast("Opening download link in browser...")
            } catch (e: Exception) {
                showToast("Cannot trigger download: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
