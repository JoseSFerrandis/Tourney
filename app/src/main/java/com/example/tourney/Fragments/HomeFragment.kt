package com.example.tourney.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tournamentapp.adapters.TournamentAdapter
import com.example.tournamentapp.models.Tournament
import com.example.tourney.R
import com.example.tourney.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var tournamentAdapter: TournamentAdapter

    // Sample tournament data
    private val tournaments = listOf(
        Tournament(
            id = 1,
            name = "Copa League of Legends 2026",
            game = "League of Legends",
            participants = 32,
            maxParticipants = 32,
            date = "25 Ene 2026",
            location = "KOI",
            status = "Inscripciones Abiertas",
            prize = "$5,000",
            code =  777
        ),
        Tournament(
            id = 2,
            name = "Torneo Counter-Strike Relámpago",
            game = "CS:GO",
            participants = 6,
            maxParticipants = 8,
            date = "18 Ene 2026",
            location = "Cybercafé Central",
            status = "Inscripciones Abiertas",
            prize = "$2,000",
            code =  69
        ),
        Tournament(
            id = 3,
            name = "Championship Dungeons & Dragons",
            game = "D&D 5e",
            participants = 12,
            maxParticipants = 12,
            date = "20 Ene 2026",
            location = "Tienda Gaming Local",
            status = "En Progreso",
            prize = "$1,500",
            code =  1
        ),
        Tournament(
            id = 4,
            name = "Torneo Valorant Summer",
            game = "Valorant",
            participants = 20,
            maxParticipants = 32,
            date = "28 Ene 2026",
            location = "Online/Presencial",
            status = "Inscripciones Abiertas",
            prize = "$3,000",
            code =  1000
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        binding.rvTournaments.layoutManager = LinearLayoutManager(context)
        
        tournamentAdapter = TournamentAdapter(tournaments) { tournament ->
            onTournamentClick(tournament)
        }
        
        binding.rvTournaments.adapter = tournamentAdapter
    }

    private fun setupListeners() {
        binding.btnProfile.setOnClickListener {
             findNavController().navigate(R.id.action_DashboardFragment_to_ProfileFragment2)
        }

        binding.btnCreateTournament.setOnClickListener {
            Toast.makeText(requireContext(), "Próximamente: Crear Torneo", Toast.LENGTH_SHORT).show()
        }

        binding.btnJoinTournament.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_DashboardFragment_to_JoinTournamentFragment2)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en navegación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onTournamentClick(tournament: Tournament) {
        // Creamos el Bundle y pasamos el objeto Parcelable
        val bundle = Bundle().apply {
            putParcelable("tournament_data", tournament)
        }
        
        // Navegamos pasando el bundle (Asegúrate que esta acción existe en nav_graph)
        try {
            findNavController().navigate(R.id.action_DashboardFragment_to_TournamentFragment2, bundle)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Acción de navegación no encontrada", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
