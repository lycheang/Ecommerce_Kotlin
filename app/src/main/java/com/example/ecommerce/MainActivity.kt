package com.example.ecommerce

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels // Required for by viewModels()
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.ContextCompat // Required for colors
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.ecommerce.databinding.ActivityMainBinding
import com.example.ecommerce.ui.theme.viewmodel.CartViewModel // Import your ViewModel
import com.example.ecommerce.util.Resource // Import your Resource class
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest // Required for Flow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // 1. FIX: Declare the ViewModel
    private val viewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialize NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. Define Top Level Destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.homeFragment,
                R.id.adminDashboardFragment,
                R.id.cartFragment,
                R.id.profileFragment,
                R.id.orderHistoryFragment
            )
        )

        // 4. Setup Toolbar & BottomNav
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)

        // 5. Setup Menu & Badge
        setupMenu()
        setupCartBadge() // Now works because viewModel is declared

        // 6. Visibility Logic
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Auth Screens -> Hide All
                R.id.loginFragment,
                R.id.signupFragment,
                R.id.splashFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE
                }
                // Admin Screens -> Hide All (Admin has own UI)
                R.id.adminDashboardFragment,
                R.id.adminProductListFragment,
                R.id.adminProductFragment,
                R.id.adminCategoryFragment,
                R.id.adminOrdersFragment,
                R.id.adminUserFragment,

                    // --- ADD THIS LINE ---
                R.id.adminOrdersDetailsFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                    binding.toolbar.visibility = View.GONE // Optional: Hide toolbar too if you have a custom one
                }

                // Customer Screens -> Show
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.toolbar.visibility = View.VISIBLE
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ecommerce_channel",
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for order status and checkout"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setupMenu() {
        // Handle Logout Click
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logoutUser()
                    true
                }
                else -> menuItem.onNavDestinationSelected(navController) || false
            }
        }

        // Force icons to show in overflow menu (Optional fix)
        val menu = binding.toolbar.menu
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        navController.navigate(R.id.loginFragment, null, navOptions)
    }

    private fun setupCartBadge() {
        // Use launchWhenStarted to safely collect flow
        lifecycleScope.launchWhenStarted {
            viewModel.cartItems.collectLatest { resource ->
                if (resource is Resource.Success) {
                    val items = resource.data ?: emptyList()
                    val count = items.size

                    val badge = binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment)

                    if (count > 0) {
                        badge.isVisible = true
                        badge.number = count
                        // FIX: Use ContextCompat for colors
                        badge.backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.red) // Ensure 'red' is in colors.xml
                        badge.badgeTextColor = ContextCompat.getColor(this@MainActivity, R.color.white)
                    } else {
                        badge.isVisible = false
                    }
                }
            }
        }
    }
}