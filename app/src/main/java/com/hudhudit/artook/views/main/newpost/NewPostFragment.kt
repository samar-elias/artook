package com.hudhudit.artook.views.main.newpost

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.appdefs.createMultipartBodyPart
import com.hudhudit.artook.apputils.appdefs.createRequestBody
import com.hudhudit.artook.apputils.helpers.PathUtil.getPath
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentNewPostBinding
import com.hudhudit.artook.views.main.MainActivity
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream


@AndroidEntryPoint
class NewPostFragment : Fragment() {

    lateinit var binding: FragmentNewPostBinding
    lateinit var mainActivity: MainActivity
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_TAKE_GALLERY_VIDEO = 100
    var image1Path = ""
    var image2Path = ""
    var image3Path = ""
    var image4Path = ""
    var image5Path = ""
    var image6Path = ""
    var current = 1
    lateinit var image1: Bitmap
    lateinit var image2: Bitmap
    lateinit var image3: Bitmap
    lateinit var image4: Bitmap
    lateinit var image5: Bitmap
    lateinit var image6: Bitmap
    var mediaPath=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_new_post, container, false)
        binding = FragmentNewPostBinding.inflate(layoutInflater)
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
        onClick()
    }

    private fun onClick() {
        binding.close.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.media1CV.setOnClickListener { pickImagePopUp() }
        binding.next.setOnClickListener {  }
    }

    private fun pickImagePopUp() {
        val alertView: View =
            LayoutInflater.from(context).inflate(R.layout.pick_post_popup, null)
        val alertBuilder = AlertDialog.Builder(context).setView(alertView).show()
        alertBuilder.show()
        alertBuilder.setCancelable(false)

        alertBuilder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val images: MaterialCardView = alertView.findViewById(R.id.images)
        val videos: MaterialCardView = alertView.findViewById(R.id.videos)
        val close: ImageView = alertView.findViewById(R.id.close)

        images.setOnClickListener {
            val intent = CropImage.activity().getIntent(requireActivity())
            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
            alertBuilder.dismiss()
//            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
//                .crop(40F, 40F)
//                .compress(1024)
//                .maxResultSize(40, 40)
//                .start(REQUEST_IMAGE_GALLERY)
        }
        videos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
            alertBuilder.dismiss()
        }
        close.setOnClickListener { alertBuilder.dismiss() }

    }

    private fun openImages() {

    }

    private fun openVideos() {

    }

    private fun createPost( uri: Uri) {
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
        val category_id = createRequestBody("1")
        val describtion = createRequestBody("test")
        val image: MultipartBody.Part? = createMultipartBodyPart(
            "ProfilePictureFile",
            getPath(requireContext(), uri)!!
        )
        var x = mutableListOf<MultipartBody.Part>()
        var z= Array<MultipartBody.Part?>(1,{image})

        val updateProfileCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).createPost(category_id, describtion, z!!)

        updateProfileCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(
                call: Call<BooleanResponse>,
                response: Response<BooleanResponse>
            ) {
                Log.d("error",x.toString())
                if (response.isSuccessful) {
                    Toast.makeText(mainActivity, "add sussess", Toast.LENGTH_SHORT).show()
                } else {
                    val gson = Gson()
                    val type = object :
                        TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(
                        response.errorBody()!!.charStream(),
                        type
                    ) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    //    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("error", response.toString())
                    Log.d("myLogs", response.toString());
                    Log.d("myLogs",response.errorBody().toString());
                    Log.d("myLogs", response.code().toString());
                    Log.d("myLogs", response.toString());


                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Log.d("error", t.toString())
                Toast.makeText(
                    mainActivity,
                    resources.getString(R.string.internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
       if (data != null) {
            val uri = data!!.data

            Glide.with(mainActivity).load(uri).into(binding.media1)
//        image1Path = mainActivity.fileUriToBase64(uri!!, mainActivity.contentResolver)!!
//        image1Path = FileUriUtils.getRealPath(mainActivity, uri!!)!!
//            image1Path = FileUriUtils.getRealPath(mainActivity, uri!!)!!
//        image1Path = mainActivity.encoder(image1Path)!!

      //      image1Path = mainActivity.compress(image1Path)!!
//        image1Path = mainActivity.compressImage(uri.toString())!!
//        image1Path = mainActivity.encoder(image1Path)!!
//        Glide.with(mainActivity).load(image1Path).into(binding.media1)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri: Uri = result.uri
                Log.d("myimage",resultUri!!.toString())


                //  addAttachmentImage(resultUri)
                //image2Path= resultUri!!

               // var mymulti=postImage(list)
               createPost(resultUri!!)






// https://httpbin.org/post
// "foo=foo&bar=bar"


            }


        }



    }


    private fun postImage(selectedUris: java.util.ArrayList<String>):
            ArrayList<MultipartBody.Part>  {
        var multiParts: ArrayList<MultipartBody.Part> = ArrayList<MultipartBody.Part>()
        for (i in 0 until selectedUris.size) {
            // 1. Create File using image url (String)
            val file = File(selectedUris.get(i))
            // 2. Create requestBody by using multipart/form-data MediaType from file
            val requestFile: RequestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            // 3. Finally, Create MultipartBody using MultipartBody.Part.createFormData
            val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image", file.name.trim(), requestFile)
            multiParts.add(body)
        }
        return multiParts
    }

}