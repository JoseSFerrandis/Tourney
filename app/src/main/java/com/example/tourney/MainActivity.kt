package com.example.tourney

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tourney.databinding.ActivityMainBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
import com.example.tourney.tools.APIService
import com.example.tourney.tools.TournamentsDao
import com.example.tourney.tools.UsersDao
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var cookiesDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Cargar preferencia de tema antes de super.onCreate
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val selectedTheme = prefs.getString("user_theme", "Blue")
        
        val themeId = when (selectedTheme) {
            "Purple" -> R.style.Theme_Tourney_Purple
            "GraySilver" -> R.style.Theme_Tourney_GraySilver
            "GraySlate" -> R.style.Theme_Tourney_GraySlate
            "GrayCharcoal" -> R.style.Theme_Tourney_GrayCharcoal
            "Cyan" -> R.style.Theme_Tourney_Cyan
            "SeaGreen" -> R.style.Theme_Tourney_SeaGreen
            "Emerald" -> R.style.Theme_Tourney_Emerald
            "Sunset" -> R.style.Theme_Tourney_Sunset
            "Crimson" -> R.style.Theme_Tourney_Crimson
            "Sakura" -> R.style.Theme_Tourney_Sakura
            "Midnight" -> R.style.Theme_Tourney_Midnight
            else -> R.style.Theme_Tourney
        }
        setTheme(themeId)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Recuperar NavController de forma segura a través del NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setImageBitmap(resources.getDrawable(R.drawable.ic_trophy).toBitmap())
        binding.fab.backgroundTintList = ColorStateList.valueOf(Color.WHITE)

        val primaryColor = MaterialColors.getColor(binding.fab, R.attr.appColorPrimaryDark)
        binding.fab.imageTintList = ColorStateList.valueOf(primaryColor)
        
        binding.fab.setOnClickListener { showCustomHomeDialog() }

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

            // Si cambiamos de pantalla y no estamos en los términos, comprobamos si hay que mostrar el aviso
            if (destination.id != R.id.TermsDetailFragment) {
                checkCookiesConsent()
            }
        }

        TournamentRepository.getInstance(this).loadFromDatabase(this)

        // DEBUG: Log de todos los usuarios registrados
        logAllUsers()

        // Comprobar consentimiento inicial
        checkCookiesConsent()
    }

    override fun onResume() {
        super.onResume()
        // Re-comprobar al volver a la app o al volver atrás de un fragmento
        if (::navController.isInitialized && navController.currentDestination?.id != R.id.TermsDetailFragment) {
            checkCookiesConsent()
        }
    }

    private fun checkCookiesConsent() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("cookies_accepted", false)) {
            // Solo lo mostramos si no está ya mostrándose
            if (cookiesDialog == null || !cookiesDialog!!.isShowing) {
                showCookiesDialog()
            }
        }
    }

    private fun showCookiesDialog() {
        val dialogView = layoutInflater.inflate(R.layout.cookies_alert, null)
        cookiesDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false) // No permite salir sin aceptar
            .create()

        cookiesDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvReadMore = dialogView.findViewById<TextView>(R.id.tvReadMoreTerms)
        val cbTerms = dialogView.findViewById<MaterialCheckBox>(R.id.cbTerms)
        val cbPrivacy = dialogView.findViewById<MaterialCheckBox>(R.id.cbPrivacy)
        val btnAccept = dialogView.findViewById<MaterialButton>(R.id.btnAcceptCookies)

        // Obtenemos los colores del tema actual para el botón
        val colorPrimary = MaterialColors.getColor(this, R.attr.appColorPrimary, Color.BLUE)
        val colorPrimaryLight = MaterialColors.getColor(this, R.attr.appColorPrimaryLight, Color.LTGRAY)

        val updateButtonState = {
            val isAccepted = (cbTerms?.isChecked == true) && (cbPrivacy?.isChecked == true)
            btnAccept?.isEnabled = isAccepted
            
            // Aplicar color dinámico
            val targetColor = if (isAccepted) colorPrimary else colorPrimaryLight
            btnAccept?.backgroundTintList = ColorStateList.valueOf(targetColor)
        }

        updateButtonState()

        cbTerms?.setOnCheckedChangeListener { _, _ -> updateButtonState() }
        cbPrivacy?.setOnCheckedChangeListener { _, _ -> updateButtonState() }

        tvReadMore?.setOnClickListener {
            cookiesDialog?.dismiss()
            navController.navigate(R.id.TermsDetailFragment)
        }

        btnAccept?.setOnClickListener {
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("cookies_accepted", true).apply()
            cookiesDialog?.dismiss()
        }

        cookiesDialog?.show()
    }

    private fun logAllUsers() {
        try {
            val users = UsersDao(this).getAllUsers()
            Log.e("DEBUG_USERS", "=== LISTA DE USUARIOS REGISTRADOS ===")
            users.forEach { user ->
                Log.e("DEBUG_USERS", "Email: ${user.email} | Password: ${user.password} | Nick: ${user.nickname}")
            }
            Log.e("DEBUG_USERS", "=====================================")
        } catch (e: Exception) {
            Log.e("DEBUG_USERS", "Error al leer usuarios: ${e.message}")
        }
    }

    private fun showCustomHomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_home_options, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCreate = dialogView.findViewById<Button>(R.id.btnCreateOption)
        val btnJoin = dialogView.findViewById<Button>(R.id.btnJoinOption)
        if(User.actualUser?.logged == false) btnJoin.visibility = View.GONE

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
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
