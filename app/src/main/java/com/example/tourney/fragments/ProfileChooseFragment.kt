package com.example.tourney.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tourney.R
import com.example.tourney.adapters.AvatarAdapter
import com.example.tourney.databinding.FragmentProfileChooseBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.UserRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.UsersDao

class ProfileChooseFragment : Fragment() {

    private var _binding: FragmentProfileChooseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val avatarResources = listOf(
            R.drawable.ic_user_pfp1, R.drawable.ic_user_pfp2, R.drawable.ic_user_pfp3,
            R.drawable.ic_user_pfp4, R.drawable.ic_user_pfp5, R.drawable.ic_user_pfp6,
            R.drawable.ic_user_pfp7, R.drawable.ic_user_pfp8, R.drawable.ic_user_pfp9,
            R.drawable.ic_user_pfp10, R.drawable.ic_user_pfp11, R.drawable.ic_user_pfp12,
            R.drawable.ic_user_pfp13, R.drawable.ic_user_pfp14, R.drawable.ic_user_pfp15,
            R.drawable.ic_user_pfp16, R.drawable.ic_user_pfp17, R.drawable.ic_user_pfp18,
        )

        val adapter = AvatarAdapter(avatarResources) { selectedNumber ->
            Log.d("AvatarAdapter", "Se seleccionó el avatar número: $selectedNumber")
            
            val currentUser = User.actualUser
            if (currentUser != null) {
                // 1. Actualizar en la base de datos (SQLite)
                //val rows = UsersDao(requireContext()).updateAvatar(currentUser.email, selectedNumber)
                UserRepository(UsersDao(requireContext()), APIService.getInstance())
                    .updateAvatar(
                        selectedNumber,
                        requireContext(),
                        {
                            Toast.makeText(requireContext(), "Avatar actualizado", Toast.LENGTH_SHORT).show()
                            // 2. Actualizar el objeto global para que el cambio se vea al instante
                            currentUser.photo = selectedNumber
                            Log.d("IMAGDB", " ${currentUser.nickname}")

                            // 3. Volver al fragmento de perfil
                            findNavController().popBackStack()
                        },
                        {
                            exception ->
                            Toast.makeText(requireContext(), exception.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                        )
            }

        }

        binding.rvAvatars.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvAvatars.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}