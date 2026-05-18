package com.example.tourney.fragments

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
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
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.CheckValues
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
        
        // Cargar datos del usuario actual
        refreshData()

        // Cambiar Avatar (Preferencias)
        binding.btnPreferences.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_ProfileChooseFragment)
        }

        // Cambiar Color de la App (Editar cuenta)
        if(User.actualUser?.logged == false) binding.btnEditProfile.visibility = View.GONE
        binding.btnEditProfile.setOnClickListener { showEditAccountDialog() }

        // Cambiar Contraseña
        if(User.actualUser?.logged == false) binding.btnChangePassword.visibility = View.GONE
        binding.btnChangePassword.setOnClickListener { showInsertPasswordDialog() }

        // Cambiar tema
        binding.btnEditThemes?.setOnClickListener { showThemeSelectorCustom() }

        binding.btnLogout.setOnClickListener {
            User.actualUser = null
            findNavController().navigate(R.id.action_ProfileFragment_to_LoginFragment)
        }
    }

    private fun refreshData(){
        User.actualUser?.let { user ->
            binding.tvName.text = user.nickname
            binding.tvEmail.text = user.email
            updateProfileImage()
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
            if(!CheckValues.checkEmail(email)){
                Toast.makeText(requireContext(), "Por favor, introduce un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                btnChangePassword.isEnabled = false
                btnSave.isEnabled = false
                btnSave.text = "Actualizando..."

                UserRepository.getInstance(UsersDao(requireContext()), APIService.getInstance()).editAccount(
                    email,
                    nickname,
                    requireContext(),
                    {
                        when (it) {
                            0 -> {
                                Toast.makeText(requireContext(), "Cuenta actualizada", Toast.LENGTH_SHORT).show()
                                User.actualUser?.email = email
                                User.actualUser?.nickname = nickname
                                refreshData()
                                dialog.dismiss()
                            }
                            1 -> {
                                Toast.makeText(requireContext(), "Ya existe una cuenta con ese email", Toast.LENGTH_SHORT)
                                    .show()
                                btnSave.isEnabled = true
                                btnSave.text = "Guardar cambios"
                                btnChangePassword.isEnabled = true
                            }
                            else -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Ya existe una cuenta con ese nickname",
                                    Toast.LENGTH_SHORT
                                ).show()
                                btnSave.isEnabled = true
                                btnSave.text = "Guardar cambios"
                                btnChangePassword.isEnabled = true
                            }
                        }
                    },
                    {
                        Toast.makeText(requireContext(), "Error al actualizar cuenta", Toast.LENGTH_SHORT).show()
                        btnSave.isEnabled = true
                        btnSave.text = "Guardar cambios"
                        btnChangePassword.isEnabled = true
                    }
                )
            }
        }

        dialog.show()
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
            btnAccept.isEnabled = false
            btnAccept.text = "Comprobando..."
            // Lógica de validación próximamente
            val Password = dialogView.findViewById<TextInputEditText>(R.id.etCurrentPassword)

            UserRepository.getInstance(UsersDao(requireContext()), APIService.getInstance()).checkPassword(
                Password.text.toString(),
                requireContext(),
                {
                    if(it){
                        dialog.dismiss()
                        showInsertNewPasswordDialog()
                    }else{
                        Toast.makeText(requireContext(), "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        btnAccept.isEnabled = true
                        btnAccept.text = "Cambiar contraseña"
                    }
                },
                {
                    btnAccept.isEnabled = true
                    btnAccept.text = "Cambiar contraseña"
                    Toast.makeText(requireContext(), "Error al comprobar contraseña", Toast.LENGTH_SHORT).show()
                })
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showInsertNewPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_insert_new_password, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<Button>(R.id.btnAccept)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnAccept.setOnClickListener {

            val Password1 = dialogView.findViewById<TextInputEditText>(R.id.etNewPassword)
            val Password2 = dialogView.findViewById<TextInputEditText>(R.id.etRepeatPassword)


            if(Password1.text.toString().length < 8){
                Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Password1.text.toString().matches(Regex(".*[A-Z].*"))){
                Toast.makeText(requireContext(), "La contraseña debe tener al menos una mayúscula", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Password1.text.toString().matches(Regex(".*[0-9].*"))){
                Toast.makeText(requireContext(), "La contraseña debe tener al menos un número", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Password1.text.toString().matches(Regex(".*[a-z].*"))){
                Toast.makeText(requireContext(), "La contraseña debe tener al menos una minúscula", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!Password1.text.toString().matches(Regex(".*[!@#$%^&*()].*"))){
                Toast.makeText(requireContext(), "La contraseña debe tener al menos un carácter especial", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(Password1.text.toString() != Password2.text.toString()){
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                btnAccept.isEnabled = false
                btnAccept.text = "Actualizando..."

                UserRepository.getInstance(UsersDao(requireContext()), APIService.getInstance()).updatePassword(
                    Password2.text.toString(),
                    requireContext(),
                    { updated ->
                        if(updated){
                            Toast.makeText(requireContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }else{
                            btnAccept.isEnabled = true
                            btnAccept.text = "Cambiar contraseña"
                            Toast.makeText(requireContext(), "Error al actualizar contraseña", Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        btnAccept.isEnabled = true
                        btnAccept.text = "Cambiar contraseña"
                        Toast.makeText(requireContext(), "No se pudo establecer conexión con el servidor. Vuelve a intentarlo", Toast.LENGTH_SHORT).show()
                    })
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

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