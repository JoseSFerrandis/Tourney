package com.example.tourney.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.entities.Tournament
import com.example.tourney.databinding.FragmentTournamentPageBinding
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.TournamentType
import com.example.tourney.entities.User
import com.example.tourney.tools.UsersDao
import java.util.Date
import kotlin.toString

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
                setupClickListeners(tournament)
            } else {
                Log.e("TournamentPage", "No se recibieron datos del torneo")
                Toast.makeText(requireContext(), "Error al cargar datos del torneo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("TournamentPage", "Error procesando argumentos: ${e.message}")
            Toast.makeText(requireContext(), "Error crítico al cargar la página", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI(tournament: Tournament) {
        // Rellenamos la UI con los datos recibidos
        binding.tvTournamentTitle.text = establishedValue( tournament.name )
        binding.tvGameName.text = establishedValue( tournament.game )
        binding.tvCreator.text = establishedValue( tournament.creatorNickname )
        binding.tvParticipants.text = establishedValue( "${tournament.numParticipants}/${tournament.maxParticipants}" )
        binding.tvDate.text = establishedValue( tournament.date )
        binding.tvLocation.text = establishedValue( tournament.location )
        binding.tvPrize.text = establishedValue( tournament.prize )
        binding.tvTournamentType.text = establishedValue( Tournament.getTournamentTypeString(tournament.type) )
        binding.btnFollow?.isChecked = User.actualUser?.followingTournamentList?.contains(tournament.id) == true


        binding.btnFollow?.isVisible = User.actualUser?.nickname != tournament.creatorNickname


        // Actualiza el color del badge según el estado del torneo
        val badgeBackground = when (tournament.tournamentStatus) {
            TournamentStatus.EDITABLE -> R.drawable.bg_badge_open
            TournamentStatus.IN_PROGRESS -> R.drawable.bg_badge_progress
            TournamentStatus.FINISHED -> R.drawable.bg_badge_finished
        }
        binding.tvStatusBadge.setBackgroundResource(badgeBackground)

        binding.tvStatusBadge.text = when (tournament.tournamentStatus) {
            TournamentStatus.EDITABLE -> "Abierto"
            TournamentStatus.IN_PROGRESS -> "En progreso"
            TournamentStatus.FINISHED -> "Finalizado"
        }

        // Por ahora el XML solo tiene el título y botones
    }

    private fun setupClickListeners(tournament: Tournament) {
        binding.btnClassification.setOnClickListener {
            val tournament = arguments?.getParcelable<Tournament>("tournament_data")
            if (tournament != null) {
                val bundle = Bundle().apply {
                    putParcelable("tournament_data", tournament)
                }
                findNavController().navigate(R.id.action_TournamentFragment_to_ClasificationListFragment, bundle)
            }
        }

        binding.btnMatches.setOnClickListener {
            val tournament = arguments?.getParcelable<Tournament>("tournament_data")
            if (tournament != null) {
                val bundle = Bundle().apply {
                    putParcelable("tournament_data", tournament)
                }
                findNavController().navigate(R.id.action_TournamentFragment_to_MatchesFragment, bundle)
            }
        }

        binding.btnRules.setOnClickListener {
            val tournament = arguments?.getParcelable<Tournament>("tournament_data")
            if (tournament != null) {
                val rulesTextId = when (tournament.type) {
                    TournamentType.ELIMINATION -> R.string.rules_elimination
                    TournamentType.LIGUILLA -> R.string.rules_liguilla
                    TournamentType.SUIZO -> R.string.rules_suizo
                    TournamentType.OTRO -> R.string.rules_otro
                }
                
                AlertDialog.Builder(requireContext())
                    .setTitle(R.string.rules_title)
                    .setMessage(rulesTextId)
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Error al cargar las reglas", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnViewParticipants.setOnClickListener {
            val tournament = arguments?.getParcelable<Tournament>("tournament_data")
            if (tournament != null) {
                val bundle = Bundle().apply {
                    putParcelable("tournament_data", tournament)
                    // Convertimos la lista a ArrayList para que sea Parcelable
                    //putParcelableArrayList("participants_list", ArrayList(tournament.participantList))
                }
                findNavController().navigate(R.id.action_TournamentFragment_to_ParticipantsListFragment, bundle)
            }
        }

        binding.btnFollow?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Lógica para añadir a siguiendo
                User.actualUser?.followingTournamentList?.add(tournament.id)
                Toast.makeText(context, "Siguiendo torneo", Toast.LENGTH_SHORT).show()
            } else {
                // Lógica para dejar de seguir
                User.actualUser?.followingTournamentList?.remove(tournament.id)
            }

            // Aquí es donde usarías el Context para guardar en la DB (como vimos antes)
            //UsersDao(requireContext()).updateUser(User.actualUser.id!!)
            UsersDao(requireContext()).updateFollowingTournamentList(User.actualUser?.email!!,
                User.actualUser?.followingTournamentList.toString()?.replace("[", "")?.replace("]", "")!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun establishedValue(value: String): String{
        return if (value.isNullOrBlank() || value == "null") {
            requireContext().getString(R.string.no_established)
        } else {
            value
        }
    }
    fun establishedValue(value: Long?): String{
        return if (value == null) {
            requireContext().getString(R.string.no_established)
        } else {
            Date(value).toString()
        }
    }
}
