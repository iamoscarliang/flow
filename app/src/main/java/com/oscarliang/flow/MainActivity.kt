package com.oscarliang.flow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.oscarliang.flow.databinding.ActivityMainBinding
import com.oscarliang.flow.repository.DarkMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this)

        initWindow()
        initNavController()
        initToolbar()
        toggleDarkMode()
    }

    private fun initWindow() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            // Prevent the app bar from overlapping the status bar
            binding.appbar.updatePadding(
                top = insets.top,
            )
            // Prevent the view from overlapping the navigation bar in landscape mode
            binding.root.updatePadding(
                right = insets.right,
            )
            windowInsets
        }
    }

    private fun initNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        this.navController = navHostFragment.navController
        binding.bottomNav?.setupWithNavController(navController)
        binding.navView?.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val id = destination.id
            val showNav = id != R.id.newsDetailFragment && id != R.id.browserFragment
            binding.appbar.isVisible = showNav
            binding.bottomNav?.isVisible = showNav
            binding.navView?.isVisible = showNav
        }
    }

    private fun initToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_settings)
        binding.toolbar.setOnMenuItemClickListener {
            navController.navigate(R.id.action_to_settingsDialog)
            true
        }
    }

    private fun toggleDarkMode() {
        viewModel.darkModeLiveData.observe(this) {
            when (it!!) {
                DarkMode.DEFAULT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

                DarkMode.LIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

                DarkMode.DARK ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

}