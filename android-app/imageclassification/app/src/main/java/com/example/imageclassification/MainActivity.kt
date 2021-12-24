package com.example.imageclassification

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.imageclassification.ml.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var select_image_button : Button
    lateinit var make_prediction : Button
    lateinit var img_view : ImageView // to view the image
    lateinit var text_view : TextView // to view text of predicted class
    lateinit var bitmap: Bitmap // to store the image that user selected
    lateinit var camerabtn : Button
    lateinit var tts: TextToSpeech

    // function to camera permission
    public fun checkandGetpermissions(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
        }
        else{
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        select_image_button = findViewById(R.id.button)
        make_prediction = findViewById(R.id.button2)
        img_view = findViewById(R.id.imageView2)
        text_view = findViewById(R.id.textView)
        camerabtn = findViewById<Button>(R.id.camerabtn)
        tts = TextToSpeech(this, this)

        // handling permissions
        checkandGetpermissions()

        // save class names in list of strings
        val Primary_labels = application.assets.open("Primary_Labels.txt").bufferedReader().use { it.readText() }.split("\n")
        val Veg_labels = application.assets.open("Veges_Labels.txt").bufferedReader().use { it.readText() }.split("\n")
        val Fruit_labels = application.assets.open("Fruit_Labels.txt").bufferedReader().use { it.readText() }.split("\n")
        val Package_labels = application.assets.open("Package_Labels.txt").bufferedReader().use { it.readText() }.split("\n")

        // when user clicks select button
        select_image_button.setOnClickListener(View.OnClickListener {
            Log.d("mssg", "button pressed")

            // intent of type image
            var intent : Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 250)
        })

        // when user clicks on predict button
        make_prediction.setOnClickListener(View.OnClickListener {
            // resize the bitmap to model input shape (check .tflite file)
            var resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            // the following code is copied from tflite file
            val prim_model = BaseXception.newInstance(this)

            // create byte buffer from the resized bitmap
            var prim_tensorImage = TensorImage(DataType.FLOAT32)
            prim_tensorImage.load(resized)
            var prim_byteBuffer = prim_tensorImage.buffer

            // Creates inputs for reference.
            val prim_inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            prim_inputFeature0.loadBuffer(prim_byteBuffer)

            // Runs model inference and gets result.
            val prim_outputs = prim_model.process(prim_inputFeature0)
            // predicted output
            val prim_outputFeature0 = prim_outputs.outputFeature0AsTensorBuffer

            // get the index of the highest value from output array
            var prim_max = getMax(prim_outputFeature0.floatArray,3)
            //text_view.setText(Primary_labels[prim_max])

            // Releases model resources if no longer used.
            prim_model.close()

            // Fruits class
            if(Primary_labels[prim_max]=="Fruit"){
                // the following code is copied from tflite file
                val model = FruitsXception1.newInstance(this)

                // create byte buffer from the resized bitmap
                var tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(resized)
                var byteBuffer = tensorImage.buffer

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                // predicted output
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // get the index of the highest value from output array
                var max = getMax(outputFeature0.floatArray,28)

                // get class name of the index from labels.txt
                // and replace text view with prediction
                text_view.setText(Fruit_labels[max])

                // Releases model resources if no longer used.
                model.close()
                speakOut()
            }
            // Packages class
            else if(Primary_labels[prim_max]=="Packages"){
                // the following code is copied from tflite file
                val model = PackagesVgg16.newInstance(this)

                // create byte buffer from the resized bitmap
                var tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(resized)
                var byteBuffer = tensorImage.buffer

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                // predicted output
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // get the index of the highest value from output array
                var max = getMax(outputFeature0.floatArray,31)

                // get class name of the index from labels.txt
                // and replace text view with prediction
                text_view.setText(Package_labels[max])

                // Releases model resources if no longer used.
                model.close()
                speakOut()
            }
            // Vegetables class
            else if (Primary_labels[prim_max]=="Vegetables"){
                // the following code is copied from tflite file
                val model = VegesMobilenet2.newInstance(this)

                // create byte buffer from the resized bitmap
                var tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(resized)
                var byteBuffer = tensorImage.buffer

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                // predicted output
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                // get the index of the highest value from output array
                var max = getMax(outputFeature0.floatArray,22)

                // get class name of the index from labels.txt
                // and replace text view with prediction
                text_view.setText(Veg_labels[max])

                // Releases model resources if no longer used.
                model.close()
                speakOut()
            }

            //prim_model.close()

        })

        // when users clicks on camera button
        camerabtn.setOnClickListener(View.OnClickListener {
            var camera : Intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(camera, 200)
        })


    }

    // when user selects an image, change the img view to this image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // if user selected image
        if(requestCode == 250){
            // show the selected image in the img view
            img_view.setImageURI(data?.data)

            // save the image in bitmap to use in predicting
            var uri : Uri ?= data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        }
        // if user captured image
        else if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            // save the captured image in bitmap to use in predicting
            bitmap = data?.extras?.get("data") as Bitmap
            // show the captured image in the img view
            img_view.setImageBitmap(bitmap)
        }

    }

    fun getMax(arr:FloatArray,label_num:Int) : Int{
        var ind = 0;
        var min = 0.0f;

        // iterate over the 1000 label (bec we have 1000 class) and get the index with max value
        // TODO : change number of classes

        for(i in 0..(label_num-1))
        {
            Log.d("loop", i.toString())
            if(arr[i] > min)
            {
                Log.d("hi","in if")
                Log.d("index", i.toString())
                min = arr[i]
                ind = i;
                Log.d("min", min.toString())
            }
        }
        Log.d("out", ind.toString())
        return ind
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = tts.setLanguage(Locale.US)
        }
    }

    private fun speakOut(){
        val text = text_view.text.toString()
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    /* fun pred(TheModel:Class<*>?){
        // resize the bitmap to model input shape (check .tflite file)
        var resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // the following code is copied from tflite file
        val veg_model = TheModel.newInstance(this)

        // create byte buffer from the resized bitmap
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resized)
        var byteBuffer = tensorImage.buffer

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = veg_model.process(inputFeature0)
        // predicted output
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // get the index of the highest value from output array
        var veg_max = getMax(outputFeature0.floatArray,22)

        // get class name of the index from labels.txt
        // and replace text view with prediction
        text_view.setText(Veg_labels[veg_max])

        // Releases model resources if no longer used.
        veg_model.close()
        speakOut()
     }
     */

}