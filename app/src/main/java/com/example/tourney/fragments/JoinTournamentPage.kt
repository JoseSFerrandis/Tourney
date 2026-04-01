package com.example.tourney.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.databinding.FragmentJoinTournamentPageBinding
import com.example.tourney.entities.Tournament

class JoinTournamentPage : Fragment(R.layout.fragment_join_tournament_page) {

    private var _binding: FragmentJoinTournamentPageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentJoinTournamentPageBinding.bind(view)

        binding.btnAcceptJoin.setOnClickListener {
            val code = binding.etTournamentCode.text.toString()
            var foundTournament: Tournament?
            if (code.isNotBlank()) {
                try{
                    foundTournament = Tournament.searchTournamentByCode(code.toInt())
                } catch (e: NumberFormatException){
                    binding.tilTournamentCode.error = "Por favor, introduce un código válido"
                    return@setOnClickListener
                }
                if (foundTournament == null) {
                    binding.tilTournamentCode.error = "Código no encontrado"
                    return@setOnClickListener
                }else{
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
                    return@setOnClickListener
                }
            } else {
                binding.tilTournamentCode.error = "Por favor, introduce un código"
            }
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