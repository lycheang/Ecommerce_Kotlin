package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentSignupBinding
import com.example.ecommerce.ui.theme.viewmodel.AuthViewModel
import com.example.ecommerce.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SignupFragment : Fragment(R.layout.fragment_signup) {

    private lateinit var binding: FragmentSignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignupBinding.bind(view)

        // 1. Handle Signup Button
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.signup(name, email, password)
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Handle "Go to Login" Link (Go Back)
        binding.tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        // 3. Social Placeholders
        binding.btnGoogle.setOnClickListener { Toast.makeText(context, "Google Signup", Toast.LENGTH_SHORT).show() }

        // 4. Observe State


// Observe Signup State
        lifecycleScope.launchWhenStarted {
            viewModel.signupState.collectLatest { resource -> // Use signupState
                when (resource) {
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                        // New users are always "USER", so go to Home
                        findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}