package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.chatapp.adapters.UsersAdapter
import com.example.chatapp.databinding.ActivityUsersBinding
import com.example.chatapp.listeners.UserListener
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersActivity : BaseActivity(), UserListener {
    lateinit var binding: ActivityUsersBinding
    lateinit var preferenceManager: PreferenceManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers() {
        loading(true)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val currentId: String = preferenceManager.getString(Constants.KEY_USER_ID)!!
                if (it.isSuccessful && it.result != null) {
                    val users: ArrayList<User> = ArrayList()
                    for (queryDocumentSnapshot: QueryDocumentSnapshot in it.result!!) {
                        if (currentId == queryDocumentSnapshot.id) {
                            continue
                        }
                        val user = User()
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME)
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE)
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL)
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN)
                        user.id = queryDocumentSnapshot.id
                        users.add(user)
                    }

                    if (users.size > 0) {
                        val usersAdapter = UsersAdapter(users, this)
                        binding.userRecyclerView.adapter = usersAdapter
                        binding.userRecyclerView.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}