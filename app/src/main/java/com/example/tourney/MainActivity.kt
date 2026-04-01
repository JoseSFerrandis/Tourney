package com.example.tourney

import android.os.Bundle
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

        // Inicializa datos
        tournaments[0].participantList = users
        Tournament.setTournaments(tournaments)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        /*
            Se declaran aquí los torneos y los usuarios para tenerlos "a mano" pero están instanciados estáticamente en Tournament
         */
        // Lista global de torneos
        private val tournaments = mutableListOf(
            Tournament(3, "Championship Dungeons & Dragons", "D&D 5e",  "Marquitos", mutableListOf(),12, "20 Ene 2026", "Tienda Gaming Local", "$1,500", 1),
            Tournament(1, "Copa League of Legends 2026", "League of Legends", "Marquitos", mutableListOf(), 32, "25 Ene 2026", "KOI", "$5,000", 777),
            Tournament(2, "Torneo Counter-Strike Relámpago", "CS:GO", "Marquitos", mutableListOf(), 8, "18 Ene 2026", "Cybercafé Central", "$2,000", 69),
            Tournament(4, "Torneo Valorant Summer", "Valorant", "Marquitos", mutableListOf(),32, "28 Ene 2026", "Online/Presencial", "$3,000", 1000)
        )

        private val users = mutableListOf(
            User(1, "Marquitos", "marquitos@gmail.com", "password123", 1),
            User(2, "Pepito", "pepito@gmail.com", "password123", 1),
            User(3, "Juan", "juan@gmail.com", "password123", 1),
            User(4, "Pedro", "pedro@gmail.com", "password123", 1),
            User(5, "Jose", "jose@gmail.com", "password123", 1),
            User(6, "Esteban", "esteban@gmail.com", "password123", 1),
            User(7, "Sebastián", "sebastián@gmail.com", "password123", 1),
            User(8, "Julio", "julio@gmail.com", "password123", 1),
            User(9, "Marcos", "marcos@gmail.com", "password123", 1),
            User(10, "Erik", "erik@gmail.com", "password123", 1),
            User(11, "Javier", "javier@gmail.com", "password123", 1),
            User(12, "Lucas", "lucas@gmail.com", "password123", 1),
        )

        fun sortTournaments() {
            tournaments.sortBy({ it.date })
        }
    }
}
