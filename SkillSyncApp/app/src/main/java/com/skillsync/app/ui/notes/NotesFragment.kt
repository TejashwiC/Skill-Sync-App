package com.skillsync.app.ui.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.databinding.FragmentNotesBinding
import com.skillsync.app.ui.adapter.PdfAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()
    private lateinit var adapter: PdfAdapter

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

        adapter = PdfAdapter(
            onViewClick = { pdf ->
                openPdfLink(pdf.fileURL)
            },
            onDownloadClick = { pdf ->
                openPdfLink(pdf.fileURL) // Fallback trigger for web/browser download
            }
        )
        binding.rvPdfs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPdfs.adapter = adapter

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

        binding.btnGoUpload.setOnClickListener {
            findNavController().navigate(R.id.nav_upload_pdf)
        }
    }

    private fun openPdfLink(link: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Failed to view note link: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
