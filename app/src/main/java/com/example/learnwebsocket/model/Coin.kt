package com.example.learnwebsocket.model

import com.squareup.moshi.JsonClass

/**
 * Created on : 06/05/21 | 22.24
 * Author     : dededarirahmadi
 * Name       : dededarirahmadi
 * Email      : dededarirahmadi@gmail.com
 */

@JsonClass(generateAdapter = true)
data class Coin(
    val product_id: String?,
    val price: String?
    )