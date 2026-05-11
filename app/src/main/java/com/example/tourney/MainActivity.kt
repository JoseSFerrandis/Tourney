package com.example.tourney

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tourney.databinding.ActivityMainBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.TournamentsDao
import com.example.tourney.tools.UsersDao
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setImageBitmap(resources.getDrawable(R.drawable.ic_trophy).toBitmap())
        binding.fab.backgroundTintList = getColorStateList(R.color.white)
        binding.fab.imageTintList = getColorStateList(R.color.DarkBlue2)
        
        binding.fab.setOnClickListener {
            if (User.actualUser?.id?.toInt() == 3){
                navController.navigate(R.id.action_HomeFragment_to_CreateTournamentFragment)
            }else
                showCustomHomeDialog()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.LoginFragment -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.GONE
                }
                R.id.HomeFragment -> {
                    binding.fab.show()
                    binding.toolbar.visibility = View.VISIBLE
                }
                else -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }

        TournamentRepository.getInstance().loadFromDatabase(this)

        // DEBUG: Log de todos los usuarios registrados
        logAllUsers()
    }

      private fun logAllUsers() {
          try {
              val users = UsersDao(this).getAllUsers()
              Log.e("DEBUG_USERS", "=== LISTA DE USUARIOS REGISTRADOS ===")
              users.forEach { user ->
                  Log.e("DEBUG_USERS", " ${user.id}  Email: ${user.email} | Password: ${user.password} | Nick: ${user.nickname}")
              }
              Log.e("DEBUG_USERS", "=====================================")
          } catch (e: Exception) {
              Log.e("DEBUG_USERS", "Error al leer usuarios: ${e.message}")
          }
      }

    //La nueva función para el alert Custom
    private fun showCustomHomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_home_options, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Ajustar fondo transparente para que se vean los bordes redondeados del CardView
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateOption)
        val btnJoin = dialogView.findViewById<Button>(R.id.btnJoinOption)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        btnCreate.setOnClickListener {
            navController.navigate(R.id.action_HomeFragment_to_CreateTournamentFragment)
            dialog.dismiss()
        }

        btnJoin.setOnClickListener {
            navController.navigate(R.id.action_HomeFragment_to_JoinTournamentFragment)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_deleteTable -> {
                UsersDao(this).dropAll()
                true
            }
            R.id.action_deleteTournaments -> {
                TournamentsDao(this).dropAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
