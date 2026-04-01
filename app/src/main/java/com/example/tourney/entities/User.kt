package com.example.tourney.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
data class User (
    var id: Int,
    var nickname: String,
    var email: String,
    var password: String,
    var photo: Int
): Parcelable{
    // Usuario actual
    companion object{
        var actualUser: User? = null
    }
}