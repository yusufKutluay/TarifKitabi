package com.yusufkutluay.tarifkitabim.adapter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.yusufkutluay.tarifkitabim.BottomSheet.MyBottomSheetFragment
import com.yusufkutluay.tarifkitabim.databinding.RecyclerRowBinding
import com.yusufkutluay.tarifkitabim.model.Singleton
import com.yusufkutluay.tarifkitabim.model.Tarif

class RecyclerAdapter(private var tarifListesi: List<Tarif>,private val fragmentActivity: FragmentActivity) : RecyclerView.Adapter<RecyclerAdapter.RecyclerHolder>() {

    class RecyclerHolder (val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RecyclerHolder(binding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {

        val list = tarifListesi[position]

        // Byte dizisini Bitmap'e dönüştür
        val bitmap = byteArrayToBitmap(list.gorsel)
        holder.binding.gorsel.setImageBitmap(bitmap)

        holder.binding.yemekName.text = list.baslik

        holder.itemView.setOnClickListener {

            Singleton.tarifListesi = tarifListesi[position]
            val bottomSheet = MyBottomSheetFragment()
            bottomSheet.show(fragmentActivity.supportFragmentManager, bottomSheet.tag)
        }

    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun setFilteredList(tarifListesi: List<Tarif>){
        this.tarifListesi = tarifListesi
        notifyDataSetChanged()
    }

    fun updateData(newList: List<Tarif>){
        this.tarifListesi = newList
        notifyDataSetChanged()
    }

}