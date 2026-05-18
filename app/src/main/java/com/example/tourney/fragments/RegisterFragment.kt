package com.example.tourney.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tourney.tools.UsersDao
import com.example.tourney.databinding.FragmentRegisterBinding
import com.example.tourney.models.NewUserModel
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.Security
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.net.SocketTimeoutException

// TODO: humanizar código
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // ── ViewModel (opcional – conecta con tu capa de datos) ──────────────────
    // private val viewModel: AuthViewModel by viewModels()

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

        setupClickListeners()
    }


    private fun setupClickListeners() {

        // Botón principal: Crear cuenta
        binding.btnRegister.setOnClickListener {
            if (validateFields()) {
                registerUser()
            }
        }

        // Ir a login
        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        val nickname = binding.etNickname.text?.toString()?.trim() ?: ""
        val email    = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val confirm  = binding.etPasswordConfirm.text?.toString() ?: ""


        clearError(binding.tilNickname)

        // Nickname
        when {
            nickname.isEmpty() -> {
                setError(binding.tilNickname, "El nickname es obligatorio")
                isValid = false
            }
            nickname.length < 3 -> {
                setError(binding.tilNickname, "Mínimo 3 caracteres")
                isValid = false
            }
            !nickname.matches(Regex("^[a-zA-Z0-9_]+\$")) -> {
                setError(binding.tilNickname, "Solo letras, números y guiones bajos")
                isValid = false
            }
        }

        // Email
        clearError(binding.tilEmail)
        when {
            email.isEmpty() -> {
                setError(binding.tilEmail, "El email es obligatorio")
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setError(binding.tilEmail, "Email no válido")
                isValid = false
            }
        }

        // Contraseña
        clearError(binding.tilPassword)
        when {
            password.isEmpty() -> {
                setError(binding.tilPassword, "La contraseña es obligatoria")
                isValid = false
            }
            password.length < 8 -> {
                setError(binding.tilPassword, "Mínimo 8 caracteres")
                isValid = false
            }
        }

        // Confirmar contraseña
        clearError(binding.tilPasswordConfirm)
        when {
            confirm.isEmpty() -> {
                setError(binding.tilPasswordConfirm, "Confirma tu contraseña")
                isValid = false
            }
            confirm != password -> {
                setError(binding.tilPasswordConfirm, "Las contraseñas no coinciden")
                isValid = false
            }
        }

        return isValid
    }

    private fun setError(til: TextInputLayout, message: String) {
        til.error = message
        til.isErrorEnabled = true
    }

    private fun clearError(til: TextInputLayout) {
        til.error = null
        til.isErrorEnabled = false
    }

    private fun registerUser() {
        val nickname = binding.etNickname.text.toString().trim()
        val email    = binding.etEmail.text.toString().trim()
        // Encripta la contraseña
        val password = binding.etPassword.text.toString()


        // Mostrar loading
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creando cuenta..."

        UserRepository.getInstance(UsersDao(requireContext()), APIService.getInstance()).insertNewUser(
            NewUserModel(
                nickname = nickname,
                email = email,
                password = password,
                photo = 1
            ),
            { succeed ->
                if(succeed){
                    Toast.makeText(context, "Cuenta creada", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }else{
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Crear cuenta"

                    setError(binding.tilEmail, "Ya existe una cuenta con ese email")
                }
            },
            {
                exception ->
                when(exception){
                    is TimeoutCancellationException -> Snackbar.make(requireView(), "No se pudo establecer conexión con el servidor. Vuelve a intentarlo", Snackbar.LENGTH_SHORT).show()
                    is HttpException -> Toast.makeText(context, "Ya existe una cuenta con ese email", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(context, "Error al crear cuenta, vuelve a intentarlo", Toast.LENGTH_SHORT).show()
                }
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Crear cuenta"
            }
        )

    }

    fun navigateToLogin(){
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
