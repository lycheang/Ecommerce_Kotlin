package com.example.ecommerce.ui.theme.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.ecommerce.R
import com.example.ecommerce.databinding.FragmentOtpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {

    private lateinit var binding: FragmentOtpBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOtpBinding.bind(view)

        binding.btnVerifyOtp.setOnClickListener {
            val code = binding.etOtpInput.text.toString().trim()

            // CHECK: Match the "Demo" code we showed in the previous Toast
            if (code == "1234") {
                Toast.makeText(context, "Verified Successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_otp_to_resetPass)
            } else {
                Toast.makeText(context, "Invalid Code. Try 1234", Toast.LENGTH_SHORT).show()
            }
        }
    }
}