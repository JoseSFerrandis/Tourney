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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.tourney.databinding.ActivityMainBinding
import com.example.tourney.entities.User
import com.example.tourney.repositories.TournamentRepository
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setImageBitmap(resources.getDrawable(R.drawable.ic_trophy).toBitmap())
        binding.fab.backgroundTintList = ColorStateList.valueOf(Color.WHITE)

        val primaryColor = MaterialColors.getColor(this, R.attr.appColorPrimaryDark, Color.parseColor("#041C9E"))
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

            // Usar el ID corregido del grafo de navegación
            if (destination.id != R.id.privacyFragmentDest) {
                checkCookiesConsent()
            }
        }

        TournamentRepository.getInstance().loadFromDatabase(this)
        logAllUsers()
        checkCookiesConsent()
    }

    override fun onResume() {
        super.onResume()
        if (::navController.isInitialized && navController.currentDestination?.id != R.id.privacyFragmentDest) {
            checkCookiesConsent()
        }
    }

    private fun checkCookiesConsent() {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("cookies_accepted", false)) {
            if (cookiesDialog == null || !cookiesDialog!!.isShowing) {
                showCookiesDialog()
            }
        }
    }

    private fun showCookiesDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.cookies_alert, null)
            cookiesDialog = MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            cookiesDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val tvReadMore = dialogView.findViewById<TextView>(R.id.tvReadMoreTerms)
            val cbTerms = dialogView.findViewById<MaterialCheckBox>(R.id.cbTerms)
            val cbPrivacy = dialogView.findViewById<MaterialCheckBox>(R.id.cbPrivacy)
            val btnAccept = dialogView.findViewById<MaterialButton>(R.id.btnAcceptCookies)

            val colorPrimary = MaterialColors.getColor(this, R.attr.appColorPrimary, Color.BLUE)
            val colorPrimaryLight = MaterialColors.getColor(this, R.attr.appColorPrimaryLight, Color.LTGRAY)

            val updateButtonState = {
                val isAccepted = (cbTerms?.isChecked == true) && (cbPrivacy?.isChecked == true)
                btnAccept?.isEnabled = isAccepted
                val targetColor = if (isAccepted) colorPrimary else colorPrimaryLight
                btnAccept?.backgroundTintList = ColorStateList.valueOf(targetColor)
            }

            updateButtonState()
            cbTerms?.setOnCheckedChangeListener { _, _ -> updateButtonState() }
            cbPrivacy?.setOnCheckedChangeListener { _, _ -> updateButtonState() }

            tvReadMore?.setOnClickListener {
                cookiesDialog?.dismiss()
                navController.navigate(R.id.privacyFragmentDest)
            }

            btnAccept?.setOnClickListener {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("cookies_accepted", true).apply()
                cookiesDialog?.dismiss()
            }

            cookiesDialog?.show()
        } catch (e: Exception) {
            Log.e("ERROR", "No se pudo mostrar el diálogo de cookies: ${e.message}")
        }
    }

    private fun logAllUsers() {
        try {
            val users = UsersDao(this).getAllUsers()
            Log.e("DEBUG_USERS", "=== USUARIOS REGISTRADOS ===")
            users.forEach { user ->
                Log.e("DEBUG_USERS", "Email: ${user.email} | Nick: ${user.nickname}")
            }
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
        
        if (User.actualUser?.id?.toInt() == 3) {
            btnJoin.visibility = View.GONE
        }

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
            R.id.action_privacy -> {
                navController.navigate(R.id.privacyFragmentDest)
                true
            }
            R.id.action_contact -> {
                navController.navigate(R.id.contactFragmentDest)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
