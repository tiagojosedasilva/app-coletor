package co.aladinjunior.myapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import co.aladinjunior.myapplication.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        writePermissionGranted()

        Log.d("file", filesDir.path)
        Log.d("file", "/storage/emulated/0/Download")





        binding.mainScanBttn.setOnClickListener {
            scan()





        }


    }

    private fun writeFile(content: String){
        val fileName = "arquivo.txt"
//        val content = "Conteúdo do arquivo de texto. 2 "

        try {
            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)


            if (!downloadsDir!!.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }

            Toast.makeText(this, "Arquivo criado e conteúdo escrito em: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

            Log.d("file","Arquivo criado e conteúdo escrito em: ${file.absolutePath}")
        } catch (e: Exception) {
            Toast.makeText(this, "Arquivo não criado", Toast.LENGTH_SHORT).show()
            Log.e("error", "Ocorreu um erro ao criar o arquivo: ${e.message}")
        }
    }

    private fun scan(){
        val options = ScanOptions().apply {
            setPrompt("Volume para cima para ligar o flash!")
            setBeepEnabled(true)
            setOrientationLocked(true)



            captureActivity = CameraCaptureActivity::class.java
        }
        barLauncher.launch(options)
    }

    private val barLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(ScanContract(), object : ActivityResultCallback<ScanIntentResult>{
        override fun onActivityResult(result: ScanIntentResult?) {

             val editText = EditText(this@MainActivity)
             val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
              )
            editText.layoutParams = params

            if (result?.contents != null){
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("quantidade")
                builder.setView(editText)
                builder.setMessage(result.contents)
                val quantity = editText.text.toString()
                builder.show()
                if (writePermissionGranted()){
                    
                    writeFile(result.contents + "0000" + quantity + "0000000" + "DATA DE HOJE")

                } else {
                    permissionGranted.launch(REQUEST_PERMISSION)
                }

            }

        }
    })

    private val permissionGranted = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){granted ->

    }

    private fun writePermissionGranted() : Boolean{
          return ContextCompat.checkSelfPermission(
            this,
            REQUEST_PERMISSION[0]
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    REQUEST_PERMISSION[1]
                ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
         val REQUEST_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}