package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skillsync.app.data.model.User
import com.skillsync.app.databinding.ItemUserBinding

class SelectUserAdapter : RecyclerView.Adapter<SelectUserAdapter.SelectUserViewHolder>() {

    private var users: List<User> = emptyList()
    val selectedUserIds = mutableSetOf<String>()

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectUserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SelectUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SelectUserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvUserName.text = user.name
        holder.binding.tvUserEmail.text = "Select for Group"
        holder.binding.btnFollow.visibility = android.view.View.GONE
        holder.binding.btnViewProfile.visibility = android.view.View.GONE
        
        if (selectedUserIds.contains(user.uid)) {
            holder.itemView.setBackgroundColor(android.graphics.Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        if (user.photo.isNotEmpty()) {
            holder.binding.ivUserPhoto.load(user.photo) {
                transformations(coil.transform.CircleCropTransformation())
            }
        } else {
            holder.binding.ivUserPhoto.setImageResource(com.skillsync.app.R.drawable.ic_users)
        }

        holder.itemView.setOnClickListener {
            if (selectedUserIds.contains(user.uid)) {
                selectedUserIds.remove(user.uid)
            } else {
                selectedUserIds.add(user.uid)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = users.size

    class SelectUserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)
}
