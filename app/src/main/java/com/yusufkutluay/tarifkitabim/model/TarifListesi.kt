package com.yusufkutluay.tarifkitabim.model


import java.io.Serializable

class TarifListesi (

    val baslik: String,

    val malzeme: String,

    val tarif: String,

    val gorsel: ByteArray,

    val kategori: String,

    var id : Int,

){
}