package com.hudhudit.artook.views.main.newpost

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media1Uri
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media2Uri
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media3Uri
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media4Uri
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media5Uri
import com.hudhudit.artook.apputils.appdefs.AppDefs.Companion.media6Uri
import com.hudhudit.artook.databinding.FragmentNewPostBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.profile.userprofile.UserProfileFragment
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.*

@AndroidEntryPoint
class UploadMediaNewPostFragment : Fragment() {

    lateinit var binding: FragmentNewPostBinding
    lateinit var mainActivity: MainActivity
    private val REQUEST_IMAGE_GALLERY = 101
    private val REQUEST_TAKE_GALLERY_VIDEO = 100
    val images: ArrayList<String> = ArrayList()
    val videos: ArrayList<String> = ArrayList()
    val mediaUris: ArrayList<Uri> = ArrayList()
    lateinit var media1Type: String
    lateinit var media2Type: String
    lateinit var media3Type: String
    lateinit var media4Type: String
    lateinit var media5Type: String
    lateinit var media6Type: String
    var current = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_upload_media_new_post, container, false)
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
        setData()
        mainActivity.invisibleBottomBar()
    }

    private fun onClick(){
        binding.close.setOnClickListener {
            media1Uri = null
            media2Uri = null
            media3Uri = null
            media4Uri = null
            media5Uri = null
            media6Uri = null
            mainActivity.supportFragmentManager.popBackStack()
        }
        binding.media1CV.setOnClickListener {
            pickImagePopUp()
            current = 1
        }
        binding.media2CV.setOnClickListener {
            pickImagePopUp()
            current = 2
        }
        binding.media3CV.setOnClickListener {
            pickImagePopUp()
            current = 3
        }
        binding.media4CV.setOnClickListener {
            pickImagePopUp()
            current = 4
        }
        binding.media5CV.setOnClickListener {
            pickImagePopUp()
            current = 5
        }
        binding.media6CV.setOnClickListener {
            pickImagePopUp()
            current = 6
        }
        binding.next.setOnClickListener { fillUris() }
    }

    private fun setData(){
        if (media1Uri != null){
            Glide.with(mainActivity).load(media1Uri).into(binding.media1)
        }
        if (media2Uri != null){
            Glide.with(mainActivity).load(media2Uri).into(binding.media2)
        }
        if (media3Uri != null){
            Glide.with(mainActivity).load(media3Uri).into(binding.media3)
        }
        if (media4Uri != null){
            Glide.with(mainActivity).load(media4Uri).into(binding.media4)
        }
        if (media5Uri != null){
            Glide.with(mainActivity).load(media5Uri).into(binding.media5)
        }
        if (media6Uri != null){
            Glide.with(mainActivity).load(media6Uri).into(binding.media6)
        }
    }

    private fun pickImagePopUp(){
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
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
            alertBuilder.dismiss()
        }
        videos.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
            alertBuilder.dismiss()
        }
        close.setOnClickListener { alertBuilder.dismiss() }

    }

    private fun fillUris(){
        mediaUris.clear()
        images.clear()
        videos.clear()
        if (media1Uri != null){
            mediaUris.add(media1Uri!!)
        }
        if (media2Uri != null){
            mediaUris.add(media2Uri!!)
        }
        if (media3Uri != null){
            mediaUris.add(media3Uri!!)
        }
        if (media4Uri != null){
            mediaUris.add(media4Uri!!)
        }
        if (media5Uri != null){
            mediaUris.add(media5Uri!!)
        }
        if (media6Uri != null){
            mediaUris.add(media6Uri!!)
        }

        if (mediaUris.size == 0){
            Toast.makeText(mainActivity, resources.getString(R.string.add_media), Toast.LENGTH_SHORT).show()
        }else{
            if (media1Uri != null){
                if (media1Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media1Uri!!, mainActivity.contentResolver)!!)
                }else if (media1Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media1Uri!!, mainActivity.contentResolver)!!)
                }
            }
            if (media2Uri != null){
                if (media2Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media2Uri!!, mainActivity.contentResolver)!!)
                }else if (media2Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media2Uri!!, mainActivity.contentResolver)!!)
                }
            }
            if (media3Uri != null){
                if (media3Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media3Uri!!, mainActivity.contentResolver)!!)
                }else if (media3Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media3Uri!!, mainActivity.contentResolver)!!)
                }
            }
            if (media4Uri != null){
                if (media4Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media4Uri!!, mainActivity.contentResolver)!!)
                }else if (media4Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media4Uri!!, mainActivity.contentResolver)!!)
                }
            }
            if (media5Uri != null){
                if (media5Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media5Uri!!, mainActivity.contentResolver)!!)
                }else if (media5Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media5Uri!!, mainActivity.contentResolver)!!)
                }
            }
            if (media6Uri != null){
                if (media6Type == "0"){
                    images.add(mainActivity.fileUriToBase64(media6Uri!!, mainActivity.contentResolver)!!)
                }else if (media6Type == "1"){
                    videos.add(mainActivity.fileUriToBase64(media6Uri!!, mainActivity.contentResolver)!!)
                }
            }
            navigateToMyPostDetails()
        }
    }

    private fun navigateToMyPostDetails(){
        val fragment: Fragment = NewPostDetailsFragment()
        val fragmentManager: FragmentManager = mainActivity.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putStringArrayList("images", images)
        bundle.putStringArrayList("videos", videos)
        bundle.putParcelableArrayList("uris", mediaUris)
        fragment.arguments = bundle
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack("PostDetails")
        fragmentTransaction.commit()
    }

//    private fun createPost(){
//        val okHttpClient = OkHttpClient.Builder().apply {
//            addInterceptor(
//                Interceptor { chain ->
//                    val builder = chain.request().newBuilder()
//                    builder.header("Content-Type", "application/json; charset=UTF-8")
//                    builder.header("Authorization", AppDefs.user.token!!)
//                    return@Interceptor chain.proceed(builder.build())
//                }
//            )
//        }.build()
//        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Urls.BASE_URL).client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create()).build()
//        val updateProfileCall: Call<BooleanResponse> =
//            retrofit.create(RetrofitAPIs::class.java).createPost("1", "test", image1Path)
//        updateProfileCall.enqueue(object : Callback<BooleanResponse> {
//            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
//                if (response.isSuccessful){
//                    Toast.makeText(mainActivity, "Done", Toast.LENGTH_SHORT).show()
//                }else{
//                    val gson = Gson()
//                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
//                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
//
//                }
//            }
//
//            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
//                Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null){
            val uri = data.data
            if (requestCode == REQUEST_IMAGE_GALLERY){
                images.add(mainActivity.fileUriToBase64(uri!!, mainActivity.contentResolver)!!)
            }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                videos.add(mainActivity.fileUriToBase64(uri!!, mainActivity.contentResolver)!!)
            }
//            mediaUris.add(uri!!)
            when (current){
                1 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media1)
                    media1Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media1Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media1Type = "1"
                    }
                }
                2 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media2)
                    media2Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media2Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media2Type = "1"
                    }
                }
                3 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media3)
                    media3Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media3Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media3Type = "1"
                    }
                }
                4 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media4)
                    media4Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media4Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media4Type = "1"
                    }
                }
                5 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media5)
                    media5Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media5Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media5Type = "1"
                    }
                }
                6 -> {
                    Glide.with(mainActivity).load(uri).into(binding.media6)
                    media6Uri = uri!!
                    if (requestCode == REQUEST_IMAGE_GALLERY){
                        media6Type = "0"
                    }else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO){
                        media6Type = "1"
                    }
                }
            }

//        image1Path = mainActivity.fileUriToBase64(uri!!, mainActivity.contentResolver)!!
//        image1Path = FileUriUtils.getRealPath(mainActivity, uri!!)!!
//            media1Path = FileUriUtils.getRealPath(mainActivity, uri!!)!!
//        image1Path = mainActivity.encoder(image1Path)!!
//            image1Path = mainActivity.compress(image1Path)!!
//        image1Path = mainActivity.compressImage(uri.toString())!!
//        image1Path = mainActivity.encoder(image1Path)!!
//        Glide.with(mainActivity).load(image1Path).into(binding.media1)
        }
    }

}