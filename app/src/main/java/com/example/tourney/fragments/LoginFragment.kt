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
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.TimeoutCancellationException
import java.net.SocketTimeoutException

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
            binding.btnLoginLogin.isEnabled = false
            binding.btnLoginLogin.text = "Iniciando sesión..."

            UserRepository.getInstance(UsersDao(requireContext()), APIService.getInstance())
                .loginUser(binding.loginEmailInput.text.toString(),
                    binding.loginPasswordInput.text.toString(),
                    requireContext(),
                    {
                        findNavController().navigate(R.id.action_LoginFragment_to_HomeFragment)
                    },
                    {
                        exception ->
                        when(exception){
                            is TimeoutCancellationException -> Snackbar.make(requireView(), "No se pudo establecer conexión con el servidor. Vuelve a intentarlo", Snackbar.LENGTH_SHORT).show()
                            else -> Toast.makeText(context, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                        }
                        binding.btnLoginLogin.isEnabled = true
                        binding.btnLoginLogin.text = "Iniciar sesión"
                    })
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
}
