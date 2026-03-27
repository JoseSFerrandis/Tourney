package com.example.tourney.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tourney.R
import com.example.tourney.adapters.ParticipantAdapter
import com.example.tourney.databinding.FragmentParticipantsListBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User

class ParticipantsListFragment : Fragment() {

    private var _binding: FragmentParticipantsListBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParticipantsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tournament = arguments?.getParcelable<Tournament>("tournament_data")

        // Llamamos a refresh para la carga inicial
        refresh()

        binding.addParticipant.setOnClickListener {
            if(tournament?.tournamentStatus != TournamentStatus.EDITABLE){
                Toast.makeText(requireContext(), "No se puede editar el torneo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(tournament?.hasSpace() == false){
                Toast.makeText(requireContext(), "No hay espacio para más participantes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_participant, null)
            val etParticipantName = dialogView.findViewById<EditText>(R.id.new_participant_username)

            builder.setView(dialogView)
                .setTitle("Añadir participante")
                .setPositiveButton("Añadir") { dialog, _ ->
                    val newParticipantName = etParticipantName.text.toString()
                    if(newParticipantName.isBlank()) {
                        Toast.makeText(requireContext(), "Nombre no válido", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val nextId = (tournament?.participantList?.minOfOrNull { it.id } ?: 0).coerceAtMost(0) - 1
                    val newParticipant = User(nextId, newParticipantName, "", "", 0)

                    // Comprobamos que no esté ya inscrito
                    tournament?.participantList?.forEach { participant ->
                        if(participant.nickname == newParticipantName ||
                            participant.id == newParticipant.id){
                            Toast.makeText(requireContext(), "Participante ya inscrito", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }

                    // Añadimos el participante y refrescamos
                    tournament?.addParticipant(newParticipant)
                    tournament?.restartMatches()
                    refresh()

                    Toast.makeText(requireContext(), "Participante añadido", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
    }

    val refresh = fun() {
        val participants = tournament?.participantList

        // 1. Gestionar visibilidad y Adapter
        if (participants.isNullOrEmpty()) {
            binding.tvEmptyList.visibility = View.VISIBLE
            binding.rvParticipants.visibility = View.GONE
        } else {
            binding.tvEmptyList.visibility = View.GONE
            binding.rvParticipants.visibility = View.VISIBLE

            // Si no tiene adapter, se lo ponemos. Si ya tiene, notificamos cambios.
            if (binding.rvParticipants.adapter == null) {
                binding.rvParticipants.adapter = ParticipantAdapter(tournament!!, updateInfo)
            } else {
                binding.rvParticipants.adapter?.notifyDataSetChanged()
            }
        }

        // 2. Actualizar estado del botón de añadir
        updateInfo.invoke()
    }

    val updateInfo = fun() {
        val canAdd = tournament?.hasSpace() ?: false && tournament?.tournamentStatus == TournamentStatus.EDITABLE
        binding.addParticipant.setBackgroundColor(
            if(canAdd) resources.getColor(R.color.accent_purple)
            else resources.getColor(R.color.text_hint)
        )

        binding.tvNumParticipants.text = "Participantes: ${tournament?.numParticipants}/${tournament?.maxParticipants}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
