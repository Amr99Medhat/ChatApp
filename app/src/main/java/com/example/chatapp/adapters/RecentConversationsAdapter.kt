package com.example.chatapp.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContainerRecentConversionBinding
import com.example.chatapp.listeners.ConversionListener
import com.example.chatapp.models.ChatMessage
import com.example.chatapp.models.User

class RecentConversationsAdapter(
    var chatMessages: ArrayList<ChatMessage>,
    var conversionListener: ConversionListener
) :
    RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>() {

    inner class ConversionViewHolder(private var binding: ItemContainerRecentConversionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage!!))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user = User()
                user.id = chatMessage.conversionId
                user.name = chatMessage.conversionName
                user.image = chatMessage.conversionImage
                conversionListener.onConversionClicked(user)
            }
        }
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return ConversionViewHolder(
            ItemContainerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
}