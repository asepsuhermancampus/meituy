# Meituy - Photo Editor App 📸

Android photo editing app with iPhone-style filters inspired by iPhone 15+ computational photography.

## Important Notes

### 🚫 **NO INTERNET REQUIRED!**
**Semua filter bekerja OFFLINE tanpa koneksi internet.**
- Image processing menggunakan algoritma lokal di device
- Tidak ada data yang dikirim ke server
- 100% privacy - semua processing di device Anda

### ✅ **Project Status: Ready to Test**
- **UI/UX:** Elegan & user-friendly (Material Design 3)
- **Performance:** Optimized untuk device Android
- **Stability:** Memory management, no crashes, no leaks
- **Features:** 13 filter premium + intensity slider

## Features

- **13 Premium Filters:**
  - **Original** - Clean original image
  - **Enhance** - General photo enhancement
  - **Brightness** - Light level adjustment
  - **Contrast** - Dynamic range improvement
  - **Saturation** - Color intensity boost
  - **Sharp** - Edge enhancement
  - **Color Fix** - White balance correction
  - **Meitu** - All-in-one Meitu style

- **iPhone 15+ Inspired Filters:**
  - **Vibrant** - Rich, saturated colors with highlight roll-off
  - **Natural** - Authentic skin tones, true-to-life colors
  - **Dramatic** - High contrast, deep blacks, cinematic feel
  - **Portrait** - Skin softening, portrait optimization
  - **Cinematic** - Teal/orange color grading with vignette

## **UI/UX Features**

### ✅ **Modern & Elegant Interface**
- Material Design 3 with proper AppBar
- Card-based filter selection
- Smooth animations and transitions

### ✅ **Intelligent Controls**
- **Intensity Slider** - Adjust filter strength (0-100%)
- **Reset Button** - One-tap to revert to original
- **Loading Indicators** - Visual feedback during processing
- **Placeholder UI** - Clean empty state with instructions

### ✅ **Performance Optimizations**
- **Bitmap Decoding** - Automatic downscaling for large images
- **Memory Management** - Proper recycle() calls to prevent OOM
- **Async Processing** - Coroutines for non-blocking UI
- **Thread Safety** - Main thread never blocked

### ✅ **Permission Handling**
- Graceful permission requests
- Clear error messages
- Permission denial recovery

### ✅ **File Management**
- Photos saved with timestamp + filter name
- No duplicate files
- Proper storage location (Gallery/DCIM)

## Architecture

- **Kotlin** with Coroutines for async processing
- **MVVM-friendly** structure
- **Bitmap processing** with ColorMatrix for efficient operations
- **Memory optimized** - proper bitmap recycling
- **Modern UI** with Material Design 3

## Tech Stack

- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Kotlin Coroutines for background processing
- AndroidX libraries (AppCompat, Material, ConstraintLayout, RecyclerView)
- No external dependencies needed

## How to Build

1. Clone repository
2. Open in Android Studio (Electric Eel+ recommended)
3. Build project
4. Run on emulator or physical device (Android 8.0+)

## Usage

1. Tap **Gallery** button to select photo
2. Scroll through filter thumbnails
3. Tap filter to apply
4. Adjust intensity with slider (0-100%)
5. Tap **Save** to save edited photo
6. Tap **Reset** to revert to original

## Performance Optimizations

- **Bitmap recycling** - prevents memory leaks
- **Coroutines** - background processing without blocking UI
- **Efficient algorithms** - optimized for mobile processing
- **Scaled processing** - maintains performance with high-res images

## Project Structure

```
app/src/main/java/com/meituy/app/
├── MainActivity.kt        # UI controller
├── FilterEngine.kt        # Image processing engine
└── FilterAdapter.kt       # RecyclerView adapter

app/src/main/res/
├── layout/                # UI layouts
├── drawable/             # Filter thumbnails
└── values/               # Resources
```

## Filter Algorithms

Each filter uses a combination of:
- ColorMatrix transformations
- Pixel-based luminance adjustments
- Highlight/shadows correction
- Vignette effects (cinematic filter)
- Contrast/Saturation balancing

## Device Compatibility

**Perfect for Vivo V40 Lite:**
- Android 14 (targetSdk 34)
- 8GB RAM (no memory issues)
- Large screen (responsive layout)
- Camera app integration

**Tested on:**
- Vivo V40 Lite (Android 14)
- Google Pixel 7 Pro (Android 14)
- Samsung Galaxy S23 (Android 14)
- Android Emulator (API 34)

## License

Open source for educational purposes.

## Repository

https://github.com/asepsuhermancampus/meituy