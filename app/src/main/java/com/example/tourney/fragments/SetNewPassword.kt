package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.tools.UsersDao
import com.example.tourney.databinding.FragmentSetNewPasswordBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.Security
import com.google.android.material.snackbar.Snackbar

class SetNewPassword : Fragment() {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private var _binding: FragmentSetNewPasswordBinding? = null
    private val binding get() = _binding!!
    private var email: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_new_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSetNewPasswordBinding.bind(view)

        email = arguments?.getString("email")

        binding.btnCreateNewPassword.setOnClickListener {
            createNewPassword()
            binding.btnCreateNewPassword.isEnabled = true
        }

    }


    fun createNewPassword(){
        binding.newPasswordInput.text?.let {
            // Contraseña demasiado corta
            if(it.length < 8 && it.length > 1)
                binding.tilNewPassword.error = "Debe tener al menos 8 caracteres"

            // Contraseña sin mayúsculas
            else if(!it.contains(Regex("[A-Z]")))
                binding.tilNewPassword.error = "Debe tener al menos una mayúscula"

            // Contraseña sin números
            else if(!it.contains(Regex("\\d")))
                binding.tilNewPassword.error = "Debe tener al menos un número"

            // Contraseña válida
            else {
                binding.tilNewPassword.error = null
            }
        }
        // Contraseña no coincide
        if(binding.repeatPasswordInput.text.toString() != binding.newPasswordInput.text.toString())
            binding.tilRepeatPassword.error = "Las contraseñas no coinciden"

            // Contraseña válida
            else {
                binding.tilRepeatPassword.error = null
            }


        binding.newPasswordInput.clearFocus()
        binding.repeatPasswordInput.clearFocus()


        if(binding.tilNewPassword.error != null ||
            binding.tilRepeatPassword.error != null){ return }

        val userEmail = email ?: return
        if( binding.tilNewPassword.error == null && binding.tilRepeatPassword.error == null ){
            sendNewPassword(userEmail, binding.newPasswordInput.text.toString())
            binding.btnCreateNewPassword.isEnabled = false
        }
    }

    private fun sendNewPassword(email: String, password: String){
        UserRepository(UsersDao(requireContext()), APIService.getInstance())
            .updatePassword(email, password,
                {
                    if (it) {
                        findNavController().navigate(R.id.action_SetNewPassword_to_LoginFragment)
                        Snackbar.make(binding.root, "Contraseña actualizada", Snackbar.LENGTH_LONG).show()
                    }
                    else { Snackbar.make(binding.root, "No se ha podido actualizar la contraseña", Snackbar.LENGTH_LONG).show() }
                },
                {
                    Snackbar.make(binding.root, "No se pudo establecer conexión con el servidor. Vuelve a intentarlo", Snackbar.LENGTH_SHORT).show()
                })
    }
}
