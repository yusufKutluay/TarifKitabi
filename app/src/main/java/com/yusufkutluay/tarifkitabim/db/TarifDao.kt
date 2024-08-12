package com.yusufkutluay.tarifkitabim.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.yusufkutluay.tarifkitabim.model.Tarif
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


@Dao
interface TarifDao {

    @Query("SELECT * FROM Tarif")
    fun getAll() : Flowable<List<Tarif>>

    @Query("SELECT * FROM Tarif WHERE kategori = :kategori")
    fun findByKategori(kategori:String):Flowable<List<Tarif>>

    @Query("SELECT * FROM Tarif WHERE favori = 1")
    fun findByFavori():Flowable<List<Tarif>>

    @Update
    fun update(tarif: Tarif) : Completable

    @Insert
    fun insert(tarif: Tarif) : Completable

    @Delete
    fun delete(tarif: Tarif) : Completable



}