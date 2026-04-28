package com.example.tourney

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.tourney.entities.Tournament
import com.example.tourney.databinding.ActivityMainBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.UsersDao

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

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.LoginFragment -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.GONE
                }
                else -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }

        // --- LOGS DE DEPURACIÓN PARA BASE DE DATOS Y TORNEOS ---
//        logDatabaseInfo()
    }
//
//    private fun logDatabaseInfo() {
//        val TAG = "DATABASE_DEBUG"
//
//        // 1. Usuarios en la Base de Datos Local
//        val users = UsersDao(this).getAllUsers()
//        Log.d(TAG, "===== USUARIOS REGISTRADOS (${users.size}) =====")
//        users.forEach { user ->
//            Log.d(TAG, "ID: ${user.id} | Nick: ${user.nickname} | Email: ${user.email} | Photo: ${user.photo}  | Password ${user.password}" )
//        }
//
//        // 2. Torneos Existentes en el Repositorio
//        val tournaments = TournamentRepository.getInstance().getTournaments()
//        Log.d(TAG, "===== TORNEOS DISPONIBLES (${tournaments.size}) =====")
//        tournaments.forEach { tournament ->
//            Log.d(TAG, "ID: ${tournament.id} | Nombre: ${tournament.name} | Juego: ${tournament.game} | Creador: ${tournament.creator}")
//        }
//        Log.d(TAG, "==============================================")
//    }

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
            R.id.action_deleteTournaments -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
