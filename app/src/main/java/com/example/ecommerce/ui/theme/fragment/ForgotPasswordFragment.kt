package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private lateinit var binding: FragmentForgotPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForgotPasswordBinding.bind(view)

        binding.btnSendOtp.setOnClickListener {
            val email = binding.etForgotEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // SIMULATION: In a real app, you would call API here.
                // For demo, we just show the user the "Fake" OTP code.
                Toast.makeText(context, "Demo OTP Sent: 1234", Toast.LENGTH_LONG).show()

                // Navigate to next screen
                findNavController().navigate(R.id.action_forgot_to_otp)
            } else {
                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}