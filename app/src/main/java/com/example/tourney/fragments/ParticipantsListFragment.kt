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
import com.example.tourney.entities.Participant
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.TournamentStatus
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository

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
                    val newParticipantName = etParticipantName.text.toString().trim()
                    if(newParticipantName.isBlank()) {
                        Toast.makeText(requireContext(), "Nombre no válido", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    // Comprobar si ya existe un participante con el mismo nombre (ignorar mayúsculas/minúsculas)
                    if (tournament?.participantList?.any { it.nickname.equals(newParticipantName, ignoreCase = true) } == true) {
                        Toast.makeText(requireContext(), "Participante ya existente", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val newParticipant = Participant(nickname = newParticipantName)

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
        if(tournament?.creatorId != User.actualUser?.id)
            binding.addParticipant.visibility = View.GONE
        else
            binding.addParticipant.visibility = View.VISIBLE

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

        TournamentRepository.getInstance(requireContext()).updateParticipants(
            tournament!!,
            requireContext(),
            onError = { error -> Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show() }
        )
    }

    val updateInfo = fun() {
        val canAdd = tournament?.hasSpace() ?: false && tournament?.tournamentStatus == TournamentStatus.EDITABLE
        
        // Cambiamos el estado habilitado y la opacidad en lugar de forzar el color de fondo
        binding.addParticipant.isEnabled = canAdd
        binding.addParticipant.alpha = if (canAdd) 1.0f else 0.5f

        binding.tvNumParticipants.text = "Participantes: ${tournament?.numParticipants}/${tournament?.maxParticipants}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
