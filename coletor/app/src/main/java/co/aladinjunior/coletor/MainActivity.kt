package co.aladinjunior.coletor

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import co.aladinjunior.coletor.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var archiveNum: Int
        get() = sharedPreferences.getInt(ARCHIVE_NUMBER_KEY, 1)
        set(value) = sharedPreferences.edit().putInt(ARCHIVE_NUMBER_KEY, value).apply()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        writePermissionGranted()
        sharedPreferences = getSharedPreferences("ArquivoPrefs", Context.MODE_PRIVATE)

        binding.mainScanBttn.setOnClickListener {
            scan()


        }


    }

    private val resultArray = mutableListOf<String>()

    private fun writeFile(content: List<String>) {
        val date = SimpleDateFormat(DATE_ARCHIVE, Locale.getDefault()).format(System.currentTimeMillis())
        val fileName = "$date$archiveNum.txt"

        try {
            val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)


            if (!downloadsDir!!.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                for (line in content) {
                    outputStream.write("$line\n".toByteArray())
                }

            }

            Toast.makeText(
                this,
                "Arquivo criado e conteúdo escrito em: ${file.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
            archiveNum++
            openDirectory()

        } catch (e: Exception) {
            Toast.makeText(this, "Arquivo não criado", Toast.LENGTH_SHORT).show()
            Log.e("error", "Ocorreu um erro ao criar o arquivo: ${e.message}")
        }
    }

    private fun scan() {
        val options = ScanOptions().apply {
            setPrompt("Volume para cima para ligar o flash!")
            setBeepEnabled(true)
            setOrientationLocked(true)



            captureActivity = CameraCaptureActivity::class.java
        }
        barLauncher.launch(options)
    }

    private val barLauncher: ActivityResultLauncher<ScanOptions> = registerForActivityResult(
        ScanContract()
    ) { result ->
        val date = SimpleDateFormat(
            DATE_PATTERN,
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        val editText = EditText(this@MainActivity)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editText.layoutParams = params


        editText.hint = getString(R.string.finish_collect)
        val readedCode = getString(R.string.readed_code, result?.contents)
        val insertQuantity = getString(R.string.insert_quantity)

        if (result?.contents != null) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle(insertQuantity)
            builder.setView(editText)
            builder.setMessage(readedCode)


            builder.setPositiveButton(
                android.R.string.ok
            ) { _, _ ->
                if (writePermissionGranted()) {
                    var quantity = editText.text.toString()

                    if (quantity.isEmpty() && resultArray.isEmpty())
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.first_item_quantity),
                            Toast.LENGTH_LONG
                        ).show()
                    else if (quantity.isNotEmpty()) {
                        if (quantity.length < 2) quantity = "0$quantity"

                        val finalResult = if (quantity.length == 3) {
                            "0${result.contents}${"0000".substring(1)}${quantity}0000000$date"
                        } else {
                            "0${result.contents}0000${quantity}0000000$date"
                        }
                        resultArray.add(finalResult)

                        scan()
                    }
                    if (quantity.isEmpty() && resultArray.isNotEmpty()) {
                        writeFile(resultArray)


                    }


                } else {
                    permissionGranted.launch(REQUEST_PERMISSION)
                }
            }
            builder.show()


        }
    }

    private fun openDirectory(){

        val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val pathUri = Uri.parse(downloadDir?.path)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(pathUri, "*/*")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        startActivity(intent)
    }

    private val permissionGranted =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private fun writePermissionGranted(): Boolean {
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
        const val ARCHIVE_NUMBER_KEY = "archive_number_key"
        const val DATE_ARCHIVE = "ddMM"
        const val DATE_PATTERN = "dd/MM/yyHH:mm:ss"
        val REQUEST_PERMISSION = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}