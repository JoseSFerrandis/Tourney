package com.example.tourney.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourney.adapters.TournamentAdapter
import com.example.tourney.entities.Tournament
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.databinding.FragmentHomeBinding
import com.example.tourney.entities.User
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var tournamentAdapter: TournamentAdapter

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
        /*
           @joschajov 22/03/2025
           He tenido que cambiar esta parte ya que teniamos un array de objetos y no una lista editable
           fue un fallo por mi parte hacerlo así si no me equivoco, ahora ya es editable y funciona

           A futuro: esta función donde los recogemos se tendrá que editar un poco seguramente ya que
           sacaremos los datos de una llamada a la base de datos
         */

        // Convertimos a MutableList para que el adaptador pueda manejarla
        val tournaments = Tournament.getTournaments().toMutableList()
        tournamentAdapter = TournamentAdapter(tournaments) { tournament -> onTournamentClick(tournament) }
        binding.rvTournaments.adapter = tournamentAdapter


        binding.etSearch.addTextChangedListener { text ->
            /*val filteredTournaments = tournaments.filter { tournament ->
                tournament.name.contains(text.toString(), ignoreCase = true)
            }
            tournamentAdapter.updateTournaments(filteredTournaments)*/
            tournamentAdapter.filterTournaments(text.toString())
        }
    }

    private fun setupListeners() {
        binding.btnProfile.setOnClickListener {
             findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
        }

        binding.btnJoinTournament.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_HomeFragment_to_JoinTournamentFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en navegación", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCreateTournament.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_HomeFragment_to_CreateTournamentFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error en navegación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onTournamentClick(tournament: Tournament) {
        val bundle = Bundle().apply {
            putParcelable("tournament_data", tournament)
        }
        
        try {
            findNavController().navigate(R.id.action_HomeFragment_to_TournamentFragment, bundle)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Acción de navegación no encontrada", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvGreeting.text = "Hola, ${User.actualUser?.nickname}"
        /*
            @joschajov 22/03/2025
            He tealizado cambios en esta parte, ahora el objeto adapatador puede actualizar su array
            con una lista que recoge del main

            A futuro: se debe cambiar el meotodo ya que se hará desde una base de datos
         */
        // Forzamos la actualización de los datos del adaptador al volver
        tournamentAdapter.updateTournaments(Tournament.getTournaments())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
