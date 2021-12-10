package com.example.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.example.chatapp.databinding.ActivitySignInBinding
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        binding.textCreateNewAccount.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
        binding.buttonSignIn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }

    }

    private fun signIn() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                        val documentSnapshot = it.result!!.documents.get(0)
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME)!!)
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE)!!)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        loading(false)
                        showToast("Unable to sign in")
                    }
                }
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun isValidSignInDetails(): Boolean {
        if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter a valid Email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else {
            return true
        }
    }
}