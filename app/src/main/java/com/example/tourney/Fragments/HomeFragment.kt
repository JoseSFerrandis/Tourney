package com.example.tourney.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tournamentapp.adapters.TournamentAdapter
import com.example.tournamentapp.models.Tournament
import com.example.tourney.R

class HomeFragment : Fragment() {

    private lateinit var btnProfile: FrameLayout
    private lateinit var etSearch: EditText
    private lateinit var btnCreateTournament: AppCompatButton
    private lateinit var rvTournaments: RecyclerView
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
            prize = "$5,000"
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
            prize = "$2,000"
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
            prize = "$1,500"
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
            prize = "$3,000"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews(view: View) {
        btnProfile = view.findViewById(R.id.btn_profile)
        etSearch = view.findViewById(R.id.et_search)
        btnCreateTournament = view.findViewById(R.id.btn_create_tournament)
        rvTournaments = view.findViewById(R.id.rv_tournaments)
    }

    private fun setupRecyclerView() {
        tournamentAdapter = TournamentAdapter(tournaments) { tournament ->
            onTournamentClick(tournament)
        }
        
        rvTournaments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = tournamentAdapter
        }
    }

    private fun setupListeners() {
        btnProfile.setOnClickListener {
            // Navigate to profile screen
            // TODO: Implement navigation
        }

        btnCreateTournament.setOnClickListener {
            // Navigate to create tournament screen
            // TODO: Implement navigation
        }
    }

    private fun onTournamentClick(tournament: Tournament) {
        // Navigate to tournament detail screen
        // TODO: Implement navigation
    }
}
