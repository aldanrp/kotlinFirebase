package com.example.kotlinfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlincartfirebase.utils.SpaceItemDecoration
import com.example.kotlinfirebase.adapter.MyDrinkAdapter
import com.example.kotlinfirebase.eventbus.UpdateCartEvent
import com.example.kotlinfirebase.listener.ICartLoadListener
import com.example.kotlinfirebase.listener.IDrinkLoadListener
import com.example.kotlinfirebase.model.CartModel
import com.example.kotlinfirebase.model.DrinkModel
import com.example.kotlinfirebase.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main2.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(), IDrinkLoadListener, ICartLoadListener {
    lateinit var drinkLoadListener: IDrinkLoadListener
    lateinit var cartLoadListener: ICartLoadListener

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateCartEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCartEvent::class.java)
        EventBus.getDefault().unregister(this)

    }

    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    fun onUpdateCartEvent(event:UpdateCartEvent)
    {
        countCartFromFirebase()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        init()
        loadDrinkFromFirebase()
        countCartFromFirebase()
    }

    private fun countCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child("UNIQUE_USER_ID")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener.onLoadCartFailed(error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartSnapshot in snapshot.children)
                    {
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener.onLoadCartSuccess(cartModels)
                }

            })
    }

    private fun loadDrinkFromFirebase() {
        var drinkModels : MutableList<DrinkModel> = ArrayList()
        FirebaseDatabase.getInstance()
            .getReference("Drink")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    drinkLoadListener.onDrinkLoadFailed(error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                   if (snapshot.exists())
                   {
                        for (drinkSnapshot in snapshot.children){
                            val drinkModel = drinkSnapshot.getValue(DrinkModel::class.java)
                            drinkModel!!.key = drinkSnapshot.key
                            drinkModels.add(drinkModel)
                        }
                       drinkLoadListener.onDrinkLoadSuccess(drinkModels)
                   }
                    else
                       drinkLoadListener.onDrinkLoadFailed("Drink Item not List")
                }

            })

    }

    private fun init() {
        drinkLoadListener = this
        cartLoadListener = this


        var gridLayoutManager = GridLayoutManager(this,2)
        recycler_drink.layoutManager = gridLayoutManager
        recycler_drink.addItemDecoration(SpaceItemDecoration())

        btnCart.setOnClickListener{startActivity(Intent(this,CartActivity::class.java))}
    }

    override fun onDrinkLoadSuccess(drinkModeList: List<DrinkModel>?) {
      val adapter = MyDrinkAdapter(this,drinkModeList!!, cartLoadListener)
        recycler_drink.adapter = adapter
    }

    override fun onDrinkLoadFailed(message: String?) {
        Snackbar.make(mainLayout, message!!,Snackbar.LENGTH_LONG).show()
    }

    override fun onLoadCartSuccess(cartModeList: List<CartModel>) {
        var cartSum = 0
        for (cartModel in cartModeList!!) cartSum+= cartModel!!.quantity
        badge!!.setNumber(cartSum)
    }

    override fun onLoadCartFailed(message: String) {
        Snackbar.make(mainLayout, message!!,Snackbar.LENGTH_LONG).show()
    }
}