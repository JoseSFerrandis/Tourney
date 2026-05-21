package com.example.tourney.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.tourney.databinding.FragmentRegisterBinding
import com.example.tourney.tools.UsersDao
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val usersDao by lazy { UsersDao(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupValidationListeners()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateFields()) {
                performRegistration()
            }
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    /**
     * "Humaniza" la experiencia: limpia los errores en cuanto el usuario empieza a escribir.
     */
    private fun setupValidationListeners() {
        binding.etNickname.doOnTextChanged { _, _, _, _ -> clearError(binding.tilNickname) }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> clearError(binding.tilEmail) }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> clearError(binding.tilPassword) }
        binding.etPasswordConfirm.doOnTextChanged { _, _, _, _ -> clearError(binding.tilPasswordConfirm) }
    }

    private fun validateFields(): Boolean {
        val nickname = binding.etNickname.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val confirm = binding.etPasswordConfirm.text?.toString() ?: ""

        var isValid = true

        // 1. Validar Nickname
        when {
            nickname.isEmpty() -> {
                setError(binding.tilNickname, "¡No olvides tu nombre de guerrero!")
                isValid = false
            }
            nickname.length < 3 -> {
                setError(binding.tilNickname, "El nombre es un poco corto (mín. 3)")
                isValid = false
            }
            !nickname.matches(Regex("^[a-zA-Z0-9_]+\$")) -> {
                setError(binding.tilNickname, "Usa solo letras, números o guiones bajos")
                isValid = false
            }
            usersDao.getAllUsers().any { it.nickname.equals(nickname, true) } -> {
                setError(binding.tilNickname, "Este nickname ya está en batalla. Elige otro.")
                isValid = false
            }
        }

        // 2. Validar Email
        when {
            email.isEmpty() -> {
                setError(binding.tilEmail, "Necesitamos un email para contactarte")
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setError(binding.tilEmail, "Ese email no parece correcto")
                isValid = false
            }
            usersDao.getAllUsers().any { it.email.equals(email, true) } -> {
                setError(binding.tilEmail, "Este email ya tiene una cuenta activa")
                isValid = false
            }
        }

        // 3. Validar Contraseña
        when {
            password.isEmpty() -> {
                setError(binding.tilPassword, "La seguridad es lo primero. Pon una clave.")
                isValid = false
            }
            password.length < 8 -> {
                setError(binding.tilPassword, "La contraseña debe tener al menos 8 caracteres")
                isValid = false
            }
        }

        // 4. Confirmar Contraseña
        if (confirm != password) {
            setError(binding.tilPasswordConfirm, "Las contraseñas no coinciden")
            isValid = false
        }

        return isValid
    }

    private fun performRegistration() {
        val nickname = binding.etNickname.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Estado visual de carga
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Preparando tu arena..."

        try {
            usersDao.insertNewUser(nickname, email, password)
            Toast.makeText(requireContext(), "¡Bienvenido a Tourney, $nickname!", Toast.LENGTH_LONG).show()
            navigateToLogin()
        } catch (e: Exception) {
            binding.btnRegister.isEnabled = true
            binding.btnRegister.text = "CREAR CUENTA"
            Toast.makeText(requireContext(), "Error al crear la cuenta. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setError(til: TextInputLayout, message: String) {
        til.error = message
        til.isErrorEnabled = true
    }

    private fun clearError(til: TextInputLayout) {
        if (til.error != null) {
            til.error = null
            til.isErrorEnabled = false
        }
    }

    private fun navigateToLogin() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
