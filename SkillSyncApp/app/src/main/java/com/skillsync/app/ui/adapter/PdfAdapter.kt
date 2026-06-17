package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skillsync.app.data.model.PdfNote
import com.skillsync.app.databinding.ItemPdfBinding

class PdfAdapter(
    private var pdfs: List<PdfNote> = emptyList(),
    private val onViewClick: (PdfNote) -> Unit,
    private val onDownloadClick: (PdfNote) -> Unit
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
            binding.tvPdfUploader.text = "By: ${pdf.uploadedBy}"

            binding.btnViewPdf.setOnClickListener { onViewClick(pdf) }
            binding.btnDownloadPdf.setOnClickListener { onDownloadClick(pdf) }
        }
    }
}
