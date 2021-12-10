package com.example.chatapp.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.ActivityChatBinding
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User
import com.example.chatapp.network.ApiClient
import com.example.chatapp.network.ApiService
import com.example.chatapp.utilities.Constants
import com.example.chatapp.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import org.jetbrains.annotations.NotNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : BaseActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversionId: String? = null
    private var isReceiverAvailable = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()

    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            preferenceManager.getString(Constants.KEY_USER_ID)!!,
            chatMessages,
            getBitmapFromEncodedString(receiverUser.image)
        )
        binding.chatRecyclerView.adapter = chatAdapter



        database = FirebaseFirestore.getInstance()

    }

    private fun sendMessage() {
        val message: HashMap<Any, Any> = HashMap()
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)!!)
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id!!)
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.text.toString())
        message.put(Constants.KEY_TIMESTAMP, Date())
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if (conversionId != null) {
            updateConversion(binding.inputMessage.text.toString())
        } else {
            val conversion: HashMap<String, Any> = HashMap()
            conversion.put(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
            conversion.put(
                Constants.KEY_SENDER_NAME,
                preferenceManager.getString(Constants.KEY_NAME)!!
            )
            conversion.put(
                Constants.KEY_SENDER_IMAGE,
                preferenceManager.getString(Constants.KEY_IMAGE)!!
            )
            conversion.put(
                Constants.KEY_RECEIVER_ID,
                receiverUser.id!!
            )
            conversion.put(
                Constants.KEY_RECEIVER_NAME,
                receiverUser.name!!
            )
            conversion.put(
                Constants.KEY_RECEIVER_IMAGE,
                receiverUser.image!!
            )
            conversion.put(
                Constants.KEY_LAST_MESSAGE,
                binding.inputMessage.text.toString()
            )
            conversion.put(
                Constants.KEY_TIMESTAMP,
                Date()
            )
            addConversion(conversion)
        }
        if (!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(
                    Constants.KEY_FCM_TOKEN,
                    preferenceManager.getString(Constants.KEY_FCM_TOKEN)
                )
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.text.toString())

                val body = JSONObject()
                body.put(Constants.REMOTE_MSG_DATA, data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch (exception: Exception) {
                showToast(exception.message!!)
            }
        }
        binding.inputMessage.text = null

    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        ApiClient.getClient()?.create(ApiService::class.java)?.sendMessage(
            Constants.getRemoteMsgHeaders(),
            messageBody
        )?.enqueue(object : Callback<String> {
            override fun onResponse(
                @NotNull call: Call<String>,
                @NotNull response: Response<String>
            ) {
                if (response.isSuccessful) {
                    try {
                        if (response.body() != null) {
                            val responseJson = JSONObject(response.body()!!)
                            val results: JSONArray = responseJson.getJSONArray("results")
                            if (responseJson.getInt("failure") == 1) {
                                val error: JSONObject = results.get(0) as JSONObject
                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfully")
                } else {
                    showToast("Error: ${response.code()}")
                }
            }

            override fun onFailure(@NotNull call: Call<String>, @NotNull t: Throwable) {
                showToast(t.toString())
            }

        })
    }

    private fun listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
            receiverUser.id!!
        ).addSnapshotListener(
            this
        ) { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    val availability: Int = Objects.requireNonNull(
                        value.getLong(Constants.KEY_AVAILABILITY)
                    )!!.toInt()
                    isReceiverAvailable = availability == 1
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE)
                    chatAdapter.setReceiverImageProfile(getBitmapFromEncodedString(receiverUser.image)!!)
                    chatAdapter.notifyItemChanged(0, chatMessages.size)
                }
            }
            if (isReceiverAvailable) {
                binding.textAvailability.visibility = View.VISIBLE
            } else {
                binding.textAvailability.visibility = View.GONE
            }

        }
    }

    private fun listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
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
                val count = chatMessages.size
                for (documentChange in value.documentChanges) {
                    if (documentChange.type == DocumentChange.Type.ADDED) {
                        val chatMessage = ChatMessage()
                        chatMessage.senderId =
                            documentChange.document.getString(Constants.KEY_SENDER_ID)
                        chatMessage.receiverId =
                            documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                        chatMessage.message =
                            documentChange.document.getString(Constants.KEY_MESSAGE)
                        chatMessage.dateTime =
                            getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                        chatMessage.dateObject =
                            documentChange.document.getDate(Constants.KEY_TIMESTAMP)
                        chatMessages.add(chatMessage)
                    }

                }

                chatMessages.sortWith { obj1: ChatMessage, obj2: ChatMessage ->
                    obj1.dateObject!!.compareTo(obj2.dateObject)
                }
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged()
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                }
                binding.chatRecyclerView.visibility = View.VISIBLE
            }
            binding.progressBar.visibility = View.GONE
            if (conversionId == null) {
                checkForConversion()
            }
        }

    private fun getBitmapFromEncodedString(encodedImage: String?): Bitmap? {
        return if (encodedImage != null) {
            val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }

    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.textName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any>) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener {
                conversionId = it.id
            }
    }

    private fun updateConversion(message: String) {
        val documentReference: DocumentReference =
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId!!)
        documentReference.update(
            Constants.KEY_LAST_MESSAGE,
            message,
            Constants.KEY_TIMESTAMP,
            Date()
        )
    }

    private fun checkForConversion() {

        if (chatMessages.size != 0) {
            checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                receiverUser.id!!
            )
            checkForConversionRemotely(
                receiverUser.id!!,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        }


    }

    private fun checkForConversionRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversionOnCompleteListener)
    }

    private val conversionOnCompleteListener: OnCompleteListener<QuerySnapshot> =
        OnCompleteListener {
            if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                val documentSnapshot: DocumentSnapshot = it.result!!.documents[0]
                conversionId = documentSnapshot.id
            }

        }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }

}