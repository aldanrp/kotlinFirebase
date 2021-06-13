package com.example.kotlinfirebase.listener

import com.example.kotlinfirebase.model.DrinkModel

interface IDrinkLoadListener {
    fun onDrinkLoadSuccess(drinkModeList:List<DrinkModel>?)
    fun onDrinkLoadFailed(message:String?)
}