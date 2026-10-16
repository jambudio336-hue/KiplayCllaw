# K1pL4y_Agent.pro

Asisten AI Android eksperimental untuk rekayasa perangkat lunak full-stack, otomatisasi yang disetujui pengguna, riset keamanan siber, dan analisis pasar keuangan.

> **Status:** masih tahap pengembangan. APK uji bukan rilis produksi. Fitur real-time bergantung pada koneksi sumber data, API, izin perangkat, dan tool yang benar-benar tersedia.

## Unduh APK uji

1. Buka tab **Releases**: https://github.com/jambudio336-hue/KiplayCllaw/releases
2. Pilih rilis terbaru dengan label **Test Build**.
3. Unduh `app-release.apk` dan pasang di Android.
4. APK uji saat ini ditandatangani dengan debug key; jangan gunakan sebagai rilis produksi atau Play Store.

Jika rilis belum tersedia, buka tab **Actions** dan unduh artifact dari workflow yang berhasil.

## Kemampuan yang disiapkan

- Chat AI dengan pilihan provider/API key yang didukung aplikasi.
- Memori lokal dan riwayat percakapan yang dapat dihapus.
- Salin pesan pengguna dan jawaban AI.
- Input suara dan pembacaan jawaban lewat text-to-speech jika diaktifkan dan didukung perangkat.
- Shortcut alur kerja untuk analisis pasar, keamanan siber, coding full-stack, dan riset.
- Skill analisis forex, saham Indonesia/global, indeks, emas, perak, minyak, crypto, meme coin, spot, futures, serta makroekonomi.
- Otomatisasi Android dan eksekusi coding melalui Termux setelah konfigurasi dan persetujuan pengguna.
- Tema gelap hitam-merah.

Skill analisis keuangan **tidak menyediakan feed harga sendiri**. Harga, chart, berita Forex Factory, kalender CPI, dan data real-time hanya dapat dianalisis bila sumbernya benar-benar dapat diakses melalui browser/tool/API yang tersedia. Jika sumber belum terhubung, agen wajib menyatakan batasannya dan tidak boleh mengarang data.

Untuk order live melalui MT5 atau broker lain, default adalah analisis/simulasi. Sebelum eksekusi, pengguna harus melihat ringkasan instrumen, arah, ukuran, leverage, entry, stop loss, take profit, dan risiko lalu memberi persetujuan eksplisit. Analisis tidak menjamin profit.

## Build melalui GitHub Actions

1. Buka **Actions**.
2. Pilih **K1pL4y Agent Android APK**.
3. Jalankan workflow pada branch `master` atau tunggu build otomatis setelah perubahan.
4. Tunggu workflow selesai dengan status sukses.
5. Unduh artifact debug APK dari run tersebut.

## Persyaratan dan izin

- Android 8.0 (API 26) atau lebih baru.
- API key provider AI harus dikonfigurasi; pemakaian provider mungkin berbayar.
- Aksesibilitas, mikrofon, overlay, lokasi, dan izin lain hanya aktif setelah pengguna mengizinkan.
- Integrasi Termux membutuhkan Termux 0.109+, `allow-external-apps = true`, dan izin Run Command. Periksa setiap perintah sebelum menyetujuinya.

## Keamanan

Agen dapat berinteraksi dengan UI dan meminta izin sensitif. Konfirmasi manusia harus tetap aktif untuk transaksi keuangan, pesan eksternal, perubahan akun, penghapusan, perintah berisiko, dan tindakan yang tidak dapat dibatalkan. Jangan bagikan OTP, password, atau kredensial bank kepada agen.

## Lisensi dan distribusi

Sebelum APK dibagikan secara publik atau komersial, verifikasi asal-usul kode dan lisensi sumber yang diadaptasi. Repositori publik tidak otomatis memberikan hak redistribusi. Pertahankan seluruh atribusi dan pemberitahuan yang diwajibkan oleh lisensi yang berlaku.

## Catatan build saat ini

Application ID Android: `com.k1play.agentpro`. APK saat ini masih eksperimental, membutuhkan pengujian perangkat nyata, dan belum ditandatangani dengan kunci produksi. Nama paket internal dan seluruh sumber perlu ditinjau lagi sebagai bagian dari migrasi branding menyeluruh.
