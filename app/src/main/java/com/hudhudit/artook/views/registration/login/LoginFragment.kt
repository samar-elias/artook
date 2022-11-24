package com.hudhudit.artook.views.registration.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController
    private val GOOGLE_CODE = 0
    lateinit var googleSignInClient: GoogleSignInClient
    var token = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_login, container, false)
        binding = FragmentLoginBinding.inflate(layoutInflater)
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
        FirebaseApp.initializeApp(registrationActivity)
        init(view)
        onClick()
        getToken()
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.google_client_id))
//            .requestEmail()
//            .build()
//        googleSignInClient = GoogleSignIn.getClient(registrationActivity, gso)
    }

    private fun onClick(){
        binding.loginLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.enter.setOnClickListener { checkValidation() }
        binding.signUp.setOnClickListener { navController.navigate(LoginFragmentDirections.actionLoginFragmentToSignUpFragment()) }
        binding.forgotPassword.setOnClickListener { navController.navigate(LoginFragmentDirections.actionLoginFragmentToPhoneNumberFragment()) }
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

    private fun checkValidation(){
        val emailAddress = binding.emailAddressEdt.text.toString()
        val password = binding.passwordEdt.text.toString()

        when {
            emailAddress.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_email_address), Toast.LENGTH_SHORT).show()
            }
            !emailAddress.contains("@") || !emailAddress.contains(".") -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            }
            else -> {
                login(emailAddress, password)
            }
        }
    }

    private fun login(emailAddress: String, password: String){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val loginCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).logIn(emailAddress, password, token, "1")
        loginCall.enqueue(object : Callback<UserData> {
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

//    private fun signUpGoogle() {
//        val googleIntent: Intent = googleSignInClient.getSignInIntent()
//        startActivityForResult(
//            googleIntent, GOOGLE_CODE
//        )
//    }

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