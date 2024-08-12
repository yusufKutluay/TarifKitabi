package com.yusufkutluay.tarifkitabim.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.databinding.ActivityTarifViewBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Singleton
import com.yusufkutluay.tarifkitabim.model.Tarif
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

class TarifView : AppCompatActivity() {

    private lateinit var binding: ActivityTarifViewBinding
    private var tarif = Singleton.tarifListesi
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao
    private var disposable = CompositeDisposable()
    private val PERMISSION_REQUEST_CODE = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(this, TarifDatabase::class.java, "Tarifler").build()
        tarifDao = db.tarifDao()

        binding = ActivityTarifViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadTarifData()

        setSupportActionBar(binding.toolbar)

        var malzemeVisible = false
        binding.buttonAc.setOnClickListener {
            malzemeVisible = !malzemeVisible
            binding.malzeme.visibility = if (malzemeVisible) View.VISIBLE else View.GONE
            binding.buttonAc.setImageResource(if (malzemeVisible) R.drawable.keyboard_arrow_up_24px else R.drawable.keyboard_arrow_down_24px)
        }

        var tarifVisible = false
        binding.buttonAcTarif.setOnClickListener {
            tarifVisible = !tarifVisible
            binding.tarif.visibility = if (tarifVisible) View.VISIBLE else View.GONE
            binding.buttonAcTarif.setImageResource(if (tarifVisible) R.drawable.keyboard_arrow_up_24px else R.drawable.keyboard_arrow_down_24px)
        }

        binding.backButton.setOnClickListener { finish() }
    }

    private fun loadTarifData() {
        tarif?.let {
            binding.baslik.text = "${it.baslik} (${it.kategori})"
            binding.malzeme.text = it.malzeme
            binding.tarif.text = it.tarif

            val bitmap = byteArrayToBitmap(it.gorsel)
            binding.gorsel.setImageBitmap(bitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tarifekle_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.fav -> {
                tarif?.let {
                    it.favori = true
                    updateTarif(it)
                }
                true
            }
            R.id.duzenle -> {
                val intent = Intent(this, TarifEkleme::class.java)
                intent.putExtra("tarifGeldim", "tarif")
                startActivity(intent)
                true
            }
            R.id.sil -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Uyarı")
                builder.setMessage("Tarifi silmek istediğinize emin misiniz?")
                builder.setPositiveButton("Evet") { _, _ ->
                    tarif?.let {
                        deleteTarif(it)
                    }
                    finish()
                }
                builder.setNegativeButton("Hayır") { _, _ ->
                    Toast.makeText(this, "Tarif silinmedi", Toast.LENGTH_SHORT).show()
                }
                builder.setCancelable(false)
                builder.show()
                true
            }
            R.id.paylas -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun deleteTarif(tarif: Tarif) {
        disposable.add(
            tarifDao.delete(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Tarif silindi", Toast.LENGTH_LONG).show()
                }
        )
    }

    override fun onResume() {
        super.onResume()
        loadTarifData()
    }

    private fun updateTarif(tarif: Tarif) {
        disposable.add(
            tarifDao.update(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Favorilere eklendi", Toast.LENGTH_LONG).show()
                }
        )
    }




}
