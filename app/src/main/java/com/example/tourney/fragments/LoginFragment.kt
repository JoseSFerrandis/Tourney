package com.example.tourney.fragments

import com.example.tourney.entities.User
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.tools.UsersDao
import com.example.tourney.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLoginLogin.setOnClickListener {
            if(login()) {
                findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
            }
        }

        binding.loginPasswordInput.addTextChangedListener {
            binding.tilLoginPassword.error = null
        }
        binding.loginEmailInput.addTextChangedListener {
            binding.tilLoginEmail.error = null
        }

        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RegisterFragment)
        }
        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_LoginFragment_to_RememberPassword)
        }
        
        binding.btnModoInvitado?.setOnClickListener {
            // Cargamos al usuario invitado desde la BD (ID 3 por defecto)
            val invitado = UsersDao(requireContext()).getUserById(1)
            if (invitado != null) {
                User.actualUser = invitado
                findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
            } else {
                Toast.makeText(requireContext(), "No se pudo cargar el perfil de invitado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun login(): Boolean{
        val allUsers = UsersDao(requireContext()).getAllUsers()
        for(user in allUsers){
            if(binding.loginEmailInput.text.toString() == user.email &&
                binding.loginPasswordInput.text.toString() == user.password){
                User.actualUser = user
                return true
            }
        }
        Snackbar.make(binding.root, "Email o contraseña incorrectos", Snackbar.LENGTH_LONG).show()
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
