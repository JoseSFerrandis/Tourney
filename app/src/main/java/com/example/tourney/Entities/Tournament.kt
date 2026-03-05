package com.example.tournamentapp.models

import android.os.Parcel
import android.os.Parcelable

data class Tournament(
    val id: Int,
    val name: String,
    val game: String,
    val participants: Int,
    val maxParticipants: Int,
    val date: String,
    val location: String,
    val status: String,
    val prize: String,
    val code: Int
) : Parcelable {

    // 1. Constructor para leer los datos del Parcel
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
    )

    // 2. Escribir los datos al Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(game)
        parcel.writeInt(participants)
        parcel.writeInt(maxParticipants)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(status)
        parcel.writeString(prize)
        parcel.writeInt(code)
    }

    // 3. Este es el método que te estaba pidiendo el error
    override fun describeContents(): Int {
        return 0
    }

    // 4. El objeto Creator necesario para Parcelable
    companion object CREATOR : Parcelable.Creator<Tournament> {
        override fun createFromParcel(parcel: Parcel): Tournament {
            return Tournament(parcel)
        }

        override fun newArray(size: Int): Array<Tournament?> {
            return arrayOfNulls(size)
        }
    }
}