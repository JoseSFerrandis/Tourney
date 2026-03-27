package com.example.tourney.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentMatchesBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User
import com.ventura.bracketslib.BracketsView
import com.ventura.bracketslib.model.ColomnData
import com.ventura.bracketslib.model.CompetitorData
import com.ventura.bracketslib.model.MatchData
import java.util.ArrayList

class MatchesFragment : Fragment() {
    private var _binding: FragmentMatchesBinding? = null
    private val binding get() = _binding!!

    private var tournament : Tournament? = null
    private var bracketsView: BracketsView? = null

    private lateinit var competitors: MutableList<CompetitorData>
    private lateinit var columns: MutableList<ColomnData>
    private lateinit var notDead: MutableList<CompetitorData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tournament = arguments?.getParcelable<Tournament>("tournament_data")
        if(tournament == null){
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "Error al cargar datos del torneo", Toast.LENGTH_SHORT).show()
            return
        }

        // Inicialización de datos
        competitors = participantsListToCompetitorList(tournament!!.participantList)
        notDead = competitors.toMutableList()
        columns = mutableListOf()
        updateNextRoundBtn()

        binding.btnNextRound.setOnClickListener {
            // Si el torneo es editable (aún no ha empezado), deja de ser editable (acaba de empezar)
            if(tournament?.tournamentStatus == TournamentStatus.EDITABLE)
                tournament?.tournamentStatus = TournamentStatus.IN_PROGRESS

            nextRound()
            updateNextRoundBtn()
        }

        // Genera la primera ronda
        binding.bracketContainer.post {
            if (_binding != null) initBracketView()
        }
    }

    private fun nextRound(){
        if(notDead.isNotEmpty() && tournament?.tournamentStatus != TournamentStatus.FINISHED){
            if(tournament?.nextRound(requireContext())?: false) {
                // Forzamos el refresco re inflando la vista de forma segura
                refreshBracketView()
            }
        } else {
            updateNextRoundBtn()
        }
    }

    private fun initBracketView() {
        tournament?.initMatches()

        // Forzamos el refresco re-inflando la vista de forma segura
        refreshBracketView()
    }

    private fun refreshBracketView() {
        // Usamos un Handler para salir del ciclo actual de ejecución y evitar el IllegalStateException
        Handler(Looper.getMainLooper()).post {
            if (_binding == null) return@post

            try {
                // 1. Limpiamos el contenedor completamente
                binding.bracketContainer.removeAllViews()

                // 2. Inflamos el layout intermedio que contiene la BracketsView
                val bracketLayout = layoutInflater.inflate(R.layout.layout_brackets, binding.bracketContainer, false)
                val newBracketsView = bracketLayout.findViewById<BracketsView>(R.id.bracket_view)

                // 3. Añadimos la vista y actualizamos la referencia
                binding.bracketContainer.addView(bracketLayout)
                bracketsView = newBracketsView

                // 4. Seteamos los datos (usando ArrayList para asegurar compatibilidad con la librería)
                bracketsView?.setBracketsData(ArrayList(tournament?.columnMatches))

            } catch (e: Exception) {
                // Si aun así hay problemas de inflación, el log nos dirá por qué
                e.printStackTrace()
            }
        }
    }

    private fun participantsListToCompetitorList(participants : MutableList<User>) : MutableList<CompetitorData>{
        return participants.map { CompetitorData(it.nickname, "0") }.toMutableList()
    }

    private fun createMatches(competitors : MutableList<CompetitorData>) : MutableList<MatchData>{
        val matches = mutableListOf<MatchData>()
        for(i in 0 until competitors.size - 1 step 2){
            matches.add(MatchData(competitors[i], competitors[i + 1]))
        }
        return matches
    }

    private fun createColumn(matches : MutableList<MatchData>) : ColomnData{
        return ColomnData(matches)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateNextRoundBtn(){
        binding.btnNextRound.isEnabled = tournament?.tournamentStatus != TournamentStatus.FINISHED
        binding.btnNextRound.text = if(tournament?.tournamentStatus == TournamentStatus.FINISHED)
            "Torneo finalizado" else "Siguiente ronda"
    }
}