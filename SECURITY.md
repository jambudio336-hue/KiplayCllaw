# Keamanan

## Prinsip keamanan
- Masukkan API key melalui **Pengaturan → Penyedia AI**. Jangan menaruh key di source code, README, screenshot, issue, atau commit.
- Tindakan yang mengirim pesan, mengubah data, menghapus file, menjalankan skrip/Termux, atau mengakses fungsi sensitif harus meminta persetujuan.
- Tool yang belum dikenal harus meminta persetujuan sampai ditinjau.
- Aksesibilitas, overlay, notifikasi, kamera, mikrofon, lokasi, dan layanan latar belakang harus diaktifkan sendiri oleh pengguna melalui pengaturan Android.
- Jangan gunakan aplikasi untuk mengakses perangkat, akun, jaringan, atau data tanpa izin.
- Jangan pernah mengirim OTP, password, token sesi, atau kredensial bank ke model AI.

## Pelaporan kerentanan
Untuk laporan privat, hubungi pemilik repositori melalui kanal kontak di profil GitHub. Sertakan langkah reproduksi, versi aplikasi, dampak, dan log yang sudah disensor. Jangan menyertakan API key, OTP, atau data sensitif.

## Status verifikasi
CI yang lulus hanya menunjukkan langkah yang dijalankan oleh workflow berhasil. Itu bukan audit keamanan independen, bukan jaminan bebas bug, dan bukan bukti bahwa semua izin atau fitur aman pada setiap perangkat.
