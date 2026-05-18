package com.example.tourney.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.tourney.adapters.TournamentAdapter
import com.example.tourney.R
import com.example.tourney.databinding.FragmentHomeBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.viewModel.HomeViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2
    private val viewModel: HomeViewModel by viewModels({ this })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = binding.pager
        val tabLayout = binding.tabLayout

        updateTournamentAdapter()
        Log.e("DEBUG_USERS", " ${User.actualUser?.id}  Email: ${User.actualUser?.email} | Password: ${User.actualUser?.password} , ${User.actualUser?.nickname}")

        if(User.actualUser?.logged == true) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Mis torneos"
                    1 -> tab.text = "Participando"
                    2 -> tab.text = "Siguiendo"
                }
            }.attach()
        }else {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "Mis torneos"
                }
            }.attach()
        }

        setupListeners()
        updateProfileImage()
        binding.tvGreeting.text = "Hola, ${User.actualUser?.nickname}"
    }

    private fun updateProfileImage() {
        val pn = User.actualUser?.photo ?: 0
        
        // Si el número es válido (del 1 al 18)
        if (pn > 0) {
            val resId = resources.getIdentifier("ic_user_pfp$pn", "drawable", requireContext().packageName)
            if (resId != 0) {
                binding.ivProfile.setImageResource(resId)
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
            }
        } else {
            // Imagen por defecto si es 0 o nulo
            binding.ivProfile.setImageResource(R.drawable.ic_user_pfp1)
        }
    }

    private fun setupListeners() {
        binding.btnProfile.setOnClickListener {
            //if (User.actualUser?.id?.toInt() != 3) {
                findNavController().navigate(R.id.action_HomeFragment_to_ProfileFragment)
             //}else{
               //  Toast.makeText(requireContext(), "No puedes acceder a tu perfil como invitado", Toast.LENGTH_SHORT).show()
             //}
        }

        // Search
        binding.btnSearchTournament.setOnClickListener {
            viewModel.updateSearch(binding.etSearch.text.toString().trim())

            // Cierra el menú de filtros si estuviera abierto
            binding.vwFiltersTournamentSearch.isVisible = false
        }
        binding.btnSearchFilters.setOnClickListener {
            binding.vwFiltersTournamentSearch.isVisible = !binding.vwFiltersTournamentSearch.isVisible
        }
        binding.chkFilterTournamentSearchName.setOnClickListener {
            viewModel.updateFilterByNames(binding.chkFilterTournamentSearchName.isChecked)
        }
        binding.chkFilterTournamentSearchGame.setOnClickListener {
            viewModel.updateFilterByGames(binding.chkFilterTournamentSearchGame.isChecked)
        }

        // Formats
        binding.btnFilterTournamentSearchEliminacion.isChecked = true
        binding.btnFilterTournamentSearchEliminacion.setOnClickListener {
            viewModel.updateFilterByElimination(binding.btnFilterTournamentSearchEliminacion.isChecked)
        }
        binding.btnFilterTournamentSearchLiguilla.isChecked = true
        binding.btnFilterTournamentSearchLiguilla.setOnClickListener {
            viewModel.updateFilterByLiguilla(binding.btnFilterTournamentSearchLiguilla.isChecked)
        }
        binding.btnFilterTournamentSearchSuizo.isChecked = true
        binding.btnFilterTournamentSearchSuizo.setOnClickListener {
            viewModel.updateFilterBySuizo(binding.btnFilterTournamentSearchSuizo.isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        updateProfileImage() // Actualizamos la imagen también al volver
        updateTournamentAdapter()
    }

    fun updateTournamentAdapter(){
        viewPager.adapter = TournamentCollectionAdapter(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TournamentCollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int =
        if(User.actualUser?.logged == true) 3 else 1

    override fun createFragment(position: Int): Fragment {
        val user = User.actualUser
        if(user?.logged == true)
            return when (position) {
                0 -> TournamentListFragment.newInstance(user.showableTournamentList)
                1 -> TournamentListFragment.newInstance(user.joinedTournamentList)
                else -> TournamentListFragment.newInstance(user.followingTournamentList)
            }
        else
            return TournamentListFragment.newInstance(user?.showableTournamentList ?: emptyList())
    }
}
