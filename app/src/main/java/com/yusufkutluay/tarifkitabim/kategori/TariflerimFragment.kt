package com.yusufkutluay.tarifkitabim.kategori

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.yusufkutluay.tarifkitabim.R
import com.yusufkutluay.tarifkitabim.adapter.RecyclerAdapter
import com.yusufkutluay.tarifkitabim.databinding.FragmentTariflerimBinding
import com.yusufkutluay.tarifkitabim.db.TarifDao
import com.yusufkutluay.tarifkitabim.db.TarifDatabase
import com.yusufkutluay.tarifkitabim.model.Tarif
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale

class TariflerimFragment : Fragment() {

    private var _binding: FragmentTariflerimBinding? = null
    private val binding get() = _binding!!

    private val disposable = CompositeDisposable()
    private lateinit var db: TarifDatabase
    private lateinit var tarifDao: TarifDao
    private var kategori: String = "Ana Yemekler"
    private lateinit var adapter: RecyclerAdapter
    private var tarifListesi: List<Tarif> = listOf()

    private var lastSelectedButton: Button? = null

    private var isSearchViewClosed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java, "Tarifler").build()
        tarifDao = db.tarifDao()
        verileriAl(kategori)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTariflerimBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = 2
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), spanCount)

        setupButtons()
        setupSearchView()
    }

    private fun setupButtons() {
        val buttonContainer = binding.linerLayoutButton
        val buttonNames = resources.getStringArray(R.array.button_names)

        for (name in buttonNames) {
            val button = Button(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 16)
                }
                text = name
                tag = name
                background = ContextCompat.getDrawable(requireContext(),R.drawable.button_background)
                textSize = 12f
                setPadding(35)
                setTextColor(resources.getColor(android.R.color.white, null))
            }

            button.setOnClickListener { handleButtonClick(button) }

            buttonContainer.addView(button)

            if (name == "Ana Yemekler") {
                handleButtonClick(button)
            }
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })

    }

    private fun filterList(query: String?) {
        query?.let {
            val filteredList = tarifListesi.filter { tarif ->
                tarif.baslik.lowercase(Locale.ROOT).contains(it.lowercase(Locale.ROOT))
            }

            if (filteredList.isEmpty()) {
                Snackbar.make(requireView(), "Tarif bulunamadı", Snackbar.LENGTH_LONG).show()
            } else {
                adapter.setFilteredList(filteredList)
            }
        }
    }

    private fun handleButtonClick(selectedButton: Button) {
        lastSelectedButton?.isSelected = false
        selectedButton.isSelected = true
        lastSelectedButton = selectedButton

        kategori = selectedButton.tag as String
        verileriAl(kategori)

        scrollToButton(selectedButton)
    }

    private fun scrollToButton(button: Button) {
        val scrollView = binding.scrollView
        val rect = Rect()
        button.getDrawingRect(rect)
        scrollView.offsetDescendantRectToMyCoords(button, rect)

        val padding = 300
        val scrollX = maxOf(0, rect.right - scrollView.width + padding)
        scrollView.smoothScrollTo(scrollX, 0)
    }

    private fun verileriAl(kategori: String) {
        disposable.add(
            tarifDao.findByKategori(kategori)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(tarifler: List<Tarif>) {
        tarifListesi = tarifler
        adapter = RecyclerAdapter(tarifler,requireActivity())
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        verileriAl(kategori)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        disposable.clear()
    }
}
