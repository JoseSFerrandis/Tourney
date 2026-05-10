package com.example.tourney.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentCreateTournamentBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.TournamentsDao
import com.example.tourney.tools.UsersDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class CreateTournamentFragment : Fragment(R.layout.fragment_create_tournament) {

    private var _binding: FragmentCreateTournamentBinding? = null
    private val binding get() = _binding!!
    private var tournamentDate: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateTournamentBinding.bind(view)
        val context = requireContext()

        // Infla el spinner de selección de tipo de torneo
        binding.spTournamentType.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tournament_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spTournamentType.setSelection(0)

        // Configurar el diálogo del calendario
        binding.etDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnTipoTorneoHelp?.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_tournament_type_help, null)
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create()

            // Fondo transparente para que se vean los bordes redondeados
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            dialogView.findViewById<Button>(R.id.btnOk).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        //parseo que he realizado con la IA, estaría bien mirar de cambiar los tipos de datos
        // completamente ya que por ejemplo que el dinero sea una string, no es muy buena idea
        // tambíen quiero cambiar que no se establezca el numero de participatnes iniciales por ahora
        //pero si os gusta lo podemos dejar
        binding.btnCreateTournament.setOnClickListener {
            val name = binding.etName.text.toString()
            val game = binding.etGame.text.toString()
            val maxParticipants = binding.etMaxParticipants.text.toString().toIntOrNull() ?: 32
            val date = tournamentDate
            val location = binding.etLocation.text.toString()
            val prize = binding.etPrize.text.toString()
            val code = binding.etCode.text.toString().toIntOrNull()
            val type = Tournament.getTournamentTypeFromString(binding.spTournamentType.selectedItem.toString())

            if (name.isNotBlank() && game.isNotBlank()) {
                val newTournament = Tournament(
                    id = 0, // Se asignará en el insert del DAO
                    name = name,
                    game = game,
                    creatorId = User.actualUser!!.id,
                    creatorNickname = establishedValue(context, User.actualUser?.nickname),
                    maxParticipants = maxParticipants,
                    date = date,
                    location = location,
                    prize = prize,
                    code = code,
                    type = type
                )

                // 1. GUARDAR EN LA BASE DE DATOS
                val tournamentsDao = TournamentsDao(context)
                val newId = tournamentsDao.insertTournament(newTournament)
                
                if (newId != -1L) {
                    newTournament.id = newId
                    
                    // 2. ACTUALIZAR EN REPOSITORIO Y USUARIO
                    TournamentRepository.getInstance().addTournament(newTournament)
                    User.actualUser?.addShowableTournament(newTournament.id)
                    
                    UsersDao(context).updateShowableTournamentList(
                        User.actualUser?.email ?: "", 
                        User.actualUser?.showableTournamentList.toString().replace("[", "").replace("]", "")
                    )

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
                    Toast.makeText(requireContext(), "Error al guardar el torneo en la base de datos", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(requireContext(), "Por favor, completa los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }

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
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                tournamentDate = selectedCalendar.timeInMillis
                
                val selectedDateStr = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etDate.setText(selectedDateStr)
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
        val asteriskIndex = originalText.indexOf('*')
        if (asteriskIndex != -1) {
            spannable.setSpan(ForegroundColorSpan(Color.RED), asteriskIndex, asteriskIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvName.text = spannable
    }

    private fun colorearAsteriscoCompeti() {
        val originalText = getString(R.string.competicion)
        val spannable = SpannableString(originalText)
        val asteriskIndex = originalText.indexOf('*')
        if (asteriskIndex != -1) {
            spannable.setSpan(ForegroundColorSpan(Color.RED), asteriskIndex, asteriskIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvGame.text = spannable
    }
}
