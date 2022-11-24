package com.hudhudit.artook.views.registration.forgotpassword

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentResetPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    lateinit var binding: FragmentResetPasswordBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController
    val args: ResetPasswordFragmentArgs by navArgs()
    var phone = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_reset_password, container, false)
        binding = FragmentResetPasswordBinding.inflate(layoutInflater)
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
        navController = Navigation.findNavController(view)
        phone = args.phoneNumber
    }

    private fun onClick(){
        binding.resetPasswordLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.change.setOnClickListener { checkValidation() }
        binding.navigateBack.setOnClickListener { navController.popBackStack() }
    }

    private fun checkValidation(){
        val password = binding.passwordEdt.text.toString()
        val confirmPassword = binding.confirmPasswordEdt.text.toString()

        when {
            password.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_password), Toast.LENGTH_SHORT).show()
            }
            confirmPassword.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_confirm_password), Toast.LENGTH_SHORT).show()
            }
            password != confirmPassword -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.match_passwords), Toast.LENGTH_SHORT).show()
            }
            password.length<8 || !isValidPassword(password) -> {
                binding.passwordEdt.error = resources.getString(R.string.password_format)
            }
            else -> {
                resetPassword(password)
            }
        }
    }

    private fun resetPassword(password: String){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val resetPasswordCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).resetPassword(phone, password)
        resetPasswordCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        navController.navigate(ResetPasswordFragmentDirections.actionResetPasswordFragmentToLoginFragment())
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
}