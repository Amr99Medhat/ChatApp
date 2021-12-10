package com.example.chatapp.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.RecentConversationsAdapter
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.listeners.ConversionListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(), ConversionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversions: ArrayList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }

    private fun init() {
        conversions = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversions, this)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }


    private fun loadUserDetails() {
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes: ByteArray =
            Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun listenConversations() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }

    private val eventListener: EventListener<QuerySnapshot> =
        EventListener<QuerySnapshot> { value, error ->
            if (error != null) {
                return@EventListener
            }
            if (value != null) {
                for (documentChange: DocumentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val senderId: String =
                            documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        val receiverId: String =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        val chatMessage = ChatMessage()
                        chatMessage.senderId = senderId
                        chatMessage.receiverId = receiverId
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                            chatMessage.conversionImage =
                                documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)
                            chatMessage.conversionName =
                                documentChange.document.getString(Constants.KEY_RECEIVER_NAME)
                            chatMessage.conversionId =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        } else {
                            chatMessage.conversionImage =
                                documentChange.document.getString(Constants.KEY_SENDER_IMAGE)
                            chatMessage.conversionName =
                                documentChange.document.getString(Constants.KEY_SENDER_NAME)
                            chatMessage.conversionId =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)
                        }
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        conversions.add(chatMessage)
                    } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                        for (i in 0..conversions.size) {
                            val senderId: String =
                                documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                            val receiverId: String =
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                            if (conversions[i].senderId.equals(senderId) && conversions[i].receiverId.equals(
                                    receiverId
                                )
                            ) {
                                conversions[i].message =
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE)
                                conversions[i].dateObject =
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                                break
                            }
                        }
                    }
                }
                conversions.sortWith { obj1: ChatMessage, obj2: ChatMessage ->
                    obj2.dateObject!!.compareTo(obj1.dateObject)
                }
                conversationsAdapter.notifyDataSetChanged()
                binding.conversationsRecyclerView.smoothScrollToPosition(0)
                binding.conversationsRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { updateToken(it) }
    }

    private fun updateToken(token: String) {
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token)
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener { showToast("Unable to updated token") }
    }

    private fun signOut() {
        showToast("Sign out...")
        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_USERS).document(
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        val updates: HashMap<String, Any> = HashMap()
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete())
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }

    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}