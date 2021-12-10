package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerReceivedMessageBinding
import com.example.chatapp.databinding.ItemContainerSentMessageBinding
import com.example.chatapp.models.ChatMessage

class ChatAdapter(
    var senderId: String,
    var chatMessages: ArrayList<ChatMessage>,
    var receiverProfileImage: Bitmap?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_SENT = 1
    val VIEW_TYPE_RECEIVED = 2


    fun setReceiverImageProfile(bitmap: Bitmap) {
        receiverProfileImage = bitmap
    }

    inner class SentMessageViewHolder(private var binding: ItemContainerSentMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
        }
    }



    inner class ReceivedMessageViewHolder(private var binding: ItemContainerReceivedMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            if (receiverProfileImage != null) {
                binding.imageProfile.setImageBitmap(receiverProfileImage)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceivedMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(
                chatMessages[position],
                receiverProfileImage!!
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        if (chatMessages[position].senderId.equals(senderId)) {
            return VIEW_TYPE_SENT
        } else {
            return VIEW_TYPE_RECEIVED
        }
    }
}