package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.databinding.FragmentEditAccountBinding
import com.example.tourney.entities.User

class EditAccountFragment : Fragment() {

    private var _binding: FragmentEditAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Rellenar campos con la información actual del usuario
        User.actualUser?.let { user ->
            binding.etEditNickname.setText(user.nickname)
            binding.etEditEmail.setText(user.email)
        }

        binding.btnChangePasswordEdit.setOnClickListener {
            // TODO: Implementar navegación a un fragmento de cambio de contraseña
            // O podrías navegar a RememberPassword si quieres que el flujo sea similar:
        }

        binding.btnSaveAccountChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val newNickname = binding.etEditNickname.text.toString()
        val newEmail = binding.etEditEmail.text.toString()

        if (newNickname.isBlank() || newEmail.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar el usuario actual en memoria (local)
        User.actualUser?.let {
            it.nickname = newNickname
            it.email = newEmail
            
            // TODO: Implementar la llamada a la API y DAO para persistir los cambios
            Toast.makeText(requireContext(), "Cambios guardados correctamente", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}