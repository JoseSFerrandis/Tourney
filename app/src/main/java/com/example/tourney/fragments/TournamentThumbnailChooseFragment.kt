package com.example.tourney.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tourney.R
import com.example.tourney.adapters.TournamentThumbnailAdapter
import com.example.tourney.databinding.FragmentTournamentThumbnailChooseBinding
import com.example.tourney.entities.Tournament
import com.example.tourney.tools.TournamentsDao

class TournamentThumbnailChooseFragment : Fragment() {

    private var _binding: FragmentTournamentThumbnailChooseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTournamentThumbnailChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperamos el torneo si estamos en modo edición
        val tournament = arguments?.getParcelable<Tournament>("tournament_data")

        // Lista de recursos que coinciden con los nombres: tournament_thumbnail_1, 2, 3...
        // Nota: Asegúrate de que los archivos en drawable se llamen exactamente así en minúsculas
        val thumbnailResources = listOf(
            R.drawable.tournament_thumbnail_1,
            R.drawable.tournament_thumbnail_2,
            R.drawable.tournament_thumbnail_3,
            R.drawable.tournament_thumbnail_4,
            R.drawable.tournament_thumbnail_5,
            R.drawable.tournament_thumbnail_6,
            R.drawable.tournament_thumbnail_13,
            R.drawable.tournament_thumbnail_8,
            R.drawable.tournament_thumbnail_9,
            R.drawable.tournament_thumbnail_10,
            R.drawable.tournament_thumbnail_11,
            R.drawable.tournament_thumbnail_12
        )

        val adapter = TournamentThumbnailAdapter(thumbnailResources) { selectedIndex ->
            // El thumbnailId es el número de la foto (1, 2, 3...)
            val thumbnailId = selectedIndex + 1
            Log.d("ThumbnailAdapter", "Seleccionada miniatura número: $thumbnailId")
            
            if (tournament != null) {
                // MODO EDICIÓN: Guardamos directamente en la base de datos
                TournamentsDao(requireContext()).updateTournamentThumbnail(tournament.id, thumbnailId)
                tournament.thumbnail = thumbnailId
            }
            
            // MODO CREACIÓN/EDICIÓN: Devolvemos el resultado al fragmento anterior
            val result = Bundle().apply {
                putInt("selected_thumbnail", thumbnailId)
            }
            parentFragmentManager.setFragmentResult("thumbnail_request", result)
            
            findNavController().popBackStack()
        }

        val spanCount = if (resources.configuration.screenWidthDp >= 600) 3 else 2
        binding.rvThumbnails.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.rvThumbnails.adapter = adapter

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}