package com.yusufkutluay.tarifkitabim.bottomSheet

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.databinding.FragmentMyBottomSheetBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Singleton
import com.yusufkutluay.tarifkitabim.view.TarifView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MyBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var bottomBinding: FragmentMyBottomSheetBinding
    private val binding get() = bottomBinding
    private var favButton: Boolean = false
    private val disposable = CompositeDisposable()
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java, "Tarifler").build()
        tarifDao = db.tarifDao()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bottomBinding = FragmentMyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tarif = Singleton.tarifListesi
        if (tarif != null) {
            binding.itemTitle.text = tarif.baslik
            favButton = tarif.favori

            setFavoriteButton(favButton)

            binding.favButton.setOnClickListener {

                favButton = !favButton
                setFavoriteButton(favButton)

                // Tarif nesnesini güncelle
                tarif.favori = favButton
                disposable.add(
                    tarifDao.update(tarif)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )
            }

            binding.tarifGit.setOnClickListener { tarifGit(it) }
        }
    }

    fun tarifGit(view: View){
        val intent = Intent(requireContext(),TarifView::class.java)
        startActivity(intent)

    }

    private fun handleResponse() {
        view?.let {
            val message = if (favButton) "Favorilere eklendi!" else "Favorilerden çıkarıldı!"
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.favButton.setImageResource(R.drawable.fav_close)
        } else {
            binding.favButton.setImageResource(R.drawable.fav_open)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear() // Hafıza sızıntılarını önlemek için tüm disposable'ları temizleyin
    }
}