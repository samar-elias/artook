package com.hudhudit.artook.views.registration.forgotpassword

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.messaging.FirebaseMessaging
import com.hudhudit.artook.R
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VerificationFragment : Fragment() {

    lateinit var binding: FragmentVerificationBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController
    lateinit var mAuth: FirebaseAuth
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    val args: VerificationFragmentArgs by navArgs()
    var token = ""
    var code = ""
    var codeBySystem = ""
    var phoneNum = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_verification, container, false)
        binding = FragmentVerificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RegistrationActivity) {
            registrationActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        onClick()
        getToken()
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        phoneNum = args.phoneNumber
        mAuth = FirebaseAuth.getInstance()
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                codeBySystem = s
            }

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    binding.verificationEdt.setText(code)
                    verifyCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }
        sendVerificationCodeToUser(phoneNum)
    }

    private fun onClick(){
        binding.verificationLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.next.setOnClickListener {
            if (binding.verificationEdt.text.toString().isNotEmpty()){
                code = binding.verificationEdt.text.toString()
                verifyCode(code)
            }else{
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_verification_code), Toast.LENGTH_SHORT).show()
            }
        }
        binding.resendCode.setOnClickListener {
            sendVerificationCodeToUser(phoneNum)
            Toast.makeText(registrationActivity, resources.getString(R.string.code_sent_successfully), Toast.LENGTH_SHORT).show()
        }
        binding.navigateBack.setOnClickListener { navController.popBackStack() }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    Log.w(
                        "FAILED",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@addOnCompleteListener
                }
                token = task.result!!
            }
    }

    private fun verifyCode(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = PhoneAuthProvider.getCredential(codeBySystem, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(
            registrationActivity
        ) { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, resources.getString(R.string.verification_completed), Toast.LENGTH_SHORT).show()
                navController.navigate(VerificationFragmentDirections.actionVerificationFragmentToResetPasswordFragment(phoneNum))
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        context,
                        resources.getString(R.string.wrong_code),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun sendVerificationCodeToUser(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(2L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(registrationActivity) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

}