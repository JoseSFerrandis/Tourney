package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentAccountManagementBinding
import com.example.tourney.entities.User

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
            updateProfileImage() // Usamos la función centralizada para la imagen
        }

        // Navegación al selector de avatar
        binding.btnPreferences.text = "Cambiar Avatar"
        binding.btnPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_ProfileChooseFragment)
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_to_EditAccount)
        }

        binding.btnLogout.setOnClickListener {
            User.actualUser = null
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }
    }

    private fun updateProfileImage() {
        val pn = User.actualUser?.photo ?: 0

        // Si el número es válido (del 1 al 18)
        if (pn > 0) {
            val resId = resources.getIdentifier("ic_user_pfp$pn", "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProfile.setImageResource(resId)
            } else {
                // Si getIdentifier falla por alguna razón, usamos la 1 por defecto
                binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
            }
        } else {
            // Imagen por defecto si photo es 0 o null
            binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}