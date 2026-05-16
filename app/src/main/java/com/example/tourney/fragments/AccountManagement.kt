package com.example.tourney.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tourney.R
import com.example.tourney.adapters.ThemeAdapter
import com.example.tourney.adapters.ThemeOption
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

        // Cambiar Avatar (Preferencias)
        binding.btnPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_ProfileChooseFragment)
        }

        // Cambiar Color de la App (Editar cuenta)
        binding.btnEditProfile.text = "Personalizar Colores"
        binding.btnEditProfile.setOnClickListener {
            showThemeSelectorCustom()
        }

        binding.btnLogout.setOnClickListener {
            User.actualUser = null
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }
    }

    private fun showThemeSelectorCustom() {
        val themeOptions = listOf(
            ThemeOption("Azul(Default)", "Blue", Color.parseColor("#1e4fd9")),
            ThemeOption("Lila", "Purple", Color.parseColor("#911ED9")),
            ThemeOption("Gris Claro", "GraySilver", Color.parseColor("#6B7280")),
            ThemeOption("Gris", "GraySlate", Color.parseColor("#475569")),
            ThemeOption("Gris Oscuro", "GrayCharcoal", Color.parseColor("#1F2937")),
            ThemeOption("Cyan", "Cyan", Color.parseColor("#0E7490")),
            ThemeOption("Verde", "SeaGreen", Color.parseColor("#0F766E")),
            ThemeOption("Esmeralda", "Emerald", Color.parseColor("#047857")),
            ThemeOption("Naranja ", "Sunset", Color.parseColor("#B45309")),
            ThemeOption("Rojo", "Crimson", Color.parseColor("#991B1B")),
            ThemeOption("Rosa", "Sakura", Color.parseColor("#BE185D")),
            ThemeOption("Medianoche", "Midnight", Color.parseColor("#1e1b4b"))
        )

        val dialogView = layoutInflater.inflate(R.layout.dialog_theme_selector, null)
        val rvThemes = dialogView.findViewById<RecyclerView>(R.id.rvThemes)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        rvThemes.layoutManager = LinearLayoutManager(requireContext())
        rvThemes.adapter = ThemeAdapter(themeOptions) { selectedOption ->
            val prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("user_theme", selectedOption.key).apply()
            
            dialog.dismiss()
            requireActivity().recreate()
        }

        dialog.show()
    }

    private fun updateProfileImage() {
        val pn = User.actualUser?.photo ?: 0
        if (pn > 0) {
            val resId = resources.getIdentifier("ic_user_pfp$pn", "drawable", requireContext().packageName)
            if (resId != 0) {
                // Usamos findViewById directamente por seguridad si hay conflicto de tipos
                val iv = binding.root.findViewById<ImageView>(R.id.ivProfile)
                iv?.setImageResource(resId)
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
            }
        } else {
            binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}