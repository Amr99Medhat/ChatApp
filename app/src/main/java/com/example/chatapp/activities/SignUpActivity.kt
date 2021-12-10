package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chatapp.databinding.ActivitySignUpBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var preferenceManager: PreferenceManager
    var encodedImage: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }

    private fun setListeners() {
        binding.textSignIn.setOnClickListener {
            onBackPressed()
        }
        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun signUp() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<Any, Any> = HashMap()
        user.put(Constants.KEY_NAME, binding.inputName.text.toString())
        user.put(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
        user.put(Constants.KEY_IMAGE, encodedImage!!)

        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener {
                    loading(false)
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage!!)
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    loading(false)
                    showToast(it.message.toString())
                }
    }

    private fun encodedImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width

        val previewBitmap: Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes: ByteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    if (result.data != null) {
                        val imageUri: Uri? = result.data!!.data
                        try {
                            val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                            binding.imageProfile.setImageBitmap(bitmap)
                            binding.textAddImage.visibility = View.GONE
                            encodedImage = encodedImage(bitmap)

                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                }
            }


    private fun isValidSignUpDetails(): Boolean {
        if (encodedImage == null) {
            showToast("Select profile image")
            return false
        } else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter Name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter a valid Email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString()) {
            showToast("password & Confirm password must be same !")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}