# Meituy - Photo Editor App 📸

Aplikasi edit foto dengan filter bergaya kamera premium, dibangun dengan **Flutter**.

## Important Notes

### 🚫 **NO INTERNET REQUIRED!**
Semua filter bekerja **OFFLINE** tanpa koneksi internet.
- Image processing menggunakan algoritma lokal di device
- Tidak ada data yang dikirim ke server
- 100% privacy - semua processing di device Anda

## Features

- **8 Premium Filters:**
  - **Original** - Gambar asli
  - **Ricon Flash** - Flash camera style, highlight lembut
  - **Flash Film** - Film flash look dengan kontras
  - **G7X** - Canon G7X compact camera style
  - **Fuji Flash** - Fujifilm flash aesthetic
  - **Golden Hour** - Warm sunset tones
  - **Matahari Terbenam** - Sunset dramatis + vignette
  - **Lampu Kilat iPhone** - iPhone flash look

- **2 AI Effects (offline, tanpa ML):**
  - **AI Portrait** - Skin smoothing dengan edge-preserve + depth-of-field radial
  - **AI Relight** - Relighting hangat dari sumber cahaya atas-tengah

- **Upload foto** dari galeri atau kamera — tombol selalu tersedia
- **Simpan/Download** hasil edit ke galeri (album `DCIM/Meituy`)

- **UI/UX:** Dark theme, Material Design 3
- **Filter thumbnails live** dari foto yang dipilih
- **Intensity slider** 0-100%
- **Save** ke galeri (album `DCIM/Meituy`), nama file timestamp + nama filter
- **Camera** langsung dari Home → edit hasil jepretan
- **AI Lab**: AI Enhance, Style Studio, AI Portrait, AI Relight — semua fungsional
- **InteractiveViewer** pinch-zoom pada preview
- Processing di background isolate — UI tidak pernah blocked

## Tech Stack

- **Flutter 3.44+ / Dart 3.12+**
- `image_picker` — pilih foto galeri / kamera sistem
- `gal` — simpan ke galeri via MediaStore
- `image` — decode, resize, encode JPEG
- Minimum Android: API 21 (default Flutter)

## Project Structure

```
lib/
├── main.dart                  # Entry point + routes + theme
├── models/
│   └── filter_type.dart       # Enum 8 filter
├── engine/
│   └── filter_engine.dart     # Port pixel-exact dari FilterEngine.kt lama
└── screens/
    ├── home_screen.dart       # Menu utama
    ├── editor_screen.dart     # Editor: preview, thumbnail, slider, save
    └── ai_lab_screen.dart     # AI Lab cards
test/
└── filter_engine_test.dart    # Self-check algoritma filter
```

## How to Build

1. Install Flutter SDK (`flutter doctor` harus bersih)
2. `flutter pub get`
3. Run: `flutter run`
4. Build APK: `flutter build apk --release`

## Filter Algorithms

Setiap filter = color-matrix blend dari identitas (berdasarkan intensitas),
lalu threshold highlight/shadow per piksel; beberapa filter menambahkan
warmth lift atau vignette. Identik matematis dengan versi Kotlin aslinya.

## License

Open source for educational purposes.

## Repository

https://github.com/asepsuhermancampus/meituy
