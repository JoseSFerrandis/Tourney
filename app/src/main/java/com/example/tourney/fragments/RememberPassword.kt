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
import com.google.android.material.snackbar.Snackbar

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

        binding.btnRememberPassword.setOnClickListener {
            if(checkCredentials()) {
                findNavController().navigate(R.id.action_RememberPassword_to_SetNewPassword)
            }
        }

    }


    fun checkCredentials(): Boolean{

        val allUsers = UsersDao(requireContext()).getAllUsers()

        for(user in allUsers){
            if(binding.rememberEmailInput.text.toString() == user.email &&
                binding.rememberNicknameInput.text.toString() == user.nickname){
                User.actualUser = user;
                return true
            }
        }
        Snackbar.make(binding.root, "No se ha contrado ningún usuario con esos datos", Snackbar.LENGTH_LONG).show()
        return false
    }
}