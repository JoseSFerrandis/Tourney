package com.example.incidenciasdam

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar



/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        binding.btnLoginLogin.setOnClickListener {
            if(loginByButton()) {
                Snackbar.make(view, "Hola Holita", Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_LoginFragment_to_DashboardFragment)
            }
        }


        binding.loginPasswordInput.addTextChangedListener {
            binding.tilLoginPassword.error = null
        }
        binding.loginEmailInput.addTextChangedListener {
            binding.tilLoginEmail.error = null
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
            }
    }

    fun loginByButton() : Boolean {

        val regexPassword = Regex("(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}")

        // Error en el email
        if(!(binding.loginEmailInput.text.toString().contains("@")) ||
            !(binding.loginEmailInput.text.toString().contains("."))){
            binding.tilLoginEmail.error = "Introduce un email válido"
            return false
        }else{
            binding.tilLoginEmail.error = null
        }

        // Error en la contraseña
        binding.loginPasswordInput.text?.let {
            // Contraseña demasiado corta
            if(it.length < 8 && it.length > 1)
                binding.tilLoginPassword.error = "Debe tener al menos 8 caracteres"

            // Contraseña sin mayúsculas
            else if(!it.contains(Regex("[A-Z]")))
                binding.tilLoginPassword.error = "Debe tener al menos una mayúscula"

            // Contraseña sin números
            else if(!it.contains(Regex("\\d")))
                binding.tilLoginPassword.error = "Debe tener al menos un número"

            // Contraseña válida
            else {
                binding.tilLoginPassword.error = null
            }

        }

        binding.loginEmailInput.clearFocus()
        binding.loginPasswordInput.clearFocus()

        return ((binding.loginEmailInput.text.toString() == "prueba@prueba.com") &&
                (binding.loginPasswordInput.text.toString().matches(regexPassword)))
    }
}
