package com.example.tourney.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.entities.Tournament
import com.example.tourney.R
import com.example.tourney.databinding.FragmentCreateTournamentBinding
import com.google.android.material.snackbar.Snackbar
import android.app.DatePickerDialog
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.UsersDao
import java.util.Calendar

class CreateTournamentFragment : Fragment(R.layout.fragment_create_tournament) {

    private var _binding: FragmentCreateTournamentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateTournamentBinding.bind(view)
        val context = requireContext()

        // Configurar el diálogo del calendario
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }


        //parseo que he realizado con la IA, estaría bien mirar de cambiar los tipos de datos
        // completamente ya que por ejemplo que el dinero sea una string, no es muy buena idea
        // tambíen quiero cambiar que no se establezca el numero de participatnes iniciales por ahora
        //pero si os gusta lo podemos dejar
        binding.btnCreateTournament.setOnClickListener {
            val name = binding.etName.text.toString()
            val game = binding.etGame.text.toString()


            val maxParticipants = binding.etMaxParticipants.text.toString().toIntOrNull() ?: 32
            val date = binding.etDate.text.toString()
            val location = binding.etLocation.text.toString()
            val prize = binding.etPrize.text.toString()
            val code = binding.etCode.text.toString().toIntOrNull() ?: 0


            if (name.isNotBlank() && game.isNotBlank()) {
                val newTournament = Tournament(
                    id = (100L..10000L).random(),
                    name = name,
                    game = game,
                    creator = establishedValue(context, User.actualUser?.nickname),
                    maxParticipants = maxParticipants,
                    date = date,
                    location = location,
                    prize = prize,
                    code = code
                )

                // IMPORTANTE - si tocamos esto fallará, por ahora no hay comprobaciónes
                //faltaría meter unos cuantos trycatch con debugs

                // A futuro: tras las comprobaciones, se añadirá el torneo a la base de datos con insert


                TournamentRepository.getInstance().addTournament(newTournament)
                User.actualUser?.addShowableTournament(newTournament.id)
                UsersDao(context).updateShowableTournamentList(User.actualUser?.email ?: "", User.actualUser?.showableTournamentList.toString().replace("[", "").replace("]", ""))

                Toast.makeText(requireContext(), "Torneo '$name' creado con éxito", Toast.LENGTH_LONG).show()

                // Ir a la pantalla del torneo recién creado
                val bundle = Bundle().apply {
                    putParcelable("tournament_data", newTournament)
                }

                try {
                    findNavController().navigate(R.id.action_CreateTournamentFragment_to_TournamentFragment, bundle)
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Acción de navegación no encontrada", Snackbar.LENGTH_LONG).show()
                }


            } else {
                Toast.makeText(requireContext(), "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

        //Poner el asterisco en rojo
        colorearAsteriscoNombre()
        colorearAsteriscoCompeti()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun establishedValue(context: Context, value: String?): String{
        return if (value.isNullOrBlank() || value == "null") {
            context.getString(R.string.no_established)
        } else {
            value
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha seleccionada
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etDate.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
    private fun colorearAsteriscoNombre() {
        val originalText = getString(R.string.nombre_Torneo)
        val spannable = SpannableString(originalText)

        // Buscamos la posición del asterisco
        val asteriskIndex = originalText.indexOf('*')

        if (asteriskIndex != -1) {
            // Aplicamos el color rojo solo al carácter del asterisco
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                asteriskIndex,
                asteriskIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Asignamos el texto procesado al TextView mediante el binding
        binding.tvName.text = spannable
    }
    private fun colorearAsteriscoCompeti() {
        val originalText = getString(R.string.competicion)
        val spannable = SpannableString(originalText)

        // Buscamos la posición del asterisco
        val asteriskIndex = originalText.indexOf('*')

        if (asteriskIndex != -1) {
            // Aplicamos el color rojo solo al carácter del asterisco
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                asteriskIndex,
                asteriskIndex + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // Asignamos el texto procesado al TextView mediante el binding
        binding.tvGame.text = spannable
    }
}
