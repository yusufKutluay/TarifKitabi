package com.yusufkutluay.tarifkitabim.kategori

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.adapter.FavoriRecyclerAdapter
import com.yusufkutluay.tarifkitabim.adapter.RecyclerAdapter
import com.yusufkutluay.tarifkitabim.databinding.FragmentFavorilerimBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Tarif
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class FavorilerimFragment : Fragment() {

    private lateinit var favBinding : FragmentFavorilerimBinding
    private val binding get() = favBinding
    private val disposable = CompositeDisposable()
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao
    private var tarifListesi: List<Tarif> = listOf()
    private lateinit var favAdapter: FavoriRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(),TarifDatabase::class.java,"Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favBinding = FragmentFavorilerimBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        favAdapter = FavoriRecyclerAdapter(tarifListesi,requireContext()) // Burada favAdapter oluşturuluyor
        binding.recyclerView.adapter = favAdapter

        loadFavoriTarifler()

    }


    private fun loadFavoriTarifler() {
        disposable.add(
            tarifDao.findByFavori()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    fun handleResponse(tarifListe : List<Tarif>){

        tarifListesi = tarifListe
        favAdapter.updateData(tarifListesi)

    }

    override fun onResume() {
        super.onResume()
        loadFavoriTarifler()
    }



    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }




}