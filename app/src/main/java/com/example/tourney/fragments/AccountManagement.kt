package com.example.tourney.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.databinding.FragmentAccountManagementBinding

// Añadimos R.layout.fragment_account_management aquí para que el Fragment sepa qué inflar
class AccountManagement : Fragment(R.layout.fragment_account_management) {

    private var _binding: FragmentAccountManagementBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAccountManagementBinding.bind(view)

        // Datos de ejemplo
        binding.tvName.text = "Enrique"
        binding.tvEmail.text = "EnriqueLaura@edu.gva.es"

        binding.btnLogout.setOnClickListener {
            // Volvemos al Login (asegúrate de que este ID existe en tu nav_graph)
            try {
                findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en navegación", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Editar perfil", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvName.text = MainActivity.actualUser?.nickname
        binding.tvEmail.text = MainActivity.actualUser?.email
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
