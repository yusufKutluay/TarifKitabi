package com.yusufkutluay.tarifkitabim.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.databinding.RecyclerFavoriBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Singleton
import com.yusufkutluay.tarifkitabim.model.Tarif
import com.yusufkutluay.tarifkitabim.view.TarifView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class FavoriRecyclerAdapter (var tarifListesi : List<Tarif>,val context: Context): RecyclerView.Adapter<FavoriRecyclerAdapter.RecyclerFavori>() {

    private var isChangeFav : Boolean = true
    private val db: TarifDatabase = Room.databaseBuilder(context, TarifDatabase::class.java, "Tarifler").build()
    private val tarifDao: TarifDao = db.tarifDao()
    private val disposable = CompositeDisposable()

    class RecyclerFavori(val binding: RecyclerFavoriBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerFavori {
        val binding = RecyclerFavoriBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecyclerFavori(binding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: RecyclerFavori, position: Int) {

        val tarif = tarifListesi[position]
        holder.binding.textView2.text = tarif.baslik

        isChangeFav = tarif.favori

        setFavoriteButton(isChangeFav,holder)

        holder.binding.favoriButton.setOnClickListener {
            val newFavoriteStatus = !tarif.favori
            tarif.favori = newFavoriteStatus

            // Favori durumunu güncelle
            setFavoriteButton(newFavoriteStatus, holder)
            updateVeri(tarif)

            val message = if (newFavoriteStatus) "Favorilere eklendi!" else "Favorilerden çıkarıldı!"
            Snackbar.make(holder.itemView, message, Snackbar.LENGTH_SHORT).show()
        }

        holder.itemView.setOnClickListener {

            Singleton.tarifListesi = tarif

            val intent = Intent(holder.itemView.context,TarifView::class.java)
            holder.itemView.context.startActivity(intent)

        }


    }

    private fun updateVeri(tarif: Tarif){

        disposable.add(
            tarifDao.update(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    // Veri güncelleme başarılı
                    println("Veriler güncellendi")
                }, { error ->
                    // Hata durumunda
                    println("Güncellenirken bir hata oluştu: ${error.message}")
                })
        )


    }



    private fun setFavoriteButton(isFavorite: Boolean,holder: RecyclerFavori) {
        if (isFavorite) {
            holder.binding.favoriButton.setImageResource(R.drawable.fav_close)
        } else {
            holder.binding.favoriButton.setImageResource(R.drawable.fav_open)
        }
    }

    fun updateData(newList: List<Tarif>){
        this.tarifListesi = newList
        notifyDataSetChanged()
    }


}