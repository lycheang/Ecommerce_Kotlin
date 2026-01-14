package com.example.ecommerce.ui.theme.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        // 1. Get SharedPreferences
        val prefs = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        // 2. Handle Back Arrow Click (Uncommented so it works)
        // Ensure you have an 'imgBack' in your XML, or change ID to match
//        binding.imgBack.setOnClickListener {
//            findNavController().navigateUp()
//        }

        // 3. Load saved state BEFORE setting the listener
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        // 4. Handle Dark Mode Toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save the new state
            prefs.edit().putBoolean("dark_mode", isChecked).apply()

            // Apply the theme
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            // Note: The Activity will automatically recreate itself here
            // to apply the new colors. This is normal behavior.
        }
    }
}