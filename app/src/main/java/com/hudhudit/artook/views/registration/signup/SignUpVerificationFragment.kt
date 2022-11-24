package com.hudhudit.artook.views.registration.signup

import android.content.Context
import android.content.Intent
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.modules.user.UserObj
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentSignUpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SignUpVerificationFragment : Fragment() {

    lateinit var binding: FragmentSignUpVerificationBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController
    lateinit var mAuth: FirebaseAuth
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var user: UserObj
    val args: SignUpVerificationFragmentArgs by navArgs()
    var token = ""
    var code = ""
    var codeBySystem = ""
    var phoneNum = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_sign_up_verification, container, false)
        binding = FragmentSignUpVerificationBinding.inflate(layoutInflater)
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
        user = args.userObj
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
        phoneNum = (user.phone)
        sendVerificationCodeToUser(phoneNum)
    }

    private fun onClick(){
        binding.verificationLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.navigateBack.setOnClickListener { navController.popBackStack() }
        binding.register.setOnClickListener {
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
                register()
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

    private fun register(){
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val registerCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).register(user.name, user.email, phoneNum, user.password, token, "1", "", "", user.user_name)
        registerCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    saveUserToSharedPreferences()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(registrationActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(registrationActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun saveUserToSharedPreferences() {
        val sharedPreferences =
            registrationActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)
        editor.putString(AppDefs.Language_KEY, AppDefs.lang)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.apply()

        val mainIntent = Intent(registrationActivity, MainActivity::class.java)
        startActivity(mainIntent)
        registrationActivity.finish()
    }
}