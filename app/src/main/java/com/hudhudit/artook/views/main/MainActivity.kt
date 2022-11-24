package com.hudhudit.artook.views.main

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hudhudit.artook.R
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.appdefs.Urls
import com.hudhudit.artook.apputils.modules.booleanresponse.BooleanResponse
import com.hudhudit.artook.apputils.remote.RetrofitAPIs
import com.hudhudit.artook.views.main.competition.CompetitionFragment
import com.hudhudit.artook.views.main.home.HomeFragment
import com.hudhudit.artook.views.main.profile.ProfileFragment
import com.hudhudit.artook.views.main.search.SearchFragment
import com.hudhudit.artook.views.main.videosarticles.VideosArticlesFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.*
import java.net.URLEncoder
import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

import android.os.Environment

import androidx.exifinterface.media.ExifInterface


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var bottomNavigationLayout: CoordinatorLayout
    lateinit var competitionFB: FloatingActionButton
    lateinit var fragment: Fragment
    var token = ""
    var isHome = true
    var hasBack = false
    var images: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_nav_bar)
        bottomNavigationLayout = findViewById(R.id.bottom_nav_layout)
        competitionFB = findViewById(R.id.competition_FB)
        bottomNavigationView.background = null

        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()

        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId){
                R.id.navigation_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()
                }
                R.id.navigation_search -> {
                    supportFragmentManager.beginTransaction().add(R.id.container, SearchFragment()).commit()
                }
                R.id.navigation_videos_articles -> {
                    supportFragmentManager.beginTransaction().add(R.id.container, VideosArticlesFragment()).commit()
                }
                R.id.navigation_profile -> {
                    hasBack = false
                    AppDefs.isFeed = true
                    supportFragmentManager.beginTransaction().add(R.id.container, ProfileFragment()).commit()
                }
            }

            return@setOnItemSelectedListener true
        }

        competitionFB.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.container, CompetitionFragment()).commit()
        }

//        images = getAllShownImagesPath(this)

        getToken()
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun createRequestBody(parameter: String?): RequestBody? {
        return RequestBody.create(MediaType.parse("multipart/form-data"), parameter)
    }

    fun createMultipartBodyPart(name: String?, imagePath: String?): MultipartBody.Part? {
        if (imagePath == null || !File(imagePath).exists()) {
            return null
        }
        val imageFile = File(imagePath)
        val requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)

        // MultipartBody.Part is used to send also the actual file name
        return try {
            MultipartBody.Part.createFormData(
                name,
                URLEncoder.encode(imageFile.name, "utf-8"),
                requestBody
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }

     fun createArrayMultipartBodyPart(name: String, selectedUris: ArrayList<String>):
            ArrayList<MultipartBody.Part>  {
        var multiParts: ArrayList<MultipartBody.Part> = ArrayList<MultipartBody.Part>()
        for (i in 0 until selectedUris.size) {
            // 1. Create File using image url (String)
            val file = File(selectedUris.get(i))
            // 2. Create requestBody by using multipart/form-data MediaType from file
            val requestFile: RequestBody = RequestBody.create(MediaType.parse
                ("multipart/form-data"), file)
            // 3. Finally, Create MultipartBody using MultipartBody.Part.createFormData
            val body: MultipartBody.Part = MultipartBody.Part.createFormData(
                name, file.name.trim(), requestFile)
            multiParts.add(body)
        }
        return multiParts
    }

    fun fileUriToBase64(uri: Uri, resolver: ContentResolver): String? {
        var encodedBase64: String? = ""
        try {
            val bytes = readBytes(uri, resolver)
            encodedBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return encodedBase64
    }

    @Throws(IOException::class)
    private fun readBytes(uri: Uri, resolver: ContentResolver): ByteArray {
        // this dynamically extends to take the bytes you read
        val inputStream = resolver.openInputStream(uri)
        val byteBuffer = ByteArrayOutputStream()

        // this is storage overwritten on each iteration with bytes
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        // we need to know how may bytes were read to write them to the
        // byteBuffer
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray()
    }

    fun encoder(imagePath: String?): String? {
        var base64Image = ""
        val file = File(imagePath)
        try {
            FileInputStream(file).use { imageInFile ->
                // Reading a Image file from file system
                val imageData = ByteArray(file.length().toInt())
                imageInFile.read(imageData)
                base64Image = Base64.encodeToString(imageData, 0)
            }
        } catch (e: FileNotFoundException) {
            println("Image not found$e")
        } catch (ioe: IOException) {
            println("Exception while reading the Image $ioe")
        }
        return base64Image
    }

//    @Throws(IOException::class)
//    fun compress(data: String): ByteArray? {
//        val bos = ByteArrayOutputStream(data.length)
//        val gzip = GZIPOutputStream(bos)
//        gzip.write(data.toByteArray())
//        gzip.close()
//        val compressed = bos.toByteArray()
//        bos.close()
//        return compressed
//    }

    fun compress(string: String): String? {
        var compressedString: String? = null
        try {
            val bytes: ByteArray = string.toByteArray(charset("UTF-8"))

            // Compress the bytes
            val deflater = Deflater()
            //val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            deflater.setInput(bytes)
            deflater.finish()

            deflater.deflate(buffer)
            deflater.end()
            //outputStream.close()

//            compressedString = buffer.toString()
            compressedString = Base64.encodeToString(buffer, Base64.DEFAULT)

        } catch (e: IOException) {
            e.printStackTrace()
        }
        return compressedString
    }

    @Throws(IOException::class)
    fun compressString(data: String): String? {
        val bos = ByteArrayOutputStream(data.length)
        val gzip = GZIPOutputStream(bos)
        gzip.write(data.toByteArray())
        gzip.close()
        val compressed = bos.toByteArray()
        bos.close()
        return Base64.encodeToString(compressed, Base64.DEFAULT)
    }

    // UPDATED!
    fun getPath(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor: Cursor? = contentResolver.query(uri!!, projection, null, null, null)
        return if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            val column_index: Int = cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } else null
    }

    fun invisibleBottomBar(){
        bottomNavigationLayout.visibility = View.GONE
    }

    fun visibleBottomBar(){
        bottomNavigationLayout.visibility = View.VISIBLE
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
                updateToken()
            }
    }

    private fun updateToken(){
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
        val likeCall: Call<BooleanResponse> =
            retrofit.create(RetrofitAPIs::class.java).updateToken(token, "1")
        likeCall.enqueue(object : Callback<BooleanResponse> {
            override fun onResponse(call: Call<BooleanResponse>, response: Response<BooleanResponse>) {
                if (response.isSuccessful){

                }else{
                    val gson = Gson()
                    val type = object : TypeToken<BooleanResponse>() {}.type //ErrorResponse is the data class that matches the error response
                    val errorResponse = gson.fromJson<BooleanResponse>(response.errorBody()!!.charStream(), type) // errorResponse is an instance of ErrorResponse that will contain details about the error
//                    Toast.makeText(mainActivity, errorResponse.status.massage.toString(), Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<BooleanResponse>, t: Throwable) {
//                //Toast.makeText(mainActivity, resources.getString(R.string.internet_connection), Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun compressImage(imageUri: String): String? {
        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation: Int = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180F)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270F)
                Log.d("EXIF", "Exif: $orientation")
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap!!, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        val filename = getFilename()
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
    }

    fun getFilename(): String {
        val file = File(Environment.getExternalStorageDirectory().path, "MyFolder/Images")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath.toString() + "/" + System.currentTimeMillis() + ".jpg"
    }

    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

}