package com.example.tampilansiswa.Pemesanan

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R
import java.util.*

class PemesananActivity : AppCompatActivity() {

    private lateinit var rbMale: RadioButton
    private lateinit var rbFemale: RadioButton
    private lateinit var rgMetode: RadioGroup
    private lateinit var rbDana: RadioButton
    private lateinit var rbBNI: RadioButton
    private lateinit var rbBRI: RadioButton

    private lateinit var txtTotal: TextView
    private lateinit var btnPesan: Button

    private lateinit var tvTanggal: TextView
    private lateinit var tvJam: TextView

    private lateinit var btnSD: Button
    private lateinit var btnSMP: Button
    private lateinit var btnSMA: Button

    private var selectedProgram: String = "-"
    private var selectedTanggal: String = "22/01/2025"
    private var selectedJam: String = "11:00"

    private var subtotalGuru = 50000
    private var biayaLayanan = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pemesanan)

        // View binding
        rbMale     = findViewById(R.id.rbMale)
        rbFemale   = findViewById(R.id.rbFemale)
        rgMetode   = findViewById(R.id.rgMetode)
        rbDana     = findViewById(R.id.rbDana)
        rbBNI      = findViewById(R.id.rbBNI)
        rbBRI      = findViewById(R.id.rbBRI)

        txtTotal   = findViewById(R.id.txtTotal)
        btnPesan   = findViewById(R.id.btnPesan)
        tvTanggal  = findViewById(R.id.tvTanggal)
        tvJam      = findViewById(R.id.tvJam)
        btnSD      = findViewById(R.id.btnSD)
        btnSMP     = findViewById(R.id.btnSMP)
        btnSMA     = findViewById(R.id.btnSMA)

        // Hitung total
        updateTotal()

        // Picker tanggal
        tvTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, y, m, d ->
                    selectedTanggal = "%02d/%02d/%04d".format(d, m+1, y)
                    tvTanggal.text = selectedTanggal
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Picker jam
        tvJam.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this,
                { _, h, min ->
                    selectedJam = "%02d:%02d".format(h, min)
                    tvJam.text = selectedJam
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Pilih program
        btnSD.setOnClickListener { highlightProgram("SD", btnSD) }
        btnSMP.setOnClickListener { highlightProgram("SMP", btnSMP) }
        btnSMA.setOnClickListener { highlightProgram("SMA", btnSMA) }

        // Agar metode pembayaran hanya satu tercentang
        rbDana.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbBNI.isChecked = false
                rbBRI.isChecked = false
            }
        }
        rbBNI.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbDana.isChecked = false
                rbBRI.isChecked = false
            }
        }
        rbBRI.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbDana.isChecked = false
                rbBNI.isChecked = false
            }
        }

        // Jika user klik “Buat Pesanan”

    }

    private fun updateTotal() {
        val total = subtotalGuru + biayaLayanan
        txtTotal.text = "Rp ${String.format("%,d", total).replace(',', '.')}"
    }

    private fun highlightProgram(prog: String, btn: Button) {
        // reset
        listOf(btnSD, btnSMP, btnSMA).forEach {
            it.setBackgroundColor(getColor(android.R.color.transparent))
            it.setTextColor(getColor(android.R.color.black))
        }
        // highlight
        btn.setBackgroundColor(getColor(R.color.purple))
        btn.setTextColor(getColor(android.R.color.white))
        selectedProgram = prog
    }
}
