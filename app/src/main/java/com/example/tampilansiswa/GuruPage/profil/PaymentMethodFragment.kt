package com.example.tampilansiswa.GuruPage.profil

// PaymentMethodFragment.kt
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.tampilansiswa.R

class PaymentMethodFragment : Fragment() {

    private lateinit var spinnerBank: Spinner
    private lateinit var etNomorRekening: EditText
    private lateinit var btnSimpan: Button
    private lateinit var btnHapus: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val bankList = listOf("Pilih Bank", "Mandiri", "BCA", "BRI")
    private var isUpdating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_method, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupSpinner()
        setupClickListeners()
        loadExistingPaymentMethod()
    }

    private fun initViews(view: View) {
        spinnerBank = view.findViewById(R.id.spinner_bank)
        etNomorRekening = view.findViewById(R.id.et_nomor_rekening)
        btnSimpan = view.findViewById(R.id.btn_simpan)
        btnHapus = view.findViewById(R.id.btn_hapus)
        progressBar = view.findViewById(R.id.progress_bar)
        tvStatus = view.findViewById(R.id.tv_status)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bankList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBank.adapter = adapter
    }

    private fun setupClickListeners() {
        btnSimpan.setOnClickListener {
            if (isUpdating) {
                updatePaymentMethod()
            } else {
                savePaymentMethod()
            }
        }

        btnHapus.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadExistingPaymentMethod() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            updateStatus("User tidak terautentikasi", false)
            return
        }

        showLoading(true)

        db.collection("payment_methods")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document.exists()) {
                    val bank = document.getString("bank") ?: ""
                    val nomorRekening = document.getString("nomorRekening") ?: ""

                    // Set data ke form
                    populateForm(bank, nomorRekening)

                    isUpdating = true
                    btnSimpan.text = "Update Metode Pembayaran"
                    btnHapus.visibility = View.VISIBLE
                    updateStatus("Data ditemukan. Anda dapat mengupdate informasi pembayaran.", true)
                } else {
                    isUpdating = false
                    btnSimpan.text = "Simpan Metode Pembayaran"
                    btnHapus.visibility = View.GONE
                    updateStatus("Belum ada metode pembayaran. Silakan tambah yang baru.", false)
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                updateStatus("Gagal memuat data: ${e.message}", false)
                isUpdating = false
                btnSimpan.text = "Simpan Metode Pembayaran"
                btnHapus.visibility = View.GONE
            }
    }

    private fun populateForm(bank: String, nomorRekening: String) {
        // Set spinner bank
        val bankIndex = bankList.indexOf(bank)
        if (bankIndex != -1) {
            spinnerBank.setSelection(bankIndex)
        }

        // Set nomor rekening
        etNomorRekening.setText(nomorRekening)
    }

    private fun updateStatus(message: String, isSuccess: Boolean) {
        tvStatus.text = message
        tvStatus.visibility = View.VISIBLE
        tvStatus.setTextColor(
            if (isSuccess)
                ContextCompat.getColor(requireContext(), R.color.success_color)
            else
                ContextCompat.getColor(requireContext(), R.color.warning_color)
        )
    }

    private fun savePaymentMethod() {
        val selectedBank = spinnerBank.selectedItem.toString()
        val nomorRekening = etNomorRekening.text.toString().trim()

        if (!validateInput(selectedBank, nomorRekening)) return

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        updateStatus("Menyimpan data...", true)

        val paymentData = hashMapOf(
            "bank" to selectedBank,
            "nomorRekening" to nomorRekening,
            "userId" to currentUser.uid,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("payment_methods")
            .document(currentUser.uid)
            .set(paymentData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Metode pembayaran berhasil disimpan", Toast.LENGTH_SHORT).show()
                updateStatus("Data berhasil disimpan", true)
                isUpdating = true
                btnSimpan.text = "Update Metode Pembayaran"
                btnHapus.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                updateStatus("Gagal menyimpan data: ${e.message}", false)
            }
    }

    private fun updatePaymentMethod() {
        val selectedBank = spinnerBank.selectedItem.toString()
        val nomorRekening = etNomorRekening.text.toString().trim()

        if (!validateInput(selectedBank, nomorRekening)) return

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        updateStatus("Mengupdate data...", true)

        val updateData = hashMapOf(
            "bank" to selectedBank,
            "nomorRekening" to nomorRekening,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("payment_methods")
            .document(currentUser.uid)
            .update(updateData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Metode pembayaran berhasil diupdate", Toast.LENGTH_SHORT).show()
                updateStatus("Data berhasil diupdate", true)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Gagal mengupdate: ${e.message}", Toast.LENGTH_SHORT).show()
                updateStatus("Gagal mengupdate data: ${e.message}", false)
            }
    }

    private fun validateInput(selectedBank: String, nomorRekening: String): Boolean {
        // Validasi input
        if (selectedBank == "Pilih Bank") {
            Toast.makeText(context, "Silakan pilih bank", Toast.LENGTH_SHORT).show()
            updateStatus("Silakan pilih bank", false)
            return false
        }

        if (nomorRekening.isEmpty()) {
            etNomorRekening.error = "Nomor rekening tidak boleh kosong"
            updateStatus("Nomor rekening tidak boleh kosong", false)
            return false
        }

        if (nomorRekening.length < 10) {
            etNomorRekening.error = "Nomor rekening minimal 10 digit"
            updateStatus("Nomor rekening minimal 10 digit", false)
            return false
        }

        // Validasi nomor rekening berdasarkan bank
        if (!isValidAccountNumber(selectedBank, nomorRekening)) {
            etNomorRekening.error = "Format nomor rekening $selectedBank tidak valid"
            updateStatus("Format nomor rekening $selectedBank tidak valid", false)
            return false
        }

        return true
    }

    private fun isValidAccountNumber(bank: String, accountNumber: String): Boolean {
        return when (bank) {
            "Mandiri" -> {
                // Mandiri: 13 digit, dimulai dengan 1
                accountNumber.length == 13 && accountNumber.startsWith("1") && accountNumber.all { it.isDigit() }
            }
            "BCA" -> {
                // BCA: 10 digit
                accountNumber.length == 10 && accountNumber.all { it.isDigit() }
            }
            "BRI" -> {
                // BRI: 15 digit
                accountNumber.length == 15 && accountNumber.all { it.isDigit() }
            }
            else -> false
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSimpan.isEnabled = !show
        btnHapus.isEnabled = !show
        spinnerBank.isEnabled = !show
        etNomorRekening.isEnabled = !show
    }

    private fun clearForm() {
        spinnerBank.setSelection(0)
        etNomorRekening.text.clear()
        tvStatus.visibility = View.GONE
        isUpdating = false
        btnSimpan.text = "Simpan Metode Pembayaran"
        btnHapus.visibility = View.GONE
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus metode pembayaran ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePaymentMethod()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // Function untuk hapus data
    private fun deletePaymentMethod() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        updateStatus("Menghapus data...", true)

        db.collection("payment_methods")
            .document(currentUser.uid)
            .delete()
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Metode pembayaran berhasil dihapus", Toast.LENGTH_SHORT).show()
                clearForm()
                updateStatus("Data berhasil dihapus", true)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(context, "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                updateStatus("Gagal menghapus data: ${e.message}", false)
            }
    }

    // Public function untuk refresh data dari luar
    fun refreshData() {
        loadExistingPaymentMethod()
    }
}