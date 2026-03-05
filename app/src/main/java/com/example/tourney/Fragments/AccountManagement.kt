package com.example.tourney.Fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentAccountManagementBinding

class AccountManagement : Fragment() {

    private var _binding: FragmentAccountManagementBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountManagementBinding.bind(view)

        // Cargar datos del usuario logeado desde SQLite
//        val userDao = UserDao(requireContext())
//        val user = userDao.getLoggedUser()

//        if (user != null) {
//            val nombreCompleto = "${user["nombre"]} ${user["apellidos"]}"
        binding.tvName.text = "Enrique"
        binding.tvEmail.text = "EnriqueLaura@edu.gva.es"
        // Si tienes más campos en el layout de perfil, puedes rellenarlos aquí
        // Ejemplo: binding.tvUserRole.text = user["rol"]


        binding.btnLogout.setOnClickListener {
            // Marcamos al usuario como deslogeado en la DB
            //userDao.logout()

            // Volvemos al Login
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Editar perfil", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}