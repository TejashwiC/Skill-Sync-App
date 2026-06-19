package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skillsync.app.data.model.Group
import com.skillsync.app.databinding.ItemGroupBinding

class GroupAdapter(
    private val onClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var groups: List<Group> = emptyList()

    fun submitList(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.binding.tvGroupName.text = group.name
        holder.binding.tvLastMessage.text = group.lastMessage

        if (!group.iconUrl.isNullOrEmpty()) {
            holder.binding.ivGroupIcon.load(group.iconUrl) {
                transformations(coil.transform.CircleCropTransformation())
            }
        } else {
            holder.binding.ivGroupIcon.setImageResource(com.skillsync.app.R.drawable.ic_users)
        }

        holder.itemView.setOnClickListener {
            onClick(group)
        }
    }

    override fun getItemCount(): Int = groups.size

    class GroupViewHolder(val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root)
}
