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
        /*
        @Author:JL
        @Date:04/03/2026 22:19
        Esta es la función basica que teniamos en las demás practicas para usar fav y toolbar, en mis
        layouts de torneo hay una imagen en la parte supeiro por lo que he desactivado la toolbar
         */


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.DashboardFragment -> {
                    binding.fab.show()
                    binding.toolbar.visibility = View.VISIBLE
                }
                R.id.LoginFragment -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.GONE
                }
                R.id.TournamentFragment , R.id.JoinTournamentFragment  -> {
                    binding.fab.show()
                    binding.toolbar.visibility = View.GONE
                    // Desbloquear para otras pantallas si es necesario, o mantenerlo según diseño
                }
                else -> {
                    binding.fab.hide()
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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

    companion object{
        var actualUser: User? = null
    }
}