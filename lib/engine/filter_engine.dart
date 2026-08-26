import 'dart:math' as math;
import 'dart:typed_data';

import '../models/filter_type.dart';

class FilteredImage {
  final Uint8List rgba;
  final int width;
  final int height;

  const FilteredImage(this.rgba, this.width, this.height);
}

/// Port 1:1 dari FilterEngine.kt: color-matrix blend dari identitas,
/// lalu threshold highlight/shadow per piksel. Urutan operasi dipertahankan.
class FilterEngine {
  const FilterEngine._();

  static FilteredImage apply(
    Uint8List rgba,
    int width,
    int height,
    FilterType type,
    double intensity,
  ) {
    if (type == FilterType.original || intensity <= 0) {
      return FilteredImage(Uint8List.fromList(rgba), width, height);
    }
    final out = Uint8List.fromList(rgba);
    switch (type) {
      case FilterType.original:
        break;
      case FilterType.riconFlash:
        _riconFlash(out, intensity);
        break;
      case FilterType.flashFilm:
        _flashFilm(out, intensity);
        break;
      case FilterType.g7x:
        _g7x(out, intensity);
        break;
      case FilterType.fujiFlash:
        _fujiFlash(out, intensity);
        break;
      case FilterType.goldenHour:
        _goldenHour(out, intensity);
        break;
      case FilterType.matahariTerbenam:
        _matahariTerbenam(out, width, height, intensity);
        break;
      case FilterType.lampuKilatIphone:
        _lampuKilatIphone(out, intensity);
        break;
    }
    return FilteredImage(out, width, height);
  }

  /// Matrix blend dari identitas: intensity 0 = gambar asli, 1 = penuh.
  static void _blendedMatrixPass(
    Uint8List px,
    List<double> gains,
    List<double> offsets,
    double t,
  ) {
    final gr = 1.0 + (gains[0] - 1.0) * t;
    final gg = 1.0 + (gains[1] - 1.0) * t;
    final gb = 1.0 + (gains[2] - 1.0) * t;
    final or_ = offsets[0] * t;
    final og = offsets[1] * t;
    final ob = offsets[2] * t;
    for (var i = 0; i < px.length; i += 4) {
      px[i] = (px[i] * gr + or_).round().clamp(0, 255);
      px[i + 1] = (px[i + 1] * gg + og).round().clamp(0, 255);
      px[i + 2] = (px[i + 2] * gb + ob).round().clamp(0, 255);
    }
  }

  static void _riconFlash(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.2, 1.15, 1.0], [20, 15, 0], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g;
      if (lum > 180) {
        nr = r + (255 - r) * 0.15 * t;
        ng = g + (255 - g) * 0.12 * t;
      }
      if (lum < 60) {
        final lift = (60 - lum) * 0.08 * t;
        nr += lift;
        ng += lift * 0.9;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
    }
  }

  static void _flashFilm(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.2, 1.08, 1.05], [10, 5, 0], t);
    final contrast = 1.0 + 0.15 * t;
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = (((r / 255.0 - 0.5) * contrast + 0.5) * 255);
      var ng = (((g / 255.0 - 0.5) * contrast + 0.5) * 255);
      var nb = (((b / 255.0 - 0.5) * contrast + 0.5) * 255);
      if (lum > 200) {
        nr += (255 - nr) * 0.1 * t;
        ng += (255 - ng) * 0.08 * t;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
  }

  static void _g7x(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.25, 1.2, 1.17], [12, 8, 10], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g, nb = b;
      if (lum > 190) {
        final roll = (lum - 190) / 65.0;
        nr += (255 - r) * roll * 0.12 * t;
        ng += (255 - g) * roll * 0.10 * t;
        nb += (255 - b) * roll * 0.08 * t;
      }
      if (lum < 50) {
        final lift = (50 - lum) * 0.06 * t;
        nr += lift;
        ng += lift * 1.1;
        nb += lift * 1.2;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
  }

  static void _fujiFlash(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.12, 1.18, 1.08], [8, 12, 5], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g, nb = b;
      if (lum > 180) {
        nr += (255 - r) * 0.08 * t;
        ng += (255 - g) * 0.12 * t;
      }
      if (lum < 55) {
        final lift = (55 - lum) * 0.07 * t;
        nr += lift * 1.1;
        ng += lift;
        nb += lift * 0.9;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
  }

  static void _goldenHour(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.35, 1.15, 0.9], [25, 15, -10], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g, nb = b;
      if (lum > 150) {
        final warmth = (lum - 150) / 105.0;
        nr += warmth * 15 * t;
        ng += warmth * 8 * t;
        nb -= warmth * 10 * t;
      }
      if (lum < 70) {
        final lift = (70 - lum) * 0.06 * t;
        nr += lift * 1.2;
        ng += lift * 0.9;
        nb += lift * 0.7;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
  }

  static void _matahariTerbenam(Uint8List px, int width, int height, double t) {
    _blendedMatrixPass(px, [1.45, 1.05, 0.88], [30, 10, -15], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g, nb = b;
      if (lum > 130) {
        final warmth = (lum - 130) / 125.0;
        nr += warmth * 20 * t;
        ng += warmth * 10 * t;
        nb -= warmth * 15 * t;
      }
      if (lum < 60) {
        final lift = (60 - lum) * 0.05 * t;
        nr += lift * 1.3;
        ng += lift * 0.8;
        nb += lift * 0.6;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
    if (t > 0.3) _vignette(px, width, height, 0.25 * t);
  }

  static void _lampuKilatIphone(Uint8List px, double t) {
    _blendedMatrixPass(px, [1.18, 1.16, 1.15], [15, 12, 8], t);
    for (var i = 0; i < px.length; i += 4) {
      final r = px[i].toDouble(), g = px[i + 1].toDouble(), b = px[i + 2].toDouble();
      final lum = 0.299 * r + 0.587 * g + 0.114 * b;
      var nr = r, ng = g, nb = b;
      if (lum > 200) {
        nr += (255 - r) * 0.1 * t;
        ng += (255 - g) * 0.09 * t;
        nb += (255 - b) * 0.08 * t;
      }
      if (lum < 40) {
        final lift = (40 - lum) * 0.07 * t;
        nr += lift;
        ng += lift;
        nb += lift * 1.1;
      }
      px[i] = nr.round().clamp(0, 255);
      px[i + 1] = ng.round().clamp(0, 255);
      px[i + 2] = nb.round().clamp(0, 255);
    }
  }

  static void _vignette(Uint8List px, int width, int height, double strength) {
    final centerX = width / 2.0;
    final centerY = height / 2.0;
    for (var y = 0; y < height; y++) {
      final dy = (y - centerY) / centerY;
      for (var x = 0; x < width; x++) {
        final dx = (x - centerX) / centerX;
        final dist = math.sqrt(dx * dx + dy * dy);
        final vig = 1.0 - (dist * strength).clamp(0.0, 1.0);
        final idx = (y * width + x) * 4;
        px[idx] = (px[idx] * vig).round().clamp(0, 255);
        px[idx + 1] = (px[idx + 1] * vig).round().clamp(0, 255);
        px[idx + 2] = (px[idx + 2] * vig).round().clamp(0, 255);
      }
    }
  }
}
