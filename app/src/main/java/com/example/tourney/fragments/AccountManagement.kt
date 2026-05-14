package com.example.tourney.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentAccountManagementBinding
import com.example.tourney.entities.User
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AccountManagement : Fragment() {

    private var _binding: FragmentAccountManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Cargar datos del usuario actual
        User.actualUser?.let { user ->
            binding.tvName.text = user.nickname
            binding.tvEmail.text = user.email
            updateProfileImage()
        }

        // Navegación a Editar Cuenta
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_EditAccount)
        }

        // Navegación al selector de avatar (Preferencias)
        binding.btnPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_ProfileChooseFragment)
        }

        // Acción para cambiar contraseña
        binding.btnChangePassword.setOnClickListener {
            // TODO: Implementar navegación a un fragmento de cambio de contraseña
            // O podrías navegar a RememberPassword si quieres que el flujo sea similar:
            // findNavController().navigate(R.id.RememberPassword) 
            // (necesitarías añadir la acción en el nav_graph)
            Toast.makeText(requireContext(), "Funcionalidad de cambiar contraseña próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            User.actualUser = null
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }
    }

    private fun updateProfileImage() {
        val pn = User.actualUser?.photo ?: 0
        if (pn > 0) {
            val resId = resources.getIdentifier("ic_user_pfp$pn", "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProfile.setImageResource(resId)
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
            }
        } else {
            binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
        }
    }

    private fun showCustomHomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_current_password, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Ajustar fondo transparente para que se vean los bordes redondeados del CardView
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        btnCreate.setOnClickListener {
            navController.navigate(R.id.action_HomeFragment_to_CreateTournamentFragment)
            dialog.dismiss()
        }

        btnJoin.setOnClickListener {
            navController.navigate(R.id.action_HomeFragment_to_JoinTournamentFragment)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}