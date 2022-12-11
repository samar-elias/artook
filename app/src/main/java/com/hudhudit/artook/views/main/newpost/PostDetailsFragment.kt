package com.hudhudit.artook.views.main.newpost

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.appdefs.createMultipartBodyPart
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.modules.post.Image
import com.hudhudit.artook.apputils.modules.post.NewPostMedia
import com.hudhudit.artook.apputils.modules.post.UploadedFile
import com.hudhudit.artook.apputils.modules.post.Video
import com.hudhudit.artook.apputils.modules.search.Categories
import com.hudhudit.artook.apputils.modules.search.Category
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentPostDetailsBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.newpost.Adapter.MediaAdapter
import com.hudhudit.artook.views.splash.SplashActivity
import kotlinx.android.synthetic.main.media_layout.*
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*

class PostDetailsFragment : Fragment() {

    lateinit var binding: FragmentPostDetailsBinding
    lateinit var mainActivity: MainActivity
    var images: ArrayList<Image> = ArrayList()
    var videos: ArrayList<Video> = ArrayList()
    var mediaUris: ArrayList<NewPostMedia> = ArrayList()
    var categories: ArrayList<Category> = ArrayList()
    var paths: ArrayList<String> = ArrayList()
    var categoryId = ""
    var description = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_post_details, container, false)
        binding = FragmentPostDetailsBinding.inflate(layoutInflater)
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
        init()
        onClick()
        getCategories()
        mainActivity.invisibleBottomBar()
    }

    private fun init(){
//        images = requireArguments().getStringArrayList("images")!!
//        videos = requireArguments().getStringArrayList("videos")!!
        mediaUris = requireArguments().getParcelableArrayList("uris")!!
        setData()
    }

    private fun onClick(){
        binding.toolbarLayout.navigateBack.setOnClickListener { mainActivity.supportFragmentManager.popBackStack() }
        binding.share.setOnClickListener { checkValidation() }
        binding.categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                categoryId = categories[i].id
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }

    }

    private fun setData(){
        val adapter = MediaAdapter(this, mediaUris)
        binding.mediaRV.adapter = adapter
        binding.mediaRV.layoutManager = GridLayoutManager(mainActivity, 2)
    }

    fun deleteUri(mediaUri: NewPostMedia){
        mediaUris.remove(mediaUri)
        if (mediaUris.size == 0){
            mainActivity.supportFragmentManager.popBackStack()
        }else{
            setData()
        }
    }

    private fun getCategories(){
        categories.clear()
        categories.add(Category("0", resources.getString(R.string.select_category), "0"))
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
        val categoriesCall: Call<Categories> =
            retrofit.create(RetrofitAPIs::class.java).getCategories()
        categoriesCall.enqueue(object : Callback<Categories> {
            override fun onResponse(call: Call<Categories>, response: Response<Categories>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    for (category in response.body()!!.results){
                        categories.add(category)
                    }
                    setSpinner(binding.categoriesSpinner, categories)
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Categories>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun checkValidation(){
        description = binding.descriptionEdt.text.toString()
        when {
            categoryId == "0" -> {
                Toast.makeText(mainActivity, resources.getString(R.string.select_a_category), Toast.LENGTH_SHORT).show()
            }
            description!!.isEmpty() -> {
                Toast.makeText(mainActivity, resources.getString(R.string.enter_description), Toast.LENGTH_SHORT).show()
            }
            else -> {
//                fillUris()
                getRealPaths()
            }
        }
    }

    private fun getRealPaths(){
        for (uri in mediaUris){
            val path = mainActivity.getPath(uri.mediaUri)
            paths.add(path!!)
        }
        createPost()
    }

    private fun createPost(){
        binding.progressBar.visibility = View.VISIBLE
        val catId: RequestBody = mainActivity.createRequestBody(categoryId)!!
        val desc: RequestBody = mainActivity.createRequestBody(description)!!
        val images: ArrayList<MultipartBody.Part> = ArrayList()
        for (path in paths) {
            images.add(createMultipartBodyPart("files[]", path)!!)
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
        val updateProfileCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).createPost(catId, desc, images)
        updateProfileCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.post_created), Toast.LENGTH_SHORT).show()
                    val intent = Intent(mainActivity, MainActivity::class.java)
                    startActivity(intent)
                    mainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun fillUris(){
        images.clear()
        videos.clear()

        if (mediaUris.size == 0){
            Toast.makeText(mainActivity, resources.getString(R.string.add_media), Toast.LENGTH_SHORT).show()
        }else{
            for (uri in mediaUris){
                if (uri.mediaType == "0"){
                    val image = Image(mainActivity.fileUriToBase64(uri.mediaUri, mainActivity.contentResolver)!!, uri.ext)
                    images.add(image)
                }else if (uri.mediaType == "1"){
                    val video = Video(mainActivity.fileUriToBase64(uri.mediaUri, mainActivity.contentResolver)!!, uri.ext)
                    videos.add(video)
                }
            }
            createJson()
        }
    }

    private fun createJson(){
        val imagesArray = JSONArray()
        for (image in images){
            val imageObj = JSONObject()
            imageObj.put("image", image.image)
            imageObj.put("type", image.type)
            imagesArray.put(imageObj)
        }
        val videosArray = JSONArray()
        for (video in videos){
            val videoObj = JSONObject()
            videoObj.put("video", video.video)
            videoObj.put("type", video.type)
            videosArray.put(videoObj)
        }
        val mediaObj = JSONObject()
        mediaObj.put("images", imagesArray)
        mediaObj.put("videos", videosArray)
        Log.d("mediaObj", mediaObj.toString())

        var tempFile: File? = null
        var writer: BufferedWriter? = null

        tempFile = File.createTempFile("TempFile", ".txt")
        writer = BufferedWriter(
            FileWriter(tempFile)
        )
        writer.write(
            mediaObj.toString()
        )
        println("Temp file location: "
                + tempFile.absoluteFile
        )

        val path: String = tempFile.path

        uploadFile(path)

//
//        val fileReader = FileReader(path)
//        val bufferedReader = BufferedReader(fileReader)
//        var buffer: String?
//        val stringBuilder = StringBuilder()
//
//        while (bufferedReader.readLine().also { buffer = it } != null) {
//            stringBuilder.append(buffer)
//        }
//
//        deleteTempFiles(mainActivity.cacheDir)
//
//        while (bufferedReader.readLine().also { buffer = it } != null) {
//            stringBuilder.append(buffer)
//        }
    }

    private fun uploadFile(file: String){
        binding.progressBar.visibility = View.VISIBLE
        var image: MultipartBody.Part? = null
        if (file.isNotEmpty()){
            image = mainActivity.createMultipartBodyPart("image", file)!!
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
        val updateProfileCall: Call<UploadedFile> =
            retrofit.create(RetrofitAPIs::class.java).uploadFile(image)
        updateProfileCall.enqueue(object : Callback<UploadedFile> {
            override fun onResponse(call: Call<UploadedFile>, response: Response<UploadedFile>) {
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    createPost(response.body()!!.results)
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<UserData>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<UserData>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<UploadedFile>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun createPost(file: String){

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
        val updateProfileCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).createPost(categoryId, description, file)
        updateProfileCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){
                    Toast.makeText(mainActivity, resources.getString(R.string.post_created), Toast.LENGTH_SHORT).show()
                    val intent = Intent(mainActivity, SplashActivity::class.java)
                    startActivity(intent)
                    mainActivity.finish()
                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
                Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun deleteTempFiles(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isDirectory) {
                        deleteTempFiles(f)
                    } else {
                        f.delete()
                    }
                }
            }
        }
        return file.delete()
    }

    private fun setSpinner(spinner: Spinner, categories: ArrayList<Category>) {
        val categoryTitles: ArrayList<String> = ArrayList()
        for (category in categories){
            categoryTitles.add(category.title)
        }
        val sortAdapter: ArrayAdapter<*> = ArrayAdapter(mainActivity, R.layout.category_item, categoryTitles)
        sortAdapter.setDropDownViewResource(R.layout.category_item)
        spinner.adapter = sortAdapter
    }

}