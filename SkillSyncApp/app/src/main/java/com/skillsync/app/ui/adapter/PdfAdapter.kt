package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.PdfNote
import com.skillsync.app.databinding.ItemPdfBinding
import com.skillsync.app.util.FirebaseUtil

class PdfAdapter(
    private var pdfs: List<PdfNote> = emptyList(),
    private val onViewClick: (PdfNote) -> Unit,
    private val onDownloadClick: (PdfNote) -> Unit,
    private val onDeleteClick: (PdfNote) -> Unit
) : RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {

    fun submitList(newPdfs: List<PdfNote>) {
        pdfs = newPdfs
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = ItemPdfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.bind(pdfs[position])
    }

    override fun getItemCount(): Int = pdfs.size

    inner class PdfViewHolder(private val binding: ItemPdfBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pdf: PdfNote) {
            binding.tvPdfName.text = pdf.fileName
            
            val uploaderEmailOrName = if (pdf.uploadedByEmail.isNotEmpty()) pdf.uploadedByEmail else pdf.uploadedBy
            binding.tvPdfUploader.text = "Uploaded by: $uploaderEmailOrName"

            binding.btnViewPdf.setOnClickListener { onViewClick(pdf) }
            binding.btnDownloadPdf.setOnClickListener { onDownloadClick(pdf) }

            // Check if current user is the uploader of this PDF
            val isOwner = pdf.uploaderId == FirebaseUtil.currentUid
            if (isOwner) {
                binding.btnDeletePdf.visibility = View.VISIBLE
                binding.btnDeletePdf.setOnClickListener { onDeleteClick(pdf) }
            } else {
                binding.btnDeletePdf.visibility = View.GONE
            }
        }
    }
}
