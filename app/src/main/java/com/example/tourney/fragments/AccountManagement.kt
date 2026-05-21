package com.example.tourney.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.example.tourney.tools.UsersDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

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
        
        refreshData()

        binding.btnPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_ProfileChooseFragment)
        }

        binding.btnEditProfile.setOnClickListener { showEditAccountDialog() }
        binding.btnChangePassword.setOnClickListener { showInsertPasswordDialog() }
        binding.btnEditThemes.setOnClickListener { showThemeSelectorCustom() }

        // Navegación a Términos y Privacidad
        binding.btnViewTerms.setOnClickListener {
            findNavController().navigate(R.id.privacyFragmentDest)
        }

        binding.btnLogout.setOnClickListener {
            User.actualUser = null
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }
    }

    private fun showEditAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_account, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val editEmail = dialogView.findViewById<TextInputEditText>(R.id.etEditEmail)
        val editNickname = dialogView.findViewById<TextInputEditText>(R.id.etEditNickname)
        editEmail.setText(User.actualUser?.email)
        editNickname.setText(User.actualUser?.nickname)

        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveAccountChanges)
        val btnChangePassword = dialogView.findViewById<Button>(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener { showInsertPasswordDialog() }

        btnSave.setOnClickListener {
            val email = editEmail.text.toString()
            val nickname = editNickname.text.toString()

            if(email.isEmpty() || nickname.isEmpty()){
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userList = UsersDao(requireContext()).getAllUsers()
            val emailExists = userList.any { it.email == email && it.email != User.actualUser?.email }
            val nicknameExists = userList.any { it.nickname == nickname && it.nickname != User.actualUser?.nickname }

            if(emailExists){
                Toast.makeText(requireContext(), "Ese email ya está en uso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(nicknameExists){
                Toast.makeText(requireContext(), "Ese nickname ya está en uso", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            User.actualUser?.email = email
            User.actualUser?.nickname = nickname
            UsersDao(requireContext()).updateUser(User.actualUser!!.id, nickname, email, User.actualUser!!.password)
            refreshData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun refreshData(){
        User.actualUser?.let { user ->
            binding.tvName.text = user.nickname
            binding.tvEmail.text = user.email
            updateProfileImage()
        }
    }

    private fun showInsertPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_current_password, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnAccept.setOnClickListener {
            dialog.dismiss()
            showInsertNewPasswordDialog()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showInsertNewPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_new_password, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        btnAccept.setOnClickListener {
            Toast.makeText(requireContext(), "Contraseña cambiada correctamente", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnCancel)?.setOnClickListener { dialog.dismiss() }
        dialog.show()
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
                binding.ivProfile.setImageResource(resId)
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