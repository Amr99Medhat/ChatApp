package com.example.chatapp.listeners

import com.example.chatapp.models.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}