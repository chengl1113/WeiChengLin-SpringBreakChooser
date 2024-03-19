package com.example.weichenglin_springbreakchooser

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Objects
import kotlin.math.abs

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var phraseEditText: EditText
    private lateinit var englishButton: Button
    private lateinit var spanishButton: Button
    private lateinit var frenchButton: Button
    private lateinit var chineseButton: Button
    private lateinit var japaneseButton: Button
    private lateinit var languageSelected: String

    // For the sensors
    private var sensorManager: SensorManager? = null
    private var prevX = 0f
    private var prevY = 0f
    private var prevZ = 0f
    private var prevTime: Long = 0

    // For media player
    private var mediaPlayer: MediaPlayer? = null

    private val speechRecognitionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val resultText =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.get(0) ?: ""
            phraseEditText.setText(resultText)
        }
    }

    private fun playGreeting(language: String) {
        val greetingResourceId = getGreetingResourceId(language)
        if (greetingResourceId != 0) {
            mediaPlayer = MediaPlayer.create(this, greetingResourceId)
            mediaPlayer?.start()
        }
    }

    private fun getGreetingResourceId(language: String) : Int{
        return when (language) {
            "English" -> R.raw.hello
            "Spanish" -> R.raw.hola
            "French" -> R.raw.bonjour
            "Chinese" -> R.raw.nihao
            "Japanese" -> R.raw.konnichiwa
            "Korean" -> R.raw.anneoyon
            else -> R.raw.hello
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phraseEditText = findViewById(R.id.phraseEditText)
        englishButton = findViewById(R.id.englishButton)
        spanishButton = findViewById(R.id.spanishButton)
        frenchButton = findViewById(R.id.frenchButton)
        chineseButton = findViewById(R.id.chineseButton)
        japaneseButton = findViewById(R.id.japaneseButton)

        val buttons = arrayOf(englishButton, spanishButton, frenchButton, chineseButton, japaneseButton)

        buttons.forEach { button ->
            button.setOnClickListener{
                onLanguageSelected(button.text.toString())
            }
        }
    }

    private fun setUpSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager?.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun onLanguageSelected(language: String) {
        setUpSensor()

        languageSelected = language
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLanguageCode(language))
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say a phrase in $language")
        speechRecognitionLauncher.launch(intent)
    }

    private fun getLanguageCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Spanish" -> "es"
            "French" -> "fr"
            "Chinese" -> "zh"
            "Japanese" -> "ja"
            "Korean" -> "ko"
            else -> "en"
        }
    }

    private fun openGoogleMaps(language: String){
        val geoUri: String = when (language) {
            "English" -> "geo:42.3497,-71.0997" // BU coordinates
            "Spanish" -> "geo:21.1619,-86.8515" // Cancun coordinates
            "French" -> "geo:44.8378,-0.5792"    // Bordeux coordinates
            "Chinese" -> "geo:22.5431,114.0579" // Shenzhen coordinates
            "Japanese" -> "geo:35.6591,139.7006" // Shibuya coordinates
            "Korean" -> "geo:37.5665,126.9780"  // Seoul coordinates
            else -> "geo:0,0"                    // Default coordinates
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val curTime = System.currentTimeMillis()
        val timeDiff = curTime - prevTime
        if (timeDiff > 100) {
            val x = event!!.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val velocity = abs(x + y + z - prevX - prevY - prevZ)
//            Log.d(TAG, "onSensorChanged: velocity: $velocity")

            if (velocity > 12) {
                Log.d(TAG, "onSensorChanged: velocity: $velocity")
                playGreeting(languageSelected)
                openGoogleMaps(languageSelected)
            }
            prevX = x
            prevY = y
            prevZ = y
            prevTime = curTime
        }
        
        
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        sensorManager?.unregisterListener(this)
    }

}