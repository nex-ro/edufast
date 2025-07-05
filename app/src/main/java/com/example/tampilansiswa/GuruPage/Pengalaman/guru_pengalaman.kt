package com.example.tampilansiswa.GuruPage.Pengalaman

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tampilansiswa.Data.Pengalaman
import com.example.tampilansiswa.GuruPage.Dashboard_guru
import com.example.tampilansiswa.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class guru_pengalaman : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnTambahPengalaman: Button
    private lateinit var adapter: PengalamanAdapter
    private lateinit var db: FirebaseFirestore
    private var pengalamanList = mutableListOf<Pengalaman>()
    private var uid: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guru_pengalaman, container, false)

        initViews(view)
        setupFirestore()
        setupRecyclerView()

        view.post {
            getUserUid()
        }

        btnTambahPengalaman.setOnClickListener {
            showTambahPengalamanDialog()
        }
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener{
            navigateToFragment(Dashboard_guru())
        }

        return view
    }
    private fun navigateToFragment(fragment: Fragment) {
        try {
            requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(context, "Error navigating to page", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.post {
            getUserUid()
        }
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewPengalaman)
        btnTambahPengalaman = view.findViewById(R.id.btnTambahPengalaman)
    }

    private fun setupFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    private fun setupRecyclerView() {
        adapter = PengalamanAdapter(
            pengalamanList,
            onEditClick = { pengalaman -> showEditPengalamanDialog(pengalaman) },
            onDeleteClick = { pengalaman -> showDeleteConfirmation(pengalaman) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun getUserUid() {
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        uid = sharedPref.getString("uid", "") ?: ""

        if (uid.isNotEmpty()) {
            loadPengalaman()
        } else {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPengalaman() {
        if (uid.isEmpty()) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("pengalaman")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (documents != null) {
                    pengalamanList.clear()

                    for (document in documents) {
                        try {
                            val pengalaman = document.toObject(Pengalaman::class.java)
                            pengalaman.id = document.id
                            pengalamanList.add(pengalaman)
                        } catch (e: Exception) {
                            // Handle error silently
                        }
                    }

                    if (::adapter.isInitialized) {
                        adapter.notifyDataSetChanged()
                        recyclerView.requestLayout()
                    }
                } else {
                    pengalamanList.clear()
                    if (::adapter.isInitialized) {
                        adapter.notifyDataSetChanged()
                    }
                }
            }
    }

    private fun showTambahPengalamanDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_tambah_pengalaman, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val etNamaPengalaman = dialogView.findViewById<TextInputEditText>(R.id.etNamaPengalaman)
        val etJabatan = dialogView.findViewById<TextInputEditText>(R.id.etJabatan)
        val etTahunMulai = dialogView.findViewById<TextInputEditText>(R.id.etTahunMulai)
        val etTahunBerakhir = dialogView.findViewById<TextInputEditText>(R.id.etTahunBerakhir)
        val etJobDesc = dialogView.findViewById<TextInputEditText>(R.id.etJobDesc)
        val cbMasihBekerja = dialogView.findViewById<CheckBox>(R.id.cbMasihBekerja)
        val layoutTahunBerakhir = dialogView.findViewById<TextInputLayout>(R.id.layoutTahunBerakhir)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)

        cbMasihBekerja.setOnCheckedChangeListener { _, isChecked ->
            layoutTahunBerakhir.visibility = if (isChecked) View.GONE else View.VISIBLE
            if (isChecked) {
                etTahunBerakhir.setText("")
            }
        }

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val namaPengalaman = etNamaPengalaman.text.toString().trim()
            val jabatan = etJabatan.text.toString().trim()
            val tahunMulai = etTahunMulai.text.toString().trim()
            val tahunBerakhir = etTahunBerakhir.text.toString().trim()
            val jobDesc = etJobDesc.text.toString().trim()
            val masihBekerja = cbMasihBekerja.isChecked

            if (validateInput(namaPengalaman, jabatan, tahunMulai, tahunBerakhir, masihBekerja)) {
                val pengalaman = Pengalaman(
                    namaPengalaman = namaPengalaman,
                    jabatan = jabatan,
                    tahunMulai = tahunMulai,
                    tahunBerakhir = if (masihBekerja) "" else tahunBerakhir,
                    jobDesc = jobDesc,
                    masihBekerja = masihBekerja,
                    uid = uid
                )

                tambahPengalaman(pengalaman)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showEditPengalamanDialog(pengalaman: Pengalaman) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_tambah_pengalaman, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etNamaPengalaman = dialogView.findViewById<TextInputEditText>(R.id.etNamaPengalaman)
        val etJabatan = dialogView.findViewById<TextInputEditText>(R.id.etJabatan)
        val etTahunMulai = dialogView.findViewById<TextInputEditText>(R.id.etTahunMulai)
        val etTahunBerakhir = dialogView.findViewById<TextInputEditText>(R.id.etTahunBerakhir)
        val etJobDesc = dialogView.findViewById<TextInputEditText>(R.id.etJobDesc)
        val cbMasihBekerja = dialogView.findViewById<CheckBox>(R.id.cbMasihBekerja)
        val layoutTahunBerakhir = dialogView.findViewById<TextInputLayout>(R.id.layoutTahunBerakhir)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)

        tvTitle.text = "Edit Pengalaman"
        etNamaPengalaman.setText(pengalaman.namaPengalaman)
        etJabatan.setText(pengalaman.jabatan)
        etTahunMulai.setText(pengalaman.tahunMulai)
        etTahunBerakhir.setText(pengalaman.tahunBerakhir)
        etJobDesc.setText(pengalaman.jobDesc)
        cbMasihBekerja.isChecked = pengalaman.masihBekerja

        layoutTahunBerakhir.visibility = if (pengalaman.masihBekerja) View.GONE else View.VISIBLE

        cbMasihBekerja.setOnCheckedChangeListener { _, isChecked ->
            layoutTahunBerakhir.visibility = if (isChecked) View.GONE else View.VISIBLE
            if (isChecked) {
                etTahunBerakhir.setText("")
            }
        }

        btnBatal.setOnClickListener {
            dialog.dismiss()
        }

        btnSimpan.setOnClickListener {
            val namaPengalaman = etNamaPengalaman.text.toString().trim()
            val jabatan = etJabatan.text.toString().trim()
            val tahunMulai = etTahunMulai.text.toString().trim()
            val tahunBerakhir = etTahunBerakhir.text.toString().trim()
            val jobDesc = etJobDesc.text.toString().trim()
            val masihBekerja = cbMasihBekerja.isChecked

            if (validateInput(namaPengalaman, jabatan, tahunMulai, tahunBerakhir, masihBekerja)) {
                val updatedPengalaman = pengalaman.copy(
                    namaPengalaman = namaPengalaman,
                    jabatan = jabatan,
                    tahunMulai = tahunMulai,
                    tahunBerakhir = if (masihBekerja) "" else tahunBerakhir,
                    jobDesc = jobDesc,
                    masihBekerja = masihBekerja
                )

                updatePengalaman(updatedPengalaman)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(pengalaman: Pengalaman) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Pengalaman")
            .setMessage("Apakah Anda yakin ingin menghapus pengalaman \"${pengalaman.namaPengalaman}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePengalaman(pengalaman)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun validateInput(
        namaPengalaman: String,
        jabatan: String,
        tahunMulai: String,
        tahunBerakhir: String,
        masihBekerja: Boolean
    ): Boolean {
        when {
            namaPengalaman.isEmpty() -> {
                Toast.makeText(requireContext(), "Nama pengalaman tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            jabatan.isEmpty() -> {
                Toast.makeText(requireContext(), "Jabatan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            tahunMulai.isEmpty() -> {
                Toast.makeText(requireContext(), "Tahun mulai tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            !masihBekerja && tahunBerakhir.isEmpty() -> {
                Toast.makeText(requireContext(), "Tahun berakhir tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return false
            }
            tahunMulai.length != 4 -> {
                Toast.makeText(requireContext(), "Tahun mulai harus 4 digit", Toast.LENGTH_SHORT).show()
                return false
            }
            !masihBekerja && tahunBerakhir.length != 4 -> {
                Toast.makeText(requireContext(), "Tahun berakhir harus 4 digit", Toast.LENGTH_SHORT).show()
                return false
            }
            !masihBekerja && tahunBerakhir.toInt() < tahunMulai.toInt() -> {
                Toast.makeText(requireContext(), "Tahun berakhir tidak boleh kurang dari tahun mulai", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun tambahPengalaman(pengalaman: Pengalaman) {
        db.collection("pengalaman")
            .add(pengalaman)
            .addOnSuccessListener { documentReference ->
                val generatedId = documentReference.id
                db.collection("pengalaman")
                    .document(generatedId)
                    .update("id", generatedId)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Pengalaman berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menambahkan pengalaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePengalaman(pengalaman: Pengalaman) {
        if (pengalaman.id.isEmpty()) {
            Toast.makeText(requireContext(), "ID pengalaman tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("pengalaman")
            .document(pengalaman.id)
            .set(pengalaman)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pengalaman berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal memperbarui pengalaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePengalaman(pengalaman: Pengalaman) {
        if (pengalaman.id.isEmpty()) {
            Toast.makeText(requireContext(), "ID pengalaman tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("pengalaman")
            .document(pengalaman.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pengalaman berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal menghapus pengalaman: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}