package com.yusufkutluay.tarifkitabim.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.databinding.ActivityTarifEklemeBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Singleton
import com.yusufkutluay.tarifkitabim.model.Tarif
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class TarifEkleme : AppCompatActivity() {

    private lateinit var binding: ActivityTarifEklemeBinding
    private var selectedKategori : String = ""
    private val mDisposable = CompositeDisposable()
    private lateinit var db : TarifDatabase
    private lateinit var tarifDao: TarifDao

    private lateinit var permissionLauncher : ActivityResultLauncher<String> //izin almak için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent> // galeriye erişmek için
    private var secilenGorsel : Uri? = null
    private var secilenBitmap : Bitmap? = null
    private lateinit var duzenle : String

    private var tarifSingleton = Singleton.tarifListesi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTarifEklemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        duzenle = intent.getStringExtra("tarifGeldim")?: ""
        if (duzenle == "tarif"){
            tarifDuzenle()
        }

        registerLauncher()

        db = Room.databaseBuilder(applicationContext, TarifDatabase::class.java, "Tarifler")
            .fallbackToDestructiveMigration() // Eğer şemada değişiklik yaptıysanız bu yöntem kullanışlı olabilir
            .build()
        tarifDao = db.tarifDao()


        binding.back.setOnClickListener {
            finish()
        }

        //////////////////////////////
        //spinner
        val kategoriler = resources.getStringArray(R.array.Kategoriler).toMutableList()
        kategoriler.add(0, "Kategori Seçin")

        // Adapteri oluşturma ve Spinner'a ekleme
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriler)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        // Varsayılan olarak ilk öğeyi seç

        if (duzenle == "tarif"){
            tarifDuzenleKategori(kategoriler)
        }else{
            binding.spinner.setSelection(0)
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Seçilen öğeyi alma
                selectedKategori = parent.getItemAtPosition(position).toString()
                if (selectedKategori == "Kategori Seçin"){
                    selectedKategori = ""
                }else{
                    // Seçilen öğeyi gösterme (örneğin, Toast ile)
                    Toast.makeText(this@TarifEkleme, "Seçilen: $selectedKategori", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Bir görünüm seçilmediğinde Snackbar gösterme
                Snackbar.make(parent, "Lütfen bir kategori seçiniz", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Tamam") {
                        // Snackbar kapatılırken yapılacak işlemler (varsa)
                    }.show()
            }
        }
        ///////////////////////////////////////////////////////////////////////

        binding.gorsel.setOnClickListener{ gorselSec(it) }

        binding.kaydet.setOnClickListener { kaydet(it) }

    }

    private fun tarifDuzenle(){

        binding.yemekIsimText.setText(tarifSingleton?.baslik)
        binding.malzemeText.setText(tarifSingleton?.malzeme)
        binding.tarifText.setText(tarifSingleton?.tarif)
        val bitmap = byteArrayToBitmap(tarifSingleton!!.gorsel)
        binding.imageGoster.visibility = View.VISIBLE
        binding.gorselSec.setImageBitmap(bitmap)
        secilenBitmap = bitmap

    }

    private fun tarifDuzenleKategori(kategoriler: MutableList<String>) {
        var sayac = 0
        while (sayac <= kategoriler.size){

            if (tarifSingleton!!.kategori == kategoriler[sayac]){
                break
            }else{
                sayac++
            }

        }
        binding.spinner.setSelection(sayac)
        selectedKategori = kategoriler[sayac]
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun kaydet(view: View){
        // Kullanıcı girdilerini alma
        val yemekIsimText = binding.yemekIsimText.text.toString()
        val malzemeText = binding.malzemeText.text.toString()
        val tarifText = binding.tarifText.text.toString()

        if (yemekIsimText.isNotEmpty() && malzemeText.isNotEmpty() && tarifText.isNotEmpty() && selectedKategori.isNotEmpty() && secilenBitmap != null) {

            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()
            val tarif = Tarif(yemekIsimText, malzemeText, tarifText, byteDizisi, selectedKategori)


            if (duzenle == "tarif"){

                tarifSingleton?.baslik = yemekIsimText
                tarifSingleton?.malzeme = malzemeText
                tarifSingleton?.tarif = tarifText
                tarifSingleton?.kategori = selectedKategori
                tarifSingleton?.gorsel = byteDizisi

                mDisposable.add(
                    tarifDao.update(tarifSingleton!!)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{
                            finish()
                        }
                )

            }else{
                mDisposable.add(
                    tarifDao.insert(tarif)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            this::handleResponseForInsert,
                            { error -> Log.e("TAG", "Error inserting data: ", error) }  // Hata yönetimi ekleyin
                        )
                )
            }

        } else {
            Snackbar.make(findViewById(android.R.id.content), "Lütfen tüm alanları doldurun", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani: Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1) {
            // görselimiz yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            //görselimiz dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()

        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
    }

    private fun handleResponseForInsert(){
        finish()
    }

    fun gorselSec(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this
                    //contextCompat bir önceki sürümleri kontrol ederek izin ister
                    , Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş izin istememiz gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //should kullanıcının neden izni kabul etmesi gerektiği ile ilgilidir
                    //sncakbar göstermemiz lazım kullanıcıdan neden izin istediğimizi söylememiz gerekiyor.
                    Snackbar.make(
                        view,
                        "Görsel seçmek için galeriye erişim izni vermelisiniz!",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        //ındefinite kullanıcıya bağlı ne kadar kalacağın
                        .setAction(
                            "İzin ver",
                            View.OnClickListener {
                                //izin isteyeceğiz
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)

                            }
                        ).show()
                } else {
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {
                //izin verilmiş galeriye gidebilirim
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this
                    //contextCompat bir önceki sürümleri kontrol ederek izin ister
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //izin verilmemiş izin istememiz gerekiyor
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //should kullanıcının neden izni kabul etmesi gerektiği ile ilgilidir
                    //sncakbar göstermemiz lazım kullanıcıdan neden izin istediğimizi söylememiz gerekiyor.
                    Snackbar.make(
                        view,
                        "Görsel seçmek için galeriye erişim izni vermelisiniz!",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        //ındefinite kullanıcıya bağlı ne kadar kalacağın
                        .setAction(
                            "İzin ver",
                            View.OnClickListener {
                                //izin isteyeceğiz
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                            }
                        ).show()
                } else {
                    //izin isteyeceğiz
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            } else {
                //izin verilmiş galeriye gidebilirim
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }

        }
    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->

            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            val  source = ImageDecoder.createSource(this.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageGoster.visibility = View.VISIBLE
                            binding.gorselSec.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,secilenGorsel)
                            binding.imageGoster.visibility = View.VISIBLE
                            binding.gorselSec.setImageBitmap(secilenBitmap)
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result->

            if (result){
                //izin verildi
                //galeriye gidebiliriz
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //izin verilmedi
                Toast.makeText(this,"İzin verilmedi!",Toast.LENGTH_LONG).show()
            }

        }

        binding.delete.setOnClickListener {
            binding.imageGoster.visibility = View.GONE
            secilenBitmap = null
        }

    }

}