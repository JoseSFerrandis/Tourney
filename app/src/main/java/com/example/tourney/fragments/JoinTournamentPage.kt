package com.example.tourney.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentJoinTournamentPageBinding

class JoinTournamentPage : Fragment(R.layout.fragment_join_tournament_page) {

    private var _binding: FragmentJoinTournamentPageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJoinTournamentPageBinding.bind(view)

        binding.btnAcceptJoin.setOnClickListener {
            val code = binding.etTournamentCode.text.toString()
            if (code.isNotEmpty()) {
                Toast.makeText(requireContext(), "Código aceptado: $code", Toast.LENGTH_SHORT).show()
                // Navegación a la página del torneo
                findNavController().navigate(R.id.action_JoinTournamentFragment_to_TournamentFragment)
            } else {
                binding.tilTournamentCode.error = "Por favor, introduce un código"
            }
        }

        binding.btnViewParticipants.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.btnAyuda.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}