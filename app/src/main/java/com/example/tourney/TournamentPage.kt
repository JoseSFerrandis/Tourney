package com.example.tourney

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.tourney.databinding.FragmentJoinTournamentPageBinding
import com.example.tourney.databinding.FragmentTournamentPageBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
class TournamentPage : Fragment() {

    private var _binding: FragmentTournamentPageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTournamentPageBinding.bind(view)

        binding.btnJoin.setOnClickListener {
            Toast.makeText(requireContext(), "Función no implementada", Toast.LENGTH_SHORT).show()


        }



        binding.btnReject.setOnClickListener {
            Toast.makeText(requireContext(), "Función no implementad", Toast.LENGTH_SHORT).show()

        }


    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}