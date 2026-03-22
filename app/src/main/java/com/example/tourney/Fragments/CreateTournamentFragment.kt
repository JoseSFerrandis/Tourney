package com.example.tourney.Fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tournamentapp.models.Tournament
import com.example.tourney.MainActivity
import com.example.tourney.R
import com.example.tourney.databinding.FragmentCreateTournamentBinding

class CreateTournamentFragment : Fragment(R.layout.fragment_create_tournament) {

    private var _binding: FragmentCreateTournamentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateTournamentBinding.bind(view)

        //parseo que he realizado con la IA, estaría bien mirar de cambiar los tipos de datos
        // completamente  ya que por ejemplo que el dinero sea una string, no es muy buena idea
        // tambíen quiero cambiar que no se establezca el numero de participatnes iniciales por ahora
        //pero si os gusta lo podemos dejar
        binding.btnCreateTournament.setOnClickListener {
            val name = binding.etName.text.toString()
            val game = binding.etGame.text.toString()

            //esta la deberíamos de quitar, pero la dejo por si acaso
            val participants = binding.etParticipants.text.toString().toIntOrNull() ?: 0

            val maxParticipants = binding.etMaxParticipants.text.toString().toIntOrNull() ?: 32
            val date = binding.etDate.text.toString()
            val location = binding.etLocation.text.toString()
            val prize = binding.etPrize.text.toString()
            val code = binding.etCode.text.toString().toIntOrNull() ?: 0


            if (name.isNotEmpty() && game.isNotEmpty()) {
                val newTournament = Tournament(
                    id = (100..10000).random(),
                    name = name,
                    game = game,
                    participants = participants,
                    maxParticipants = maxParticipants,
                    date = date,
                    location = location,
                    status = "Inscripciones Abiertas",
                    prize = prize,
                    code = code
                )

                // IMPORTANTE - si tocamos esto fallará, por ahora no hay comprobaciónes
                //faltaría meter unos cuantos trycatch con debugs

                // A futuro: tras las comprobaciones, se añadirá el torneo a la base de datos  con insert
                //falta meter el campo de usuario dueño (ID clave foranea) y lista mutable de usuarios

                MainActivity.addTournament(newTournament)

                Toast.makeText(requireContext(), "Torneo '$name' creado con éxito", Toast.LENGTH_LONG).show()
                
                // Volver al Dashboard (HomeFragment)
                findNavController().popBackStack()
            } else {
                Toast.makeText(requireContext(), "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
