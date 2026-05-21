package com.example.tourney.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.tourney.databinding.FragmentRegisterBinding
import com.example.tourney.models.NewUserModel
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.CheckValues
import com.example.tourney.tools.Security
import com.example.tourney.tools.UsersDao
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.TimeoutCancellationException
import retrofit2.HttpException
import java.net.SocketTimeoutException

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

        // Ajuste para que el teclado no tape el contenido
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val keyboardInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(0, 0, 0, keyboardInsets.bottom)
            insets
        }

        setupValidationListeners()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateFields()) {
                registerUser()
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
        }

        // 3. Validar Contraseña
        val passwordError = CheckValues.checkPassword(password)
        when (passwordError) {
            7 -> {
                setError(binding.tilPassword, "La seguridad es lo primero. Pon una clave.")
                isValid = false
            }
            1 -> {
                setError(binding.tilPassword, "La contraseña debe tener al menos 8 caracteres")
                isValid = false
            }
            2 -> {
                setError(binding.tilPassword, "Debe tener al menos una mayúscula")
                isValid = false
            }
            3 -> {
                setError(binding.tilPassword, "Debe tener al menos un número")
                isValid = false
            }
            4 -> {
                setError(binding.tilPassword, "Debe tener al menos una minúscula")
                isValid = false
            }
            /*
            5 -> {
                setError(binding.tilPassword, "Debe tener al menos un carácter especial")
                isValid = false
            }*/
            6 -> {
                setError(binding.tilPassword, "No puede contener espacios")
                isValid = false
            }
        }

        if(confirm != password){
            setError(binding.tilPasswordConfirm, "Las contraseñas no coinciden")
            isValid = false
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

    private fun navigateToLogin() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
