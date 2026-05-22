package com.example.tourney.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.tourney.R
import com.example.tourney.adapters.AddPunctuationParticipantAdapter
import com.example.tourney.databinding.FragmentClasificationListBinding
import com.example.tourney.entities.Participant
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.example.tourney.tools.TournamentsDao

class ClasificationListFragment : Fragment() {
    private var _binding: FragmentClasificationListBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null
    private lateinit var adapter: AddPunctuationParticipantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClasificationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ajuste dinámico para el teclado aplicado al RecyclerView
        ViewCompat.setOnApplyWindowInsetsListener(binding.rvParticipants) { v, insets ->
            val keyboardInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, keyboardInsets.bottom)
            insets
        }

        tournament = arguments?.getParcelable<Tournament>("tournament_data")

        refresh()

        binding.nextRound.setOnClickListener {
            if(tournament?.tournamentStatus == TournamentStatus.EDITABLE){
                tournament?.initMatches()
                tournament?.setStatusInProgress(requireContext())
            }
            else{ tournament?.nextRound(requireContext()) }
            tournament?.let{ TournamentsDao(requireContext()).updateTournament(it) }
            refresh()
        }

        binding.goToMatchs.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            val navigationOptions = navOptions {
                popUpTo(R.id.ClasificationListFragment) {
                    inclusive = true
                }
            }
            findNavController().navigate(R.id.action_ClasificationListFragment_to_MatchesFragment, bundle, navigationOptions)
        }
    }

    val refresh = fun() {
        tournament?.initMatches()
        val originalParticipants = tournament?.participantList ?: mutableListOf()

        if (tournament?.tournamentStatus == TournamentStatus.IN_PROGRESS) {
            val lastMatches = tournament?.getLastMatchList() ?: mutableListOf()
            val sortedParticipants = mutableListOf<Participant>()

            lastMatches.forEach { match ->
                originalParticipants.find { it.nickname == match.competitorOne.name }?.let { sortedParticipants.add(it) }
                originalParticipants.find { it.nickname == match.competitorTwo.name }?.let { sortedParticipants.add(it) }
            }

            val eliminated = originalParticipants.filter { p ->
                !sortedParticipants.any { it.nickname == p.nickname }
            }.sortedByDescending { getLastRoundReached(it.nickname) }

            sortedParticipants.addAll(eliminated)
            originalParticipants.clear()
            originalParticipants.addAll(sortedParticipants)

        } else if (tournament?.tournamentStatus == TournamentStatus.FINISHED) {
            if (tournament?.type == TournamentType.ELIMINATION) {
                originalParticipants.sortWith(compareByDescending<Participant> { p ->
                    tournament?.getNotDead()?.any { it.name == p.nickname } ?: false
                }.thenByDescending { p ->
                    getLastRoundReached(p.nickname)
                })
            } else {
                originalParticipants.sortByDescending { p -> calculateTotalScore(p.nickname) }
            }
        }

        binding.tvNumParticipants.text =
            "Participantes activos: " +
            tournament?.getNotDead()?.filter { p -> p.name != "DESCANSO" }?.size.toString() +
            "/" +
            tournament?.participantList?.size.toString()

        if (tournament != null) {
            adapter = AddPunctuationParticipantAdapter(tournament!!, requireContext())
            binding.rvParticipants.adapter = adapter
        }

        updateNextRoundBtn()
    }

    private fun getLastRoundReached(nickname: String): Int {
        var lastRound = -1
        tournament?.columnMatches?.forEachIndexed { index, column ->
            if (column.matches.any { it.competitorOne.name == nickname || it.competitorTwo.name == nickname }) {
                lastRound = index
            }
        }
        return lastRound
    }

    private fun calculateTotalScore(nickname: String): Float {
        var total = 0f
        tournament?.columnMatches?.forEach { column ->
            column.matches.forEach { match ->
                if (match.competitorOne.name == nickname) {
                    total += match.competitorOne.score.toFloatOrNull() ?: 0f
                } else if (match.competitorTwo.name == nickname) {
                    total += match.competitorTwo.score.toFloatOrNull() ?: 0f
                }
            }
        }
        return total
    }

    private fun updateNextRoundBtn(){
        if(tournament?.creatorId != User.actualUser?.id)
            binding.nextRound.visibility = View.GONE
        else
            binding.nextRound.visibility = View.VISIBLE

        binding.nextRound.text = when (tournament?.tournamentStatus) {
            TournamentStatus.EDITABLE -> "Empezar torneo"
            TournamentStatus.IN_PROGRESS -> "Siguiente ronda"
            TournamentStatus.FINISHED -> "Torneo finalizado"
            else -> "Empezar torneo"
        }
        binding.nextRound.isEnabled = tournament?.tournamentStatus != TournamentStatus.FINISHED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
