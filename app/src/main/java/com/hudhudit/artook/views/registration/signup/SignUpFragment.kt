package com.hudhudit.artook.views.registration.signup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookSdk
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.modules.user.UserObj
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentSignUpBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    lateinit var binding: FragmentSignUpBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController
    lateinit var userObj: UserObj
    lateinit var mCallbackManager: CallbackManager
    private val GOOGLE_CODE = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_sign_up, container, false)
        binding = FragmentSignUpBinding.inflate(layoutInflater)
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
    }

    private fun init(view: View){
        FacebookSdk.sdkInitialize(registrationActivity)
        navController = Navigation.findNavController(view)
        mCallbackManager = create()
        binding.fbLoginButton.text = ""
        binding.fbLoginButton.setLoginText("")
    }

    private fun onClick(){
        binding.signUpLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.signIn.setOnClickListener { navController.popBackStack() }
        binding.next.setOnClickListener { checkValidation() }
        binding.userNameEdt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.contains(" ")){
                    binding.userNameEdt.error = resources.getString(R.string.username_invalid_format)
                }
            }
        })
        binding.emailAddressEdt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.contains(" ")){
                    binding.emailAddressEdt.error = resources.getString(R.string.email_address_invalid_format)
                }
            }
        })
//        binding.fbLoginButton.setReadPermissions("email", "public_profile")
//        binding.fbLoginButton.setFragment(this)
//        binding.fbLoginButton.registerCallback(
//            mCallbackManager,
//            object : FacebookCallback<LoginResult?> {
//                override fun onCancel() {
//                    Log.d("facebookToken", "cancel")
//                }
//
//                override fun onError(error: FacebookException) {
//                    Log.d("facebookToken", error.message.toString())
//                }
//
//                override fun onSuccess(result: LoginResult?) {
//                    Log.d("facebookToken", result!!.accessToken.token)
//                }
//            })
    }

    private fun checkValidation(){
        val fullName = binding.fullNameEdt.text.toString()
        val userName = binding.userNameEdt.text.toString()
        val emailAddress = binding.emailAddressEdt.text.toString()
        val phoneNumber = binding.phoneNumberEdt.text.toString()
        val password = binding.passwordEdt.text.toString()

        when {
            fullName.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
            }
            userName.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_user_name), Toast.LENGTH_SHORT).show()
            }
            emailAddress.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_email_address), Toast.LENGTH_SHORT).show()
            }
            !emailAddress.contains("@") || !emailAddress.contains(".") -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.invalid_email_address), Toast.LENGTH_SHORT).show()
            }
            phoneNumber.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show()
            }
            !phoneNumber.startsWith("07") || phoneNumber.length != 10 -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            }
            password.length<8 || !isValidPassword(password) -> {
                binding.passwordEdt.error = resources.getString(R.string.password_format)
            }
            else -> {
                val phoneNum = "+962"+phoneNumber.substring(1)
                userObj = UserObj(fullName, userName, emailAddress, phoneNum, password)
                checkUsername()
            }
        }
    }

    private fun checkUsername(){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val checkEmailCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkUserName(userObj.user_name)
        checkEmailCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        checkEmail()
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(registrationActivity, resources.getString(R.string.username_exists), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(registrationActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(registrationActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun checkEmail(){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val checkEmailCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkEmail(userObj.email)
        checkEmailCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        checkPhone()
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(registrationActivity, resources.getString(R.string.email_exists), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(registrationActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(registrationActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun checkPhone(){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val checkEmailCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkPhone(userObj.phone)
        checkEmailCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        navController.navigate(SignUpFragmentDirections.actionSignUpFragmentToSignUpVerificationFragment(userObj))
                    }else{
                        Toast.makeText(registrationActivity, resources.getString(R.string.phone_exists), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(registrationActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(registrationActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{4,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_CODE) {
//            registrationActivity.hideProgressDialog()
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleGoogleSignInResult(task)
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data)
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}