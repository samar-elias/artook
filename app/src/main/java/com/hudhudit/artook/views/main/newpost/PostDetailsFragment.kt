package com.hudhudit.artook.views.main.newpost

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.post.NewPostMedia
import com.hudhudit.artook.apputils.modules.search.Categories
import com.hudhudit.artook.apputils.modules.search.Category
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.databinding.FragmentPostDetailsBinding
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.main.newpost.Adapter.MediaAdapter
import kotlinx.android.synthetic.main.media_layout.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
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
    var images: ArrayList<String> = ArrayList()
    var videos: ArrayList<String> = ArrayList()
    var mediaUris: ArrayList<NewPostMedia> = ArrayList()
    var categories: ArrayList<Category> = ArrayList()
    var categoryId = ""

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
        images = requireArguments().getStringArrayList("images")!!
        videos = requireArguments().getStringArrayList("videos")!!
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
//        binding.deleteMedia1.setOnClickListener {
//            binding.media1CV.visibility = View.GONE
//            mediaUris.removeAt(0)
//        }
//        binding.deleteMedia2.setOnClickListener {
//            binding.media2CV.visibility = View.GONE
//            mediaUris.removeAt(1)
//        }
//        binding.deleteMedia3.setOnClickListener {
//            binding.media3CV.visibility = View.GONE
//            mediaUris.removeAt(2)
//        }
//        binding.deleteMedia4.setOnClickListener {
//            binding.media4CV.visibility = View.GONE
//            mediaUris.removeAt(3)
//        }
//        binding.deleteMedia5.setOnClickListener {
//            binding.media5CV.visibility = View.GONE
//            mediaUris.removeAt(4)
//        }
//        binding.deleteMedia6.setOnClickListener {
//            binding.media6CV.visibility = View.GONE
//            mediaUris.removeAt(5)
//        }
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
        val description = binding.descriptionEdt.text
        if(categoryId == "0"){
            Toast.makeText(mainActivity, resources.getString(R.string.select_a_category), Toast.LENGTH_SHORT).show()
        }else if (description!!.isEmpty()){
            Toast.makeText(mainActivity, resources.getString(R.string.enter_description), Toast.LENGTH_SHORT).show()
        }else{
            createJson()
        }
    }

    private fun createJson(){
        val imagesArray = JSONArray()
        for (image in images){
            val imageObj = JSONObject()
            imageObj.put("image", image)
            imagesArray.put(imageObj)
        }
        val videosArray = JSONArray()
        for (video in videos){
            val videoObj = JSONObject()
            videoObj.put("video", video)
            videosArray.put(videoObj)
        }
        val mediaObj = JSONObject()
        mediaObj.put("images", imagesArray)
        mediaObj.put("videos", videosArray)
        Log.d("mediaObj", mediaObj.toString())

        var tempFile: File? = null
        var writer: BufferedWriter? = null

        tempFile = File.createTempFile("MyTempFile", ".txt")
        writer = BufferedWriter(
            FileWriter(tempFile)
        )
        writer.write(
            mediaObj.toString()
        )
        println("Temp file location: "
                + tempFile.absoluteFile
        )

        val path: String = tempFile.getPath()

        val fileReader = FileReader(path)
        val bufferedReader = BufferedReader(fileReader)
        var buffer: String?
        val stringBuilder = StringBuilder()

        while (bufferedReader.readLine().also { buffer = it } != null) {
            stringBuilder.append(buffer)
        }

        deleteTempFiles(mainActivity.cacheDir)

        while (bufferedReader.readLine().also { buffer = it } != null) {
            stringBuilder.append(buffer)
        }
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