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
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.views.registration.RegistrationActivity
import com.hudhudit.artook.databinding.FragmentPhoneNumberBinding
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class PhoneNumberFragment : Fragment() {

    lateinit var binding: FragmentPhoneNumberBinding
    lateinit var registrationActivity: RegistrationActivity
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_phone_number, container, false)
        binding = FragmentPhoneNumberBinding.inflate(layoutInflater)
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
    }

    private fun onClick(){
        if (AppDefs.lang == "ar"){
            binding.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.navigateBack.scaleX = (1).toFloat()
        }
        binding.phoneLayout.setOnClickListener { registrationActivity.hideKeyboard() }
        binding.next.setOnClickListener { checkValidation() }
        binding.navigateBack.setOnClickListener { navController.popBackStack() }
    }

    private fun checkValidation(){
        var phoneNumber = binding.phoneNumberEdt.text.toString()

        when {
            phoneNumber.isEmpty() -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show()
            }
            !phoneNumber.startsWith("07") || phoneNumber.length != 10 -> {
                Toast.makeText(registrationActivity, resources.getString(R.string.invalid_phone_number), Toast.LENGTH_SHORT).show()
            }
            else -> {
                phoneNumber = "+962"+phoneNumber.substring(1)
                checkPhone(phoneNumber)
            }
        }
    }

    private fun checkPhone(phoneNumber: String){
        binding.progressBar.visibility = View.VISIBLE
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val checkEmailCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkPhone(phoneNumber)
        checkEmailCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        Toast.makeText(registrationActivity, resources.getString(R.string.phone_not_exist), Toast.LENGTH_SHORT).show()
                    }else{
                        navController.navigate(PhoneNumberFragmentDirections.actionPhoneNumberFragmentToVerificationFragment(phoneNumber))
                    }
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(registrationActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(registrationActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

}