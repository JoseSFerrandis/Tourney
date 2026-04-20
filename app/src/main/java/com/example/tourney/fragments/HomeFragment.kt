package com.example.tourney.fragments

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
import com.example.tourney.R
import com.example.tourney.databinding.FragmentHomeBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
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
        updateProfileImage()
    }

    private fun updateProfileImage() {
        val pn = User.actualUser?.photo ?: 0
        
        // Si el número es válido (del 1 al 18)
        if (pn > 0) {
            val resId = resources.getIdentifier("ic_user_pfp$pn", "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProfile?.setImageResource(resId)
            } else {
                binding.ivProfile?.setImageResource(R.drawable.ic_user_pfp1)
            }
        } else {
            // Imagen por defecto si es 0 o nulo
            binding.ivProfile?.setImageResource(R.drawable.ic_user_pfp1)
        }
    }

    private fun setupRecyclerView() {
        binding.rvTournaments.layoutManager = LinearLayoutManager(context)
        val tournaments = TournamentRepository.getInstance().searchTournamentListByIds(User.actualUser?.showableTournamentList ?: mutableListOf())
        tournamentAdapter = TournamentAdapter(tournaments) { tournament -> onTournamentClick(tournament) }
        binding.rvTournaments.adapter = tournamentAdapter

        binding.etSearch.addTextChangedListener { text ->
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
        updateProfileImage() // Actualizamos la imagen también al volver
        tournamentAdapter.updateTournaments(TournamentRepository.getInstance().searchTournamentListByIds(User.actualUser?.showableTournamentList ?: mutableListOf()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
