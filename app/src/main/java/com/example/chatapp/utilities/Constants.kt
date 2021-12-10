package com.example.chatapp.utilities

class Constants {
    companion object {
        val KEY_COLLECTION_USERS = "users"
        val KEY_NAME = "name"
        val KEY_EMAIL = "email"
        val KEY_PASSWORD = "password"
        val KEY_PREFERENCE_NAME = "chatAppPreference"
        val KEY_IS_SIGNED_IN = "isSignedIn"
        val KEY_USER_ID = "usersId"
        val KEY_IMAGE = "image"
        val KEY_FCM_TOKEN = "fcmToken"
        val KEY_USER = "user"
        val KEY_COLLECTION_CHAT = "chat"
        val KEY_SENDER_ID = "senderId"
        val KEY_RECEIVER_ID = "receiverId"
        val KEY_MESSAGE = "message"
        val KEY_TIMESTAMP = "timestamp"
        val KEY_COLLECTION_CONVERSATIONS = "conversations"
        val KEY_SENDER_NAME = "senderName"
        val KEY_RECEIVER_NAME = "receiverName"
        val KEY_SENDER_IMAGE = "senderImage"
        val KEY_RECEIVER_IMAGE = "receiverImage"
        val KEY_LAST_MESSAGE = "lastMessage"
        val KEY_AVAILABILITY = "availability"
        val REMOTE_MSG_AUTHORIZATION = "Authorization"
        val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
        val REMOTE_MSG_DATA = "data"
        val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

        var remoteMsgHeaders: HashMap<String, String>? = null


        @JvmName("getRemoteMsgHeaders1")
        fun getRemoteMsgHeaders(): HashMap<String, String> {
            if (remoteMsgHeaders == null) {
                remoteMsgHeaders = HashMap()
                remoteMsgHeaders!!.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAwztdSX0:APA91bHilmsDNbrz1SJqqXXubycTV9e4E5yQYlwE8b5j9u3u4msHiFDkB_3KODrlg3aFshvt0jtQYH5Yjo2SJAmqk-RMX0ThOwddpbm5KrUCkCfYIlhPW1VF3is_MvMwLo16BaH-cXVl"
                )
                remoteMsgHeaders!!.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
                )
            }
            return remoteMsgHeaders as HashMap<String, String>
        }
    }
}