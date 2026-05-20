package com.example.tourney.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tourney.R
import com.example.tourney.adapters.TournamentAdapter
import com.example.tourney.databinding.FragmentTournamentListBinding
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.viewModel.HomeViewModel

class TournamentListFragment : Fragment() {
    private var _binding: FragmentTournamentListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels({ requireParentFragment() })

    companion object {
        private const val ARG_IDS = "tournament_ids"

        fun newInstance(ids: List<Long>) = TournamentListFragment().apply {
            arguments = Bundle().apply {
                putLongArray(ARG_IDS, ids.toLongArray())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTournamentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ids = arguments?.getLongArray(ARG_IDS)?.toList() ?: emptyList()
        val tournaments =
            TournamentRepository.getInstance().searchTournamentListByIds(ids.toMutableList())

        // Si el LayoutManager no está definido en el XML (como en tablet), ponemos el lineal por defecto
        if (binding.recyclerView.layoutManager == null) {
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
        }

        binding.recyclerView.adapter = TournamentAdapter(tournaments) { tournament ->
            val bundle = Bundle().apply { putParcelable("tournament_data", tournament) }
            findNavController().navigate(R.id.action_HomeFragment_to_TournamentFragment, bundle)
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            (binding.recyclerView.adapter as? TournamentAdapter)?.filterTournaments(viewModel)
        }
        viewModel.searchFilterByElimination.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as? TournamentAdapter)?.filterTournaments(viewModel)
        }
        viewModel.searchFilterByLiguilla.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as? TournamentAdapter)?.filterTournaments(viewModel)
        }
        viewModel.searchFilterBySuizo.observe(viewLifecycleOwner) {
            (binding.recyclerView.adapter as? TournamentAdapter)?.filterTournaments(viewModel)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}