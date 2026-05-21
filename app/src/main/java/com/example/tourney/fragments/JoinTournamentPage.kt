package com.example.tourney.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.tourney.R
import com.example.tourney.databinding.FragmentJoinTournamentPageBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.repositories.TournamentRepository

class JoinTournamentPage : Fragment(R.layout.fragment_join_tournament_page) {

    private var _binding: FragmentJoinTournamentPageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJoinTournamentPageBinding.bind(view)

        binding.btnAcceptJoin.setOnClickListener {
            val code = binding.etTournamentCode.text.toString()
            if (code.isBlank()) {
                binding.tilTournamentCode.error = "Por favor, introduce un código"
                return@setOnClickListener
            }

            val codeNumber = try {
                code.toInt()
            } catch (e: NumberFormatException) {
                binding.tilTournamentCode.error = "Por favor, introduce un código válido"
                return@setOnClickListener
            }

            TournamentRepository.getInstance(requireContext()).searchTournamentByCode(
                codeNumber,
                requireContext(),
                onSuccess = { foundTournament -> openTournament(foundTournament, code) },
                onNotFound = { binding.tilTournamentCode.error = "Código no encontrado" },
                onError = { error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show() }
            )
        }

        binding.btnAyuda.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTournament(foundTournament: Tournament, code: String) {
        binding.tilTournamentCode.error = null
        Toast.makeText(requireContext(), "Código aceptado: $code", Toast.LENGTH_SHORT).show()

        val bundle = Bundle().apply {
            putParcelable("tournament_data", foundTournament)
        }
        val navigationOptions = navOptions {
            popUpTo(R.id.JoinTournamentFragment) {
                inclusive = true
            }
        }
        findNavController().navigate(R.id.action_JoinTournamentFragment_to_TournamentFragment, bundle, navigationOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
