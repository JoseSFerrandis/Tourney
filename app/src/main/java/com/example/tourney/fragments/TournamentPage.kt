package com.example.tourney.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.TournamentsDao
import com.example.tourney.tools.UsersDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Date

class TournamentPage : Fragment() {

    private var _binding: FragmentTournamentPageBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTournamentPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Listener para recibir la miniatura seleccionada desde el selector
        parentFragmentManager.setFragmentResultListener("thumbnail_request", viewLifecycleOwner) { _, bundle ->
            val selectedThumbnail = bundle.getInt("selected_thumbnail")
            tournament?.let { t ->
                t.thumbnail = selectedThumbnail
                // Actualizar en base de datos
                TournamentsDao(requireContext()).updateTournamentThumbnail(t.id, selectedThumbnail)
                // Refrescar imagen en la cabecera
                updateHeaderImage(selectedThumbnail)
                Toast.makeText(requireContext(), "Portada actualizada", Toast.LENGTH_SHORT).show()
            }
        }

        try {
            tournament = arguments?.getParcelable<Tournament>("tournament_data")

            if (tournament != null) {
                setupUI(tournament!!)
                setupClickListeners(tournament!!)
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
        binding.btnClassification.text =
            if(tournament.tournamentStatus == TournamentStatus.FINISHED) "Clasificación"
            else "Resultados"

        binding.tvTournamentTitle?.text = establishedValue(tournament.name)
        binding.tvGameName?.text = establishedValue(tournament.game)
        binding.tvCreator?.text = establishedValue(tournament.creatorNickname)
        binding.tvParticipants?.text =
            establishedValue("${tournament.numParticipants}/${tournament.maxParticipants}")
        binding.tvDate?.text = establishedValue(tournament.date)
        binding.tvLocation?.text = establishedValue(tournament.location)
        binding.tvPrize?.text = establishedValue(tournament.prize)
        binding.tvTournamentType?.text =
            establishedValue(Tournament.getTournamentTypeString(tournament.type))

        val actualNickname = User.actualUser?.nickname?.trim()
        val creatorNickname = tournament.creatorNickname?.trim()
        val isOwner = actualNickname.equals(creatorNickname, ignoreCase = true)

        binding.btnFollow?.isChecked =
            User.actualUser?.followingTournamentList?.contains(tournament.id) == true

        // Aplicamos visibilidad
        binding.btnFollow?.isVisible = !isOwner
        binding.btnJoin?.isVisible = !isOwner
        binding.btnSettings?.isVisible = isOwner
        binding.btnJoin.isEnabled = tournament.tournamentStatus == TournamentStatus.EDITABLE
        binding.btnJoin.text = if(tournament.participantList.find { it.userId == User.actualUser?.id } != null) "Desinscribirse" else "Inscribirse"

        // Cargar imagen de portada inicial
        updateHeaderImage(tournament.thumbnail)


        // Actualiza el color del badge según el estado del torneo
        val badgeBackground = when (tournament.tournamentStatus) {
            TournamentStatus.EDITABLE -> R.drawable.bg_badge_open
            TournamentStatus.IN_PROGRESS -> R.drawable.bg_badge_progress
            TournamentStatus.FINISHED -> R.drawable.bg_badge_finished
        }
        binding.tvStatusBadge?.setBackgroundResource(badgeBackground)

        binding.tvStatusBadge?.text = when (tournament.tournamentStatus) {
            TournamentStatus.EDITABLE -> "Abierto"
            TournamentStatus.IN_PROGRESS -> "En progreso"
            TournamentStatus.FINISHED -> "Finalizado"
        }
    }

    private fun updateHeaderImage(thumbnailId: Int) {
        // Usamos findViewById directamente para asegurar el tipo ImageView
        // Esto evita errores de DataBinding cuando los tipos en los layouts (móvil/tablet) no coinciden exactamente
        val header = binding.root.findViewById<ImageView>(R.id.headerImage)
        if (thumbnailId > 0) {
            // Android requiere nombres de recursos en minúsculas
            val resName = "tournament_thumbnail_$thumbnailId"
            val resId = resources.getIdentifier(resName, "drawable", requireContext().packageName)
            if (resId != 0) {
                header?.setImageResource(resId)
            } else {
                header?.setImageResource(R.drawable.dnd_thumbnail)
            }
        } else {
            header?.setImageResource(R.drawable.dnd_thumbnail)
        }
    }

    private fun setupClickListeners(tournament: Tournament) {
        binding.btnClassification.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            findNavController().navigate(R.id.action_TournamentFragment_to_ClasificationListFragment, bundle)
        }

        binding.btnMatches.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            findNavController().navigate(R.id.action_TournamentFragment_to_MatchesFragment, bundle)
        }

        binding.btnRules?.setOnClickListener { showRulesDialog(tournament) }
        //binding.btnRulesSecond?.setOnClickListener { showRulesDialog(tournament) }

        binding.btnViewParticipants.setOnClickListener {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            findNavController().navigate(R.id.action_TournamentFragment_to_ParticipantsListFragment, bundle)
        }

        // BOTÓN AJUSTES -> IR AL SELECTOR PASANDO EL TORNEO
        binding.btnSettings?.setOnClickListener {
            showOptionsDialog(tournament)
        }

        binding.btnFollow?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Lógica para añadir a siguiendo
                User.actualUser?.addFollowingTournament(tournament.id)
                Toast.makeText(context, "Siguiendo torneo", Toast.LENGTH_SHORT).show()
            } else {
                // Lógica para dejar de seguir
                User.actualUser?.removeFollowingTournament(tournament.id)
            }

            UsersDao(requireContext()).updateFollowingTournamentList(User.actualUser?.email!!,
                User.actualUser?.followingTournamentList.toString().replace("[", "").replace("]", ""))
        }

        binding.btnJoin.setOnClickListener {
            val alreadyJoined = tournament.participantList.find { it.userId == User.actualUser?.id } != null

            if(alreadyJoined){
                tournament.removeParticipant(User.actualUser!!)
                User.actualUser?.removeJoinedTournament(tournament.id)

                UsersDao(requireContext()).updateJoinedTournamentList(
                    User.actualUser?.email!!,
                    User.actualUser?.joinedTournamentList.toString().replace("[", "")
                        .replace("]", "")
                )
                TournamentsDao(requireContext()).updateTournament(tournament)
                Toast.makeText(requireContext(), "Desinscrito", Toast.LENGTH_SHORT).show()
                setupUI(tournament)
                return@setOnClickListener
            }

            if(tournament.addParticipant(User.actualUser!!)){
                User.actualUser?.addJoinedTournament(tournament.id)
                UsersDao(requireContext()).updateJoinedTournamentList(
                    User.actualUser?.email!!,
                    User.actualUser?.joinedTournamentList.toString().replace("[", "")
                        .replace("]", "")
                )
                TournamentsDao(requireContext()).updateTournament(tournament)
                Toast.makeText(requireContext(), "Inscripción exitosa", Toast.LENGTH_SHORT).show()
                setupUI(tournament)
            }
        }
    }
    //La nueva opción para el dialogo custom
    fun showOptionsDialog(tournament: Tournament) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tournament_options, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnEditDetails = dialogView.findViewById<MaterialButton>(R.id.btnEditDetails)
        val btnEditThumbnail = dialogView.findViewById<MaterialButton>(R.id.btnEditThumbnail)
        val btnDeleteTournament = dialogView.findViewById<MaterialButton>(R.id.btnDeleteTournament)


        if (tournament.tournamentStatus != TournamentStatus.EDITABLE) {
            btnEditDetails.visibility = View.GONE
        }
        btnEditDetails.setOnClickListener {
            modifyDetails(tournament)
            dialog.dismiss()
        }

        btnEditThumbnail.setOnClickListener {
            modifyThumbnail(tournament)
            dialog.dismiss()
        }

        btnDeleteTournament.setOnClickListener {
            confirmDeleteTournament(tournament)
            dialog.dismiss()
        }

        dialog.show()
    }
    fun modifyDetails(tournament: Tournament) {
        if (tournament.tournamentStatus == TournamentStatus.EDITABLE) {
            val bundle = Bundle().apply {
                putParcelable("tournament_data", tournament)
            }
            // Pass the bundle here!
            findNavController().navigate(
                R.id.action_TournamentFragment_to_EditTournamentFragment,
                bundle
            )
        }
    }

    fun modifyThumbnail(tournament: Tournament) {
        val bundle = Bundle().apply {
            putParcelable("tournament_data", tournament)
        }
        findNavController().navigate(R.id.action_TournamentFragment_to_TournamentThumbnailChooseFragment, bundle)
    }

    fun confirmDeleteTournament(tournament: Tournament){
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar torneo")
            .setMessage("¿Estás seguro de que deseas eliminar este torneo?")
            .setPositiveButton("Sí") { _, _ ->
                TournamentRepository.getInstance().deleteTournament(requireContext(), tournament.id)
                Toast.makeText(requireContext(), "Torneo eliminado", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .setNegativeButton("No", null)
            .show()
    }

    fun showRulesDialog(tournament: Tournament) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun establishedValue(value: String?): String {
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
