package com.example.tourney.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tournamentapp.models.Tournament
import com.example.tourney.databinding.FragmentTournamentPageBinding

class TournamentPage : Fragment() {

    private var _binding: FragmentTournamentPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos la vista correctamente usando ViewBinding
        _binding = FragmentTournamentPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Recibimos el objeto Tournament de forma segura
            val tournament = arguments?.getParcelable<Tournament>("tournament_data")

            if (tournament != null) {
                setupUI(tournament)
            } else {
                Log.e("TournamentPage", "No se recibieron datos del torneo")
                Toast.makeText(requireContext(), "Error al cargar datos del torneo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("TournamentPage", "Error procesando argumentos: ${e.message}")
            Toast.makeText(requireContext(), "Error crítico al cargar la página", Toast.LENGTH_SHORT).show()
        }

        setupClickListeners()
    }

    private fun setupUI(tournament: Tournament) {
        // Rellenamos la UI con los datos recibidos
        binding.tvTournamentTitle.text = tournament.name
        
        // Aquí podrías añadir más campos si tu XML los tuviera (ej. fecha, premios, etc.)
        // Por ahora el XML solo tiene el título y botones
    }

    private fun setupClickListeners() {
        binding.btnJoin.setOnClickListener {
            Toast.makeText(requireContext(), "Cargando Clasificación...", Toast.LENGTH_SHORT).show()
        }

        binding.btnReject.setOnClickListener {
            Toast.makeText(requireContext(), "Cargando Emparejamientos...", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewParticipants.setOnClickListener {
            Toast.makeText(requireContext(), "Mostrando Participantes...", Toast.LENGTH_SHORT).show()
        }

        binding.btnRules.setOnClickListener {
            Toast.makeText(requireContext(), "Mostrando Reglas...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
