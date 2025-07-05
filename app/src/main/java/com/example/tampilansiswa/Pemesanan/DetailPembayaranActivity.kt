package com.example.tampilansiswa.Pemesanan

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tampilansiswa.R
import java.text.SimpleDateFormat
import java.util.*

class DetailPembayaranActivity : AppCompatActivity() {
    private lateinit var txtGender: TextView
    private lateinit var txtMetode: TextView
    private lateinit var txtProgram: TextView
    private lateinit var txtTanggal: TextView
    private lateinit var txtJam: TextView
    private lateinit var txtTotal: TextView
    private lateinit var txtCountdown: TextView
    private lateinit var txtJatuhTempo: TextView
    private lateinit var txtRekening: TextView
    private lateinit var btnOK: Button

    private val rekening = "112 0896 2357 9810"
    private val jatuhTempoMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000 // +24 jam

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pembayaran)

        // Bind semua View
        txtGender     = findViewById(R.id.txtGender)
        txtMetode     = findViewById(R.id.txtMetode)
        txtProgram    = findViewById(R.id.txtProgram)
        txtTanggal    = findViewById(R.id.txtTanggal)
        txtJam        = findViewById(R.id.txtJam)
        txtTotal      = findViewById(R.id.txtTotal)
        txtCountdown  = findViewById(R.id.txtCountdown)
        txtJatuhTempo = findViewById(R.id.txtJatuhTempo)
        txtRekening   = findViewById(R.id.txtRekening)
        btnOK         = findViewById(R.id.btnOK)


        // Ambil data dari Intent
        val gender  = intent.getStringExtra("gender")  ?: "-"
        val metode  = intent.getStringExtra("metode")  ?: "-"
        val program = intent.getStringExtra("program") ?: "-"
        val tanggal = intent.getStringExtra("tanggal") ?: "-"
        val jam     = intent.getStringExtra("jam")     ?: ""
        val total   = intent.getIntExtra("total", 0)

        // Tampilkan data
        txtGender.text  = "Jenis Kelamin: $gender"
        txtMetode.text  = "Metode Bayar: $metode"
        txtProgram.text = "Program: $program"
        txtTanggal.text = "Tanggal: $tanggal"
        txtJam.text     = "Jam: $jam"
        txtTotal.text   = "Rp ${String.format("%,d", total).replace(',', '.')}"
        txtRekening.text = rekening

        // Format jatuh tempo
        val fmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        txtJatuhTempo.text = "Jatuh tempo ${fmt.format(Date(jatuhTempoMillis))}"

        // Mulai countdown
        startCountdown(jatuhTempoMillis - System.currentTimeMillis())

        // Copy nomor rekening
        txtRekening.setOnClickListener {
            val cb = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cb.setPrimaryClip(ClipData.newPlainText("Rekening", rekening))
            Toast.makeText(this, "Nomor rekening disalin", Toast.LENGTH_SHORT).show()
        }

        // Tombol OK: tampilkan dialog sukses
        btnOK.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_sukses, null)
            val dlg = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
            dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dlg.show()
            view.postDelayed({
                dlg.dismiss()
                finish()
            }, 2000)
        }
    }

    private fun startCountdown(millis: Long) {
        object : CountDownTimer(millis, 1000) {
            override fun onTick(remaining: Long) {
                val h = remaining / (1000 * 60 * 60)
                val m = (remaining / (1000 * 60)) % 60
                val s = (remaining / 1000) % 60
                txtCountdown.text = "%02d jam %02d menit %02d detik".format(h, m, s)
            }
            override fun onFinish() {
                txtCountdown.text = "Waktu habis"
            }
        }.start()
    }
}
