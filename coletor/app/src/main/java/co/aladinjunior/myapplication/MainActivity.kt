package co.aladinjunior.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import co.aladinjunior.myapplication.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainScanBttn.setOnClickListener {
            scan()
        }
    }

    private fun scan(){
        val options = ScanOptions().apply {
            setPrompt("Aperte volume para cima para ligar o flash!")
            setBeepEnabled(true)
            setOrientationLocked(true)
            setCaptureActivity(CameraCaptureActivity::class.java)
        }
        barLauncher.launch(options)
    }

    private val barLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract(), object : ActivityResultCallback<ScanIntentResult>{
        override fun onActivityResult(result: ScanIntentResult?) {


            if (result?.contents != null){
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("resultado")
                builder.setMessage(result.contents)
                builder.show()
            }
        }
    })
}