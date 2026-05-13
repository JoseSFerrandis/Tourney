package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.tools.UsersDao
import com.example.tourney.databinding.FragmentRememberPasswordBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RememberPassword : Fragment() {
    private var _binding: FragmentRememberPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_remember_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRememberPasswordBinding.bind(view)

        binding.btnRememberPassword.setOnClickListener { checkCredentials() }

    }


    fun checkCredentials(){
        val email = binding.rememberEmailInput.text.toString()
        val nickname = binding.rememberNicknameInput.text.toString()

        UserRepository(UsersDao(requireContext()), APIService.getInstance())
            .rememberPassword(
                email,
                nickname,
                {
                    val bundle = Bundle()
                    bundle.putString("email", email)
                    findNavController().navigate(R.id.action_RememberPassword_to_SetNewPassword, bundle)
                },
                {
                    exception ->
                    Snackbar.make(binding.root, exception.message.toString(), Snackbar.LENGTH_SHORT).show()
                }
            )
    }
}
