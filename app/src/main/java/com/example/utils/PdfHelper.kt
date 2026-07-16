package com.example.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.Peminjaman

object PdfHelper {
    fun generatePermissionLetter(context: Context, item: Peminjaman) {
        val webView = WebView(context)
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    @page { size: A4; margin: 0; }
                    body { font-family: 'Helvetica', 'Arial', sans-serif; margin: 0; padding: 40px; color: #1e293b; line-height: 1.5; background: #fff; }
                    
                    .header-container { display: flex; align-items: center; border-bottom: 3px solid #1e3a8a; padding-bottom: 10px; margin-bottom: 20px; }
                    .logo-box { background-color: #1e3a8a; color: white; padding: 15px; border-radius: 12px; font-weight: 900; font-size: 24px; margin-right: 20px; min-width: 60px; text-align: center; }
                    .header-text h1 { margin: 0; font-size: 18px; color: #1e3a8a; font-weight: 800; letter-spacing: 0.5px; }
                    .header-text h2 { margin: 2px 0; font-size: 14px; color: #1e3a8a; font-weight: 700; }
                    .header-text p { margin: 1px 0; font-size: 10px; color: #64748b; font-weight: 500; }
                    
                    .doc-title { text-align: center; margin-top: 30px; margin-bottom: 5px; }
                    .doc-title h3 { margin: 0; font-size: 16px; font-weight: 800; text-decoration: underline; color: #0f172a; }
                    .doc-num { text-align: center; font-size: 11px; font-weight: 700; color: #64748b; margin-bottom: 30px; }
                    
                    .intro { font-size: 12px; margin-bottom: 25px; text-align: justify; }
                    .intro b { color: #0f172a; }
                    
                    .info-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }
                    .info-table td { padding: 8px 0; font-size: 12px; vertical-align: top; }
                    .label { width: 30%; font-weight: 700; color: #475569; text-transform: uppercase; font-size: 11px; }
                    .colon { width: 3%; font-weight: 700; color: #475569; }
                    .value { font-weight: 600; color: #1e293b; }
                    
                    .status-badge { background-color: #f0fdf4; color: #166534; padding: 4px 12px; border-radius: 6px; font-weight: 800; font-size: 10px; border: 1px solid #bbf7d0; display: inline-block; }
                    
                    .ketentuan-box { background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px; margin-bottom: 40px; }
                    .ketentuan-box h4 { margin: 0 0 10px 0; font-size: 11px; font-weight: 800; color: #0f172a; }
                    .ketentuan-box ul { margin: 0; padding-left: 20px; }
                    .ketentuan-box li { font-size: 11px; color: #475569; margin-bottom: 5px; font-weight: 500; }
                    
                    .footer-container { display: flex; justify-content: space-between; align-items: flex-start; margin-top: 40px; }
                    
                    .validasi-box { border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px; width: 260px; display: flex; align-items: center; }
                    .qr-mock { background: #0f172a; width: 50px; height: 50px; border-radius: 6px; margin-right: 12px; display: grid; grid-template-columns: repeat(4, 1fr); gap: 2px; padding: 4px; }
                    .qr-mock div { background: #fff; opacity: 0.8; }
                    .validasi-text p { margin: 0; font-size: 9px; font-weight: 800; color: #0f172a; text-transform: uppercase; }
                    .validasi-text span { font-size: 8px; color: #64748b; font-weight: 600; display: block; margin-top: 2px; }
                    
                    .ttd-box { text-align: center; width: 220px; }
                    .ttd-box p { margin: 0; font-size: 11px; font-weight: 600; color: #475569; }
                    .ttd-box .city-date { font-weight: 700; color: #0f172a; margin-bottom: 5px; }
                    .stamp-mock { margin: 10px auto; width: 80px; height: 80px; border: 2px dashed #4f46e5; border-radius: 50%; display: flex; align-items: center; justify-content: center; transform: rotate(-15deg); opacity: 0.6; color: #4f46e5; font-size: 8px; font-weight: 900; text-align: center; padding: 5px; }
                    .ttd-name { font-weight: 800; color: #0f172a; text-decoration: underline; font-size: 13px; margin-top: 5px; }
                    .ttd-nidn { font-size: 10px; color: #64748b; font-weight: 700; }
                    
                    .disclaimer { text-align: center; font-size: 9px; color: #94a3b8; margin-top: 60px; font-weight: 500; border-top: 1px solid #f1f5f9; padding-top: 15px; }
                </style>
            </head>
            <body>
                <div class="header-container">
                    <div class="logo-box">UM</div>
                    <div class="header-text">
                        <h1>PANITIA PRASARANA & RUMAH TANGGA</h1>
                        <h2>UNIVERSITAS MUHAMMADIYAH SEMARANG (UNIMUS)</h2>
                        <p>Alamat Resmi: Gedung Rektorat Lt. 2, Jl. Kedungmundu Raya No. 125, Semarang 50273 • Telp: (024) 76740295</p>
                        <p>Situs Portal: siprus.unimus.ac.id • Email: rumahtangga@unimus.ac.id</p>
                    </div>
                </div>
                
                <div class="doc-title">
                    <h3>SURAT IZIN & DISPOSISI PEMAKAIAN SARANA</h3>
                </div>
                <div class="doc-num">NOMOR SURAT: 000${item.id}/UNIMUS-SIPRUS/BAUK-RT/V/2026</div>

                <div class="intro">
                    Berdasarkan permohonan pengajuan peminjaman ruangan yang didaftarkan melalui sistem informasi <b>SIPRUS UNIMUS (UniRoom)</b>, Kepala Urusan Rumah Tangga Universitas Muhammadiyah Semarang menerangkan bahwa permohonan berikut dinyatakan <b>DISETUJUI SEPENUHNYA</b> untuk digunakan sesuai peruntukan:
                </div>

                <table class="info-table">
                    <tr><td class="label">NAMA PEMESAN</td><td class="colon">:</td><td class="value">${item.user?.name ?: "Mahasiswa UNIMUS"}</td></tr>
                    <tr><td class="label">EMAIL CIVITAS</td><td class="colon">:</td><td class="value">${item.user?.email ?: "N/A"}</td></tr>
                    <tr><td class="label">RUANG PRASARANA</td><td class="colon">:</td><td class="value">${item.ruang?.nama ?: "-"}</td></tr>
                    <tr><td class="label">GEDUNG & LOKASI</td><td class="colon">:</td><td class="value">${item.ruang?.gedung?.nama ?: "-"} — Lantai ${item.ruang?.lantai ?: "-"} (Kampus)</td></tr>
                    <tr><td class="label">TANGGAL PEMAKAIAN</td><td class="colon">:</td><td class="value">${item.tanggal}</td></tr>
                    <tr><td class="label">ALOKASI WAKTU</td><td class="colon">:</td><td class="value">Pukul ${item.waktuMulai} s/d ${item.waktuSelesai} WIB</td></tr>
                    <tr><td class="label">AGENDA KEGIATAN</td><td class="colon">:</td><td class="value">"${item.keperluan}"</td></tr>
                    <tr><td class="label">KAPASITAS RUANG</td><td class="colon">:</td><td class="value">Maksimum ${item.ruang?.kapasitas ?: "-"} Orang Sektor Kursi</td></tr>
                    <tr><td class="label">OTORISASI TERDAFTAR</td><td class="colon">:</td><td><div class="status-badge">TERVERIFIKASI RUMAH TANGGA</div></td></tr>
                </table>

                <div class="ketentuan-box">
                    <h4>KETENTUAN SYARAT PENGGUNAAN:</h4>
                    <ul>
                        <li>Harap tunjukkan surat disposisi ini (cetak/digital) kepada penjaga/satpam gedung sebelum memasuki ruangan.</li>
                        <li>Pemohon bertanggung jawab penuh atas kebersihan, ketertiban, dan pemeliharaan fasilitas ruangan selama acara.</li>
                        <li>Matikan seluruh lampu, pendingin ruangan (AC), dan proyektor setelah kegiatan selesai digunakan.</li>
                    </ul>
                </div>

                <div class="footer-container">
                    <div class="validasi-box">
                        <div class="qr-mock">
                            <div></div><div></div><div></div><div></div>
                            <div></div><div></div><div></div><div></div>
                            <div></div><div></div><div></div><div></div>
                            <div></div><div></div><div></div><div></div>
                        </div>
                        <div class="validasi-text">
                            <p>VALIDASI DIGITAL SIPRUS</p>
                            <span>CODE: PR-60</span>
                            <span style="color: #10b981;">Persetujuan Online Kepala Bagian Biro RT.</span>
                        </div>
                    </div>
                    
                    <div class="ttd-box">
                        <p class="city-date">Semarang, ${item.tanggal}</p>
                        <p><b>Kepala Bagian Rumah Tangga,</b></p>
                        <div class="stamp-mock">DISETUJUI<br>BIRO RT<br>UNIMUS</div>
                        <div class="ttd-name">Avril Lavigne, M.M.</div>
                        <p class="ttd-nidn">NIDN. 0627098402</p>
                    </div>
                </div>

                <div class="disclaimer">
                    Dokumen ini diterbitkan sah secara hukum oleh Sistem Informasi SIPRUS - Universitas Muhammadiyah Semarang secara otomatis.
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Surat_Disposisi_UniRoom_${item.id}")
                printManager.print("Surat Disposisi UniRoom #${item.id}", printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }
}
