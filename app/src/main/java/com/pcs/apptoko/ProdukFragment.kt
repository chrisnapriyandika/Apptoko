package com.pcs.apptoko

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pcs.apptoko.LoginActivity.Companion.sessionManager
import com.pcs.apptoko.adapter.ProdukAdapter
import com.pcs.apptoko.api.BaseRetrofit
import com.pcs.apptoko.response.produk.Produk
import com.pcs.apptoko.response.produk.ProdukResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProdukFragment : Fragment() {

    private val rvAdapter = ProdukAdapter()
    private val api by lazy { BaseRetrofit().endPoint }
    private val listProduct = mutableListOf<Produk>()
    private var nameAsc = true
    private var priceAsc = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_produk, container, false)

        val rv = view.findViewById(R.id.rv_produk) as RecyclerView
        rv.setHasFixedSize(true)
        rv.layoutManager = LinearLayoutManager(activity)
        rv.adapter = rvAdapter

        getProduk(view)

        val btnTambah = view.findViewById<Button>(R.id.btnTambah)
        btnTambah.setOnClickListener {
            Toast.makeText(activity?.applicationContext, "Click", Toast.LENGTH_LONG).show()

            val bundle = Bundle()
            bundle.putString("status", "tambah")

            findNavController().navigate(R.id.produkFormFragment, bundle)
        }

        setSortButtons(view)
        setSearchField(view)

        return view
    }

    private fun setSearchField(view: View) {
        val edtSearch = view.findViewById<EditText>(R.id.edtSearch)
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    rvAdapter.setProducts(listProduct)
                } else {
                    val filtered = mutableListOf<Produk>()
                    listProduct.forEach {
                        if (it.nama.contains(s, ignoreCase = true)) filtered.add(it)
                    }
                    rvAdapter.setProducts(filtered)
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun setSortButtons(view: View) {
        val arrowUp =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_upward_24)
        val arrowDown =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_arrow_downward_24)
        val btnSortName = view.findViewById<Button>(R.id.btnSortName)
        val btnSortPrice = view.findViewById<Button>(R.id.btnSortPrice)
        btnSortName.setOnClickListener {
            rvAdapter.sortName(nameAsc)
            nameAsc = !nameAsc
            btnSortName.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                if (nameAsc) arrowDown else arrowUp,
                null
            )
        }
        btnSortPrice.setOnClickListener {
            rvAdapter.sortPrice(priceAsc)
            priceAsc = !priceAsc
            btnSortPrice.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                if (priceAsc) arrowDown else arrowUp,
                null
            )
        }
    }

    fun getProduk(view: View) {
        val token = sessionManager.getString("TOKEN")

        api.getProduk(token.toString()).enqueue(object : Callback<ProdukResponse> {
            override fun onResponse(
                call: Call<ProdukResponse>,
                response: Response<ProdukResponse>,
            ) {
                Log.d("ProdukData", response.body().toString())

                response.body()?.data?.produk?.let {
                    listProduct.clear()
                    listProduct.addAll(it)
                    val txtTotalProduk = view.findViewById(R.id.txtTotalProduk) as TextView
                    txtTotalProduk.text = it.size.toString() + " Item"
                    rvAdapter.setProducts(it)
                }
            }

            override fun onFailure(call: Call<ProdukResponse>, t: Throwable) {
                Log.e("ProdukError", t.toString())
            }

        })
    }

}