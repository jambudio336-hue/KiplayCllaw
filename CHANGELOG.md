## Unreleased — General app automation improvements
## Unreleased

- Improved cross-app UI targeting by preferring exact visible labels before partial matches.
- Added a `clear_text` action for identified editable fields to support safer form editing.
- Strengthened agent execution guidance: checklist-based steps, screen verification, bounded safe retries, honest reporting, and confirmation before consequential external actions.

- Expanded cross-app automation guidance: inspect foreground app, use accessibility labels or visual analysis, and verify state after each action.
- Added a long-press accessibility action by visible label or screen coordinates.
- Clarified that an existing signed-in session enables ordinary visible UI interaction only; it does not bypass authentication/security controls or authorize extraction of session tokens.
- Added a safer TradingView/XAUUSD analysis workflow to the market analyst skill; no live market feed or trading accuracy is implied by these instructions.

# Changelog

Semua perubahan penting dicatat di sini. Versi saat ini masih eksperimental dan belum merupakan rilis produksi.

## [0.1.2] — 2026-10-17
- Fix OpenRouter requests that reused a Claude model ID.
- Add `openrouter/free` as the default OpenRouter model.
- Sync provider selection with the selected provider's default model.
- Clarify the OpenRouter API key label and expected key prefix.

## [0.1.3] — 2026-10-17
- Improve accessibility UI matching using both visible text and content descriptions.
- Check whether text insertion and send-button activation actually succeeded.
- Fix messaging tool behavior that previously reported a message as sent without checking the send action.
- Support common Indonesian and English message-input/send labels.
- Add device inspection for handset brand/model, Android version, security patch, ABI, memory, battery, and accessibility status.
- Optionally enumerate installed app labels/package IDs for user-requested workflows.
- Validate WhatsApp direct-chat phone numbers and normalize them to country-code digits.
- Report UI submission separately from delivery confirmation.
- Fix Always Allow decisions being treated as denials; persist explicit per-tool choices across app restarts.
- Improve the agent's device-aware planning, screen re-checks, and truthful messaging status reporting.
- Require a supported messaging app to be foregrounded and focus the message field before typing; report submission only when the chat UI provides evidence.

## [Unreleased] — 2026-10-17

### Keamanan
- Ubah kebijakan alat yang tidak dikenal agar meminta persetujuan, bukan otomatis dijalankan.
- Profil izin awal diubah ke Hati-hati; operasi baca sensitif meminta persetujuan.
- Operasi tulis, pengiriman, otomasi, eksekusi skrip/Termux, dan pengelolaan memori meminta persetujuan.
- Hapus auto-enable Aksesibilitas dari tugas Gradle installDebug; instalasi tidak lagi mengubah pengaturan keamanan Android otomatis.
- Nonaktifkan backup data aplikasi dan pindahkan API key Gemini dari query URL ke header.
- Tambahkan ikon aplikasi robot bertopeng dengan palet gelap merah-hitam.
- Catat status lisensi sebagai pekerjaan wajib karena repositori induk belum mencantumkan lisensi yang dapat diverifikasi.

### Perbaikan
- Sinkronkan tes default model provider dengan konfigurasi provider yang benar-benar digunakan aplikasi.
- Perbaiki tes kebijakan persetujuan yang sebelumnya mengharapkan perilaku berbeda dari implementasi.
- Dokumentasi fitur, pemasangan, konfigurasi API key, privasi, batasan, dan build diperluas.

### Catatan
- APK GitHub Actions yang ditandatangani dengan debug key hanya untuk pengujian.
- Latar belakang video anime pertarungan dan efek suara belum disertakan sebagai video produksi; perlu aset video/audio yang dibuat atau dilisensikan secara sah dan pengujian performa perangkat.
- Tidak ada klaim bahwa seluruh bug telah dihilangkan atau keamanan telah tersertifikasi.

## [0.1.0] — Build eksperimental sebelumnya
- Chat AI, konfigurasi provider, memori lokal, riwayat percakapan, alat otomasi dengan izin, dan integrasi Termux.
