package com.example.tourney.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.tourney.R
import com.example.tourney.adapters.AddPunctuationParticipantAdapter
import com.example.tourney.databinding.FragmentClasificationListBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User

class ClasificationListFragment : Fragment() {
    private var _binding: FragmentClasificationListBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null
    private lateinit var adapter: AddPunctuationParticipantAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClasificationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tournament = arguments?.getParcelable<Tournament>("tournament_data")

        // Carga inicial
        refresh()

        binding.nextRound.setOnClickListener {
            // Empieza el torneo
            if(tournament?.tournamentStatus == TournamentStatus.EDITABLE){
                tournament?.initMatches()
                tournament?.setStatusInProgress(requireContext())
                refresh()
                return@setOnClickListener
            }
            //val matches = tournament?.columnMatches.
            tournament?.nextRound(requireContext())


            refresh()
        }

        binding.goToMatchs.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            //findNavController().navigate(R.id.action_ClasificationListFragment_to_MatchesFragment, bundle)

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

        // 1. ORDENACIÓN SEGÚN ESTADO Y TIPO
        if (tournament?.tournamentStatus == TournamentStatus.IN_PROGRESS) {
            // Si el torneo está en progreso, ordenamos por PAREJAS de la ronda actual
            val lastMatches = tournament?.getLastMatchList() ?: mutableListOf()
            val sortedParticipants = mutableListOf<User>()

            // Recorremos los partidos para añadir a los participantes en orden de emparejamiento
            lastMatches.forEach { match ->
                originalParticipants.find { it.nickname == match.competitorOne.name }?.let { sortedParticipants.add(it) }
                originalParticipants.find { it.nickname == match.competitorTwo.name }?.let { sortedParticipants.add(it) }
            }

            // Añadimos al final a los que NO están en la ronda actual (eliminados)
            val eliminated = originalParticipants.filter { p ->
                !sortedParticipants.any { it.nickname == p.nickname }
            }.sortedByDescending { getLastRoundReached(it.nickname) }

            sortedParticipants.addAll(eliminated)

            // Actualizamos la lista del torneo
            originalParticipants.clear()
            originalParticipants.addAll(sortedParticipants)

        } else if (tournament?.tournamentStatus == TournamentStatus.FINISHED) {
            // Si ha terminado, ordenamos por el ranking final
            if (tournament?.type == TournamentType.ELIMINATION) {
                originalParticipants.sortWith(compareByDescending<User> { p ->
                    tournament?.getNotDead()?.any { it.name == p.nickname } ?: false
                }.thenByDescending { p ->
                    getLastRoundReached(p.nickname)
                })
            } else {
                // Para LIGUILLA y SUIZO, ordenamos por puntos totales
                originalParticipants.sortByDescending { p -> calculateTotalScore(p.nickname) }
            }
        }

        // Pone el mensaje "Participantes activos: X/Y"
        binding.tvNumParticipants.text =
            "Participantes activos: " +
            tournament?.getNotDead()?.filter { p -> p.name != "DESCANSO" }?.size.toString() +
            "/" +
            tournament?.participantList?.size.toString()

        // 2. CONFIGURACIÓN DEL RECYCLERVIEW
        if (tournament != null) {
            adapter = AddPunctuationParticipantAdapter(tournament!!, requireContext())
            binding.rvParticipants.adapter = adapter
        }

        updateNextRoundBtn()
    }

    /**
     * Devuelve el índice de la última columna donde apareció el jugador
     */
    private fun getLastRoundReached(nickname: String): Int {
        var lastRound = -1
        tournament?.columnMatches?.forEachIndexed { index, column ->
            if (column.matches.any { it.competitorOne.name == nickname || it.competitorTwo.name == nickname }) {
                lastRound = index
            }
        }
        return lastRound
    }


    /**
     * Suma todos los scores de un participante a lo largo de todas las jornadas
     */
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
