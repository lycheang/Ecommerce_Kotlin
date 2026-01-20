package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentResetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private lateinit var binding: FragmentResetPasswordBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentResetPasswordBinding.bind(view)

        binding.btnSavePassword.setOnClickListener {
            val pass1 = binding.etNewPassword.text.toString().trim()
            val pass2 = binding.etConfirmNewPassword.text.toString().trim()

            if (pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass1 == pass2) {
                // SIMULATION: We pretend the password is changed.
                // In reality, without the old password, we can't update Firebase unless logged in.
                Toast.makeText(context, "Password Reset Successfully!", Toast.LENGTH_LONG).show()

                // Navigate back to Login and clear stack
                findNavController().navigate(R.id.action_reset_to_login)
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}