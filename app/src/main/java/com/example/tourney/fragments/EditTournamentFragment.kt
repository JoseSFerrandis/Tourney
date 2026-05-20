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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tourney.R
import com.example.tourney.databinding.FragmentEditTournamentBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.TournamentsDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

class EditTournamentFragment : Fragment(R.layout.fragment_edit_tournament) {

    private var _binding: FragmentEditTournamentBinding? = null
    private val binding get() = _binding!!
    private var tournament: Tournament? = null
    private var tournamentDate: Long? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEditTournamentBinding.bind(view)
        
        tournament = arguments?.getParcelable("tournament_data")
        
        if (tournament == null) {
            Toast.makeText(requireContext(), "Error al cargar datos del torneo", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupUI()
        setupListeners()
        
        colorearAsteriscoNombre()
        colorearAsteriscoCompeti()
    }

    private fun setupUI() {
        if(User.actualUser?.logged == false){
            binding.etCode.visibility = View.GONE
            binding.tvCode.visibility = View.GONE
        }

        tournament?.let { t ->
            binding.etName.setText(t.name)
            binding.etGame.setText(t.game)
            binding.etLocation.setText(t.location)
            binding.etPrize.setText(t.prize)
            binding.etCode.setText(t.code?.toString() ?: "")

            tournamentDate = t.date
            t.date?.let {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it
                val dateStr = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
                binding.etDate.setText(dateStr)
            }

            // Configurar Spinner
            val adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.tournament_types,
                android.R.layout.simple_spinner_item
            ).also { arrayAdapter ->
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spTournamentType.adapter = adapter
            
            val typeString = Tournament.getTournamentTypeString(t.type)
            val spinnerPosition = adapter.getPosition(typeString)
            binding.spTournamentType.setSelection(spinnerPosition)
        }
    }

    private fun setupListeners() {
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

        binding.btnUpdateTournament.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val game = binding.etGame.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val prize = binding.etPrize.text.toString().trim()
            val code = binding.etCode.text.toString().toIntOrNull()
            val typeString = binding.spTournamentType.selectedItem.toString()
            val type = Tournament.getTournamentTypeFromString(typeString)

            if (name.isNotBlank() && game.isNotBlank()) {
                tournament?.let { t ->
                    // 1. Actualizamos el objeto con los nuevos valores de la UI
                    t.name = name
                    t.game = game
                    t.location = location
                    t.prize = prize
                    t.code = code
                    t.type = type
                    t.date = tournamentDate

                    // 2. Guardamos los cambios en la base de datos
                    val dao = TournamentsDao(requireContext())
                    val success = dao.updateTournament(t)

                    if (success) {
                        // 3. Sincronizamos el repositorio manteniendo la posición original
                        TournamentRepository.getInstance().updateTournamentInList(t)
                        
                        Toast.makeText(requireContext(), "Torneo actualizado correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Error al guardar los cambios en la base de datos", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Por favor, completa los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        tournamentDate?.let { calendar.timeInMillis = it }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                tournamentDate = selectedCalendar.timeInMillis
                
                val selectedDateStr = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.etDate.setText(selectedDateStr)
            },
            year, month, day
        ).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
