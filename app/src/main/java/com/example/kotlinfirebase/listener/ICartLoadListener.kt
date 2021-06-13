package com.example.kotlinfirebase.listener

import com.example.kotlinfirebase.model.CartModel

interface ICartLoadListener {
    fun onLoadCartSuccess(cartModeList: List<CartModel>)
    fun onLoadCartFailed(message:String)
}