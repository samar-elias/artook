package com.hudhudit.artook.views.main.competition.participate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.databinding.FragmentParticipateBinding
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

@AndroidEntryPoint
class ParticipateFragment : Fragment() {

    lateinit var binding: FragmentParticipateBinding
    lateinit var mainActivity: MainActivity
    private val REQUEST_CODE = 100
    var imagePath = ""
    var contestId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_participate, container, false)
        binding = FragmentParticipateBinding.inflate(layoutInflater)
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
        contestId = requireArguments().getString("contestId")!!
        onClick()
    }

    private fun onClick(){
        binding.close.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.imageCV.setOnClickListener { pickImage() }
        binding.submitParticipation.setOnClickListener { checkValidation() }
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
                binding.img.setImageBitmap(r.bitmap)
                binding.cameraImg.visibility = View.GONE
                imagePath = r.path
            }.show(mainActivity)
        }
    }

    private fun checkValidation(){
        val description = binding.descriptionEdt.text.toString()
        when {
            imagePath.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_image), Toast.LENGTH_SHORT).show()
            }
            description.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_description), Toast.LENGTH_SHORT).show()
            }
            else -> {
                participate(description)
            }
        }
    }

    private fun participate(desc: String){
        binding.progressBar.visibility = View.VISIBLE
        val description: RequestBody = mainActivity.createRequestBody(desc)!!
        val id: RequestBody = mainActivity.createRequestBody(contestId)!!
        val image: MultipartBody.Part = mainActivity.createMultipartBodyPart("image", imagePath)!!
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
        val participateCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).participate(id, description, image)
        participateCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.participation_submitted), Toast.LENGTH_SHORT).show()
                    mainActivity.supportFragmentManager.popBackStack()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, resources.getString(R.string.already_participated), Toast.LENGTH_SHORT).show()
//                    mainActivity.supportFragmentManager.popBackStack()
                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                Toast.makeText(mainActivity, resources.getString(R.string.already_participated), Toast.LENGTH_SHORT).show()
//                mainActivity.supportFragmentManager.popBackStack()
                Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }
}