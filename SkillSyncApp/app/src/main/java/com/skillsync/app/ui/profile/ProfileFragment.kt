package com.skillsync.app.ui.profile

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.skillsync.app.R
import com.skillsync.app.data.repository.AuthRepository
import com.skillsync.app.databinding.FragmentProfileBinding
import com.skillsync.app.ui.auth.LoginActivity
import com.skillsync.app.util.AvatarGenerator
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val authRepository = AuthRepository()

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.pbPhotoUpload.show()
            try {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (originalBitmap != null) {
                    val maxDim = 300f
                    val scale = Math.min(maxDim / originalBitmap.width, maxDim / originalBitmap.height)
                    val w = Math.max(1, (originalBitmap.width * scale).toInt())
                    val h = Math.max(1, (originalBitmap.height * scale).toInt())
                    
                    val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, w, h, true)
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    
                    val finalString = "data:image/jpeg;base64,$base64String"
                    viewModel.uploadProfileImage(finalString)
                } else {
                    binding.pbPhotoUpload.hide()
                    showToast("Could not load image")
                }
            } catch (e: Exception) {
                binding.pbPhotoUpload.hide()
                showToast("Failed to process image")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe user profile
        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvProfileName.text = it.name
                binding.tvProfileEmail.text = it.email
                if (it.photo.isNotEmpty() && it.photo.length > 10) {
                    if (it.photo.startsWith("data:image")) {
                        try {
                            val base64String = it.photo.substringAfter("base64,")
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            binding.ivProfileAvatar.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            binding.ivProfileAvatar.setImageBitmap(AvatarGenerator.generateAvatar(it.name))
                        }
                    } else {
                        binding.ivProfileAvatar.load(it.photo) {
                            placeholder(android.R.drawable.sym_def_app_icon)
                            error(android.R.drawable.sym_def_app_icon)
                        }
                    }
                } else {
                    binding.ivProfileAvatar.setImageBitmap(AvatarGenerator.generateAvatar(it.name))
                }
            }
        }

        // Observe image upload
        viewModel.uploadImageResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.pbPhotoUpload.hide()
                if (it.isSuccess) {
                    showToast("Profile Photo Updated!")
                } else {
                    showToast("Failed to upload photo")
                }
                viewModel.resetUploadImageResult()
            }
        }

        binding.fabEditPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Button clicks navigation
        binding.btnNavViewProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_view_profile)
        }

        binding.btnNavEditProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_edit_profile)
        }

        binding.btnNavSkillsOverview.setOnClickListener {
            findNavController().navigate(R.id.nav_skills_overview)
        }

        binding.btnNavAddSkill.setOnClickListener {
            findNavController().navigate(R.id.nav_add_skill)
        }

        binding.btnNavNotes.setOnClickListener {
            findNavController().navigate(R.id.nav_notes)
        }

        binding.btnNavSettings.setOnClickListener {
            findNavController().navigate(R.id.nav_settings)
        }

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            showToast("Logged Out Successfully!")
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
