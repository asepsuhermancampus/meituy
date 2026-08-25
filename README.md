# Meituy - Photo Editor App 📸

Android photo editing app with iPhone-style filters inspired by iPhone 15+ computational photography.

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

## Architecture

- **Kotlin** with Coroutines for async processing
- **MVVM-friendly** structure
- **Bitmap processing** with ColorMatrix for efficient operations
- **Memory optimized** - proper bitmap recycling
- **Modern UI** with Material Design

## Tech Stack

- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Kotlin Coroutines for background processing
- AndroidX libraries (AppCompat, ConstraintLayout, RecyclerView)
- No external dependencies needed

## How to Build

1. Clone repository
2. Open in Android Studio (Electric Eel+ recommended)
3. Build project
4. Run on emulator or physical device (Android 8.0+)

## Usage

1. Tap "Select Image" to choose photo from gallery
2. Scroll through filter thumbnails
3. Tap filter to apply (processing shows progress bar)
4. Tap "Save Image" to save edited photo to gallery

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

## Testing Notes

Tested on:
- Vivo V40 Lite (Android 14)
- Google Pixel 7 Pro (Android 14)
- Samsung Galaxy S23 (Android 14)

## License

Open source for educational purposes.