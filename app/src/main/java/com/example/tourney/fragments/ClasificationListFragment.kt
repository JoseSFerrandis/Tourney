package com.example.tourney.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.tourney.adapters.AddPunctuationParticipantAdapter
import com.example.tourney.databinding.FragmentClasificationListBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus

class ClasificationListFragment : Fragment() {
    private var _binding: FragmentClasificationListBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null


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
    }

    // TODO: Crear el adapter, hacer que funcione, actualizar la vista, etc.
    val refresh = fun() {
        tournament?.initMatches()

        val participants = tournament?.participantList

        participants?.sortByDescending { p ->
            tournament?.getNotDead()?.any { it.name == p.nickname } ?: false
        }

        // 1. Gestionar visibilidad y Adapter
        if (participants.isNullOrEmpty()) {
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.rvParticipants.visibility = View.GONE
        } else {
            binding.tvEmptyList.visibility = View.GONE
            binding.rvParticipants.visibility = View.VISIBLE

            // Si no tiene adapter, se lo ponemos. Si ya tiene, notificamos cambios.
            if (binding.rvParticipants.adapter == null) {
                binding.rvParticipants.adapter = AddPunctuationParticipantAdapter(tournament!!, requireContext())
            } else {
                binding.rvParticipants.adapter?.notifyDataSetChanged()
            }
        }
        val notDead = tournament?.getNotDead()?.filterNot { it.name == "" }
        binding.tvNumParticipants.text = "Participantes vivos: ${notDead?.size}/${tournament?.numParticipants}"

        // 2. Actualizar estado del botón NextRound
        updateNextRoundBtn()
    }

    private fun updateNextRoundBtn(){
        binding.nextRound.text = when (tournament?.tournamentStatus) {
            TournamentStatus.EDITABLE -> "Empezar torneo"
            TournamentStatus.IN_PROGRESS -> "Siguiente ronda"
            TournamentStatus.FINISHED -> "Torneo finalizado"
            else -> "Empezar torneo"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}