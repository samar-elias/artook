package com.hudhudit.artook.views.main.profile.edit

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.helpers.LocaleHelper
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentEditProfileBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.splash.SplashActivity
import com.vansuita.pickimage.bean.PickResult
import com.vansuita.pickimage.bundle.PickSetup
import com.vansuita.pickimage.dialog.PickImageDialog
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Matcher
import java.util.regex.Pattern

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    lateinit var binding: FragmentEditProfileBinding
    lateinit var mainActivity: MainActivity
    private val REQUEST_CODE = 100
    private val REQUEST_IMAGE_GALLERY = 101
    var imagePath = ""
    var lang = "en"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        binding = FragmentEditProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lang = AppDefs.lang!!
        onClick()
        setData()
        mainActivity.visibleBottomBar()
    }

    private fun onClick(){
        if (AppDefs.lang == "ar"){
            binding.navigateBack.scaleX = (-1).toFloat()
        }else{
            binding.navigateBack.scaleX = (1).toFloat()
        }
        binding.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
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
        binding.changePicture.setOnClickListener { pickImage() }
        binding.changePassword.setOnClickListener { changePasswordPopUp() }
        binding.logout.setOnClickListener { logoutPopUp() }
        binding.language.setOnClickListener { changeLanguagePopUp() }
    }

    private fun setData(){
        binding.fullNameEdt.setText(AppDefs.user.results!!.name)
        binding.userNameEdt.setText(AppDefs.user.results!!.user_name)
        binding.emailAddressEdt.setText(AppDefs.user.results!!.email)
        binding.phoneNumberEdt.setText(AppDefs.user.results!!.phone)
        binding.bioEdt.setText(AppDefs.user.results!!.bio)
        if (!AppDefs.user.results!!.image.isNullOrEmpty()){
            Glide.with(mainActivity).load(AppDefs.user.results!!.image).into(binding.profileImg)
        }
        binding.update.setOnClickListener { checkValidation() }
    }
    
    private fun checkValidation(){
        val fullName = binding.fullNameEdt.text.toString()
        val userName = binding.userNameEdt.text.toString()
        val emailAddress = binding.emailAddressEdt.text.toString()
        val phoneNumber = binding.phoneNumberEdt.text.toString()
        val bio = binding.bioEdt.text.toString()

        when {
            fullName.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
            }
            userName.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_user_name), Toast.LENGTH_SHORT).show()
            }
            phoneNumber.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_phone_number), Toast.LENGTH_SHORT).show()
            }
            else -> {
                updateProfile(fullName, userName, emailAddress, phoneNumber, bio)
            }
        }
    }

    private fun checkUsername(fullName: String, userName: String, emailAddress: String, phoneNumber: String, bio: String){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    builder.header("Lang", AppDefs.lang!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val checkEmailCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).checkUserNameUpdate(userName)
        checkEmailCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    if (response.body()!!.results!!){
                        updateProfile(fullName, userName, emailAddress, phoneNumber, bio)
                    }else{
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(mainActivity, resources.getString(R.string.username_exists), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    binding.progressBar.visibility = View.GONE
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun changePasswordPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.change_password_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val currentPasswordEdt: TextInputEditText = alertView.findViewById(R.id.current_password_edt)
        val newPasswordEdt: TextInputEditText = alertView.findViewById(R.id.new_password_edt)
        val confirmPasswordEdt: TextInputEditText = alertView.findViewById(R.id.confirm_password_edt)
        val changePasswordBtn: MaterialButton = alertView.findViewById(R.id.change_password)
        val close: ImageView = alertView.findViewById(R.id.close)

        close.setOnClickListener { alertBuilder.dismiss() }
        changePasswordBtn.setOnClickListener {
            val currentPassword = currentPasswordEdt.text.toString()
            val newPassword = newPasswordEdt.text.toString()
            val confirmPassword = confirmPasswordEdt.text.toString()

            when {
                newPassword.isEmpty() -> {
                    Toast.makeText(mainActivity, resources.getString(R.string.enter_current_password), Toast.LENGTH_SHORT).show()
                }
                newPassword.isEmpty() -> {
                    Toast.makeText(mainActivity, resources.getString(R.string.enter_new_password), Toast.LENGTH_SHORT).show()
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(mainActivity, resources.getString(R.string.enter_confirm_password), Toast.LENGTH_SHORT).show()
                }
                currentPassword != AppDefs.user.results!!.password -> {
                    Toast.makeText(mainActivity, resources.getString(R.string.wrong_current_password), Toast.LENGTH_SHORT).show()
                }
                newPassword != confirmPassword -> {
                    Toast.makeText(mainActivity, resources.getString(R.string.match_passwords), Toast.LENGTH_SHORT).show()
                }
                newPassword.length<8 || !isValidPassword(newPassword) -> {
                    newPasswordEdt.error = resources.getString(R.string.password_format)
                }
                confirmPassword.length<8 || !isValidPassword(confirmPassword) -> {
                    confirmPasswordEdt.error = resources.getString(R.string.password_format)
                }
                else -> {
                    changePassword(newPassword)
                    alertBuilder.dismiss()
                }
            }


        }
    }

    private fun changePassword(password: String){
        binding.progressBar.visibility = View.VISIBLE
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    builder.header("Lang", AppDefs.lang!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val changePasswordCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).changePassword(password)
        changePasswordCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.password_updated), Toast.LENGTH_SHORT).show()
                    mainActivity.supportFragmentManager.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()

            }

        })
    }

    private fun updateProfile(fullName: String, userName: String, emailAddress: String, phoneNumber: String, bio: String){
        binding.progressBar.visibility = View.VISIBLE
        val fName: RequestBody = mainActivity.createRequestBody(fullName)!!
        val username: RequestBody = mainActivity.createRequestBody(userName)!!
        val email: RequestBody = mainActivity.createRequestBody(emailAddress)!!
        val phone: RequestBody = mainActivity.createRequestBody(phoneNumber)!!
        val bioText: RequestBody = mainActivity.createRequestBody(bio)!!
        var image: MultipartBody.Part? = null
        if (imagePath.isNotEmpty()){
            image = mainActivity.createMultipartBodyPart("image", imagePath)!!
        }
        val okHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(
                Interceptor { chain ->
                    val builder = chain.request().newBuilder()
                    builder.header("Content-Type", "application/json; charset=UTF-8")
                    builder.header("Authorization", AppDefs.user.token!!)
                    return@Interceptor chain.proceed(builder.build())
                }
            )
        }.build()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
        val updateProfileCall: Call<UserData> =
            retrofit.create(RetrofitAPIs::class.java).updateProfile(fName, email, phone, image, bioText)
        updateProfileCall.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    AppDefs.user = response.body()!!
                    saveUserToSharedPreferences(false)
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun pickImage() {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_CODE
            )
        } else {
            PickImageDialog.build(PickSetup()).setOnPickResult { r: PickResult ->
                binding.profileImg.setImageBitmap(r.bitmap)
                imagePath = r.path
            }.show(mainActivity)
        }
    }

    private fun logoutPopUp(){
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.logout_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val close: ImageView = alertView.findViewById(R.id.close)
        val logout: MaterialButton = alertView.findViewById(R.id.logout)

        close.setOnClickListener { alertBuilder.dismiss() }
        logout.setOnClickListener {
            alertBuilder.dismiss()
            val preferences: SharedPreferences = mainActivity.getSharedPreferences(
                AppDefs.SHARED_PREF_KEY,
                Context.MODE_PRIVATE
            )
            val editor = preferences.edit()
            editor.clear()
            editor.apply()
            val splashIntent = Intent(mainActivity, SplashActivity::class.java)
            startActivity(splashIntent)
            mainActivity.finish()
        }

    }

    private fun saveUserToSharedPreferences(isRefresh: Boolean) {
        val sharedPreferences =
            mainActivity.getSharedPreferences(AppDefs.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(AppDefs.ID_KEY, AppDefs.user.results!!.id)
        editor.putString(AppDefs.Language_KEY, AppDefs.lang)

        val gson = Gson()
        val json = gson.toJson(AppDefs.user)
        editor.putString(AppDefs.USER_KEY, json)
        editor.apply()
        if (isRefresh){
            val intent = Intent(mainActivity, SplashActivity:: class.java)
            startActivity(intent)
            mainActivity.finish()
        }else{
            mainActivity.supportFragmentManager.popBackStack()
        }

    }

    private fun changeLanguagePopUp() {
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.change_language_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val english: MaterialCardView = alertView.findViewById(R.id.english)
        val arabic: MaterialCardView = alertView.findViewById(R.id.arabic)
        val close: ImageView = alertView.findViewById(R.id.close)

        english.setOnClickListener {
            lang = "en"
            AppDefs.lang = lang
            LocaleHelper.setAppLocale(AppDefs.lang, mainActivity)
            saveUserToSharedPreferences(true)
            alertBuilder.dismiss()
        }
        arabic.setOnClickListener {
            lang = "ar"
            AppDefs.lang = lang
            LocaleHelper.setAppLocale(AppDefs.lang, mainActivity)
            saveUserToSharedPreferences(true)
            alertBuilder.dismiss()
        }
        close.setOnClickListener { alertBuilder.dismiss() }

    }

    private fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{4,}$"
        pattern = Pattern.compile(passwordPattern)
        val matcher: Matcher = pattern.matcher(password)
        return matcher.matches()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data!!.data
        imagePath = mainActivity.fileUriToBase64(uri!!, mainActivity.contentResolver)!!
        imagePath = mainActivity.compress(imagePath)!!
    }
}