package com.skillsync.app.ui.users

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skillsync.app.R
import com.skillsync.app.data.model.User
import com.skillsync.app.databinding.FragmentSearchUsersBinding
import com.skillsync.app.ui.adapter.UserAdapter
import com.skillsync.app.util.hide
import com.skillsync.app.util.show
import com.skillsync.app.util.showToast

class SearchUsersFragment : Fragment() {

    private var _binding: FragmentSearchUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UsersViewModel by viewModels()
    private lateinit var adapter: UserAdapter
    private var fullList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        adapter = UserAdapter(
            onFollowClick = { user ->
                viewModel.toggleFollowUser(user)
            },
            onViewProfileClick = { user ->
                val bundle = Bundle().apply {
                    putString("userId", user.uid)
                }
                findNavController().navigate(R.id.nav_user_profile, bundle)
            }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter

        // Cache all users
        viewModel.allUsers.observe(viewLifecycleOwner) { list ->
            fullList = list
            performSearch(binding.etSearchQuery.text.toString().trim())
        }

        // Live text query filtering
        binding.etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    showToast("Follow Status Updated!")
                } else {
                    showToast("Error: ${it.exceptionOrNull()?.message}")
                }
                viewModel.resetActionResult()
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            binding.tvSearchPlaceholder.show()
            binding.tvSearchPlaceholder.text = "Type name, email, or skill to search..."
            binding.rvSearchResults.hide()
            return
        }

        val filtered = fullList.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.teach.contains(query, ignoreCase = true) ||
            it.learn.contains(query, ignoreCase = true)
        }

        if (filtered.isEmpty()) {
            binding.tvSearchPlaceholder.show()
            binding.tvSearchPlaceholder.text = "No matching users found."
            binding.rvSearchResults.hide()
        } else {
            binding.tvSearchPlaceholder.hide()
            binding.rvSearchResults.show()
            adapter.submitList(filtered)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
