package com.skillsync.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.skillsync.app.R
import com.skillsync.app.data.model.User
import com.skillsync.app.databinding.ItemUserBinding
import com.skillsync.app.util.FirebaseUtil

class UserAdapter(
    private var users: List<User> = emptyList(),
    private val onFollowClick: (User) -> Unit,
    private val onViewProfileClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            
            if (user.photo.isNotEmpty() && user.photo.length > 10) {
                if (user.photo.startsWith("data:image")) {
                    try {
                        val base64String = user.photo.substringAfter("base64,")
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.ivUserPhoto.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        binding.ivUserPhoto.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(user.name))
                    }
                } else {
                    binding.ivUserPhoto.load(user.photo) {
                        placeholder(android.R.drawable.sym_def_app_icon)
                        error(android.R.drawable.sym_def_app_icon)
                    }
                }
            } else {
                binding.ivUserPhoto.setImageBitmap(com.skillsync.app.util.AvatarGenerator.generateAvatar(user.name))
            }

            val currentUid = FirebaseUtil.currentUid
            if (user.followers.contains(currentUid)) {
                binding.btnFollow.text = "Unfollow"
            } else {
                binding.btnFollow.text = "Follow"
            }

            binding.btnFollow.setOnClickListener { onFollowClick(user) }
            binding.btnViewProfile.setOnClickListener { onViewProfileClick(user) }
        }
    }
}
