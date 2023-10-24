package co.aladinjunior.coletor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
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
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: SharedPreferences
    private var stringArray: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        writePermissionGranted()

        sharedPref = getSharedPreferences("MyPrefrerences", Context.MODE_PRIVATE)
        stringArray = sharedPref.getString("array2", null)
        val sharedList: MutableList<String>? = stringArray?.split(",")?.toMutableList()
        with(binding) {
            if (sharedList != null && sharedList.isNotEmpty()) {
                mainDeleteBttn.visibility = View.VISIBLE
                mainSaveBttn.visibility = View.VISIBLE
                scanTxt.setTextColor(Color.RED)
                scanTxt.text = getString(R.string.existing_data)
                scanSubTxt.text = getString(R.string.clean_data)
                mainDeleteBttn.setOnClickListener {
                    resultArray.removeAll(sharedList)
                    sharedList.clear()
                    Toast.makeText(
                        this@MainActivity,
                        "Dados foram excluídos com sucesso. Reinicie o aplicativo!",
                        Toast.LENGTH_SHORT
                    ).show()
                    sharedPref.edit()
                        .clear()
                        .apply()

                }
                resultArray.addAll(sharedList)
            } else {
                null
            }
        }




        binding.mainScanBttn.setOnClickListener {
            scan()
        }
        binding.mainSaveBttn.setOnClickListener {
            sharedPref.edit()
                .clear()
                .apply()
            createDocument()
        }


    }

    private val resultArray = mutableListOf<String>()

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
        editText.inputType = InputType.TYPE_CLASS_NUMBER


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
                        binding.mainSaveBttn.visibility = View.VISIBLE
                        if (quantity.length < 2) quantity = "0$quantity"

                        val finalResult = if (quantity.length == 3) {
                            "0${result.contents}${"0000".substring(1)}${quantity}0000000$date"
                        } else {
                            "0${result.contents}0000${quantity}0000000$date"
                        }
                        resultArray.add(finalResult)
                        if (resultArray.isNotEmpty()) {
                            stringArray = resultArray.joinToString(",")
                            sharedPref.edit()
                                .putString("array2", stringArray)
                                .apply()

                        }
                        Log.i("file", stringArray.toString())
                        Toast.makeText(
                            this@MainActivity,
                            "Adicionado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } else {
                    permissionGranted.launch(REQUEST_PERMISSION)
                }
            }
            builder.show()


        }
    }
    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            try {
                val outputStream = this.contentResolver.openOutputStream(uri!!)
                for (line in resultArray) {
                    outputStream?.write("$line\n".toByteArray())
                }
                resultArray.clear()
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            Toast.makeText(
                this,
                "Ação cancelada ou falhou ao criar o documento",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createDocument() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "fileName.txt")

        }

        createDocumentLauncher.launch(intent)
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
        const val DATE_PATTERN = "dd/MM/yyHH:mm:ss"
        val REQUEST_PERMISSION = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}