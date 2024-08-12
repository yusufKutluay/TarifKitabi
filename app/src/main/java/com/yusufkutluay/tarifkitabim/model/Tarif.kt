package com.yusufkutluay.tarifkitabim.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tarif(

    @ColumnInfo(name = "baslik")
    var baslik: String,

    @ColumnInfo(name = "malzeme")
    var malzeme: String,

    @ColumnInfo(name = "tarif")
    var tarif: String,

    @ColumnInfo(name = "gorsel")
    var gorsel: ByteArray,

    @ColumnInfo(name = "kategori")
    var kategori: String,

    @ColumnInfo(name = "favori")
    var favori : Boolean = false

)  {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}