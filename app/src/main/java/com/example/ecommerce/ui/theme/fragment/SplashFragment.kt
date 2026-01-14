package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            delay(1500)

            // SAFETY CHECK: If user minimized app, stop here to prevent crash
            if (!isAdded) return@launchWhenStarted

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null) {
                try {
                    val doc = FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .get()
                        .await()

                    val role = doc.getString("role") ?: "USER"

                    // DOUBLE CHECK before navigating
                    if (isAdded) {
                        if (role == "ADMIN") {
                            findNavController().navigate(R.id.action_splashFragment_to_adminDashboardFragment)
                        } else {
                            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                        }
                    }
                } catch (e: Exception) {
                    if (isAdded) {
                        Toast.makeText(context, "Login Error: ${e.message}", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                    }
                }
            } else {
                if (isAdded) {
                    findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
                }
            }
        }
    }
}