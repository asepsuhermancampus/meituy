import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:meituy/engine/filter_engine.dart';
import 'package:meituy/models/filter_type.dart';

void main() {
  // Piksel merah murni 2x1.
  Uint8List redRgba() => Uint8List.fromList([255, 0, 0, 255, 255, 0, 0, 255]);

  Uint8List grey9x9() =>
      Uint8List.fromList(List.filled(9 * 9 * 4, 200, growable: false));

  // 3x3 merah murni: piksel tengah berjarak vignette 0, tak terpengaruh vignette.
  Uint8List red3x3() {
    final px = Uint8List(9 * 4);
    for (var i = 0; i < px.length; i += 4) {
      px[i] = 255;
      px[i + 3] = 255;
    }
    return px;
  }

  test('original mengembalikan salinan identik', () {
    final src = redRgba();
    final out = FilterEngine.apply(src, 2, 1, FilterType.original, 1.0);
    expect(out.rgba, src);
  });

  test('intensity 0 = identitas untuk semua filter', () {
    final src = redRgba();
    for (final t in FilterType.values.skip(1)) {
      final out = FilterEngine.apply(src, 2, 1, t, 0.0);
      expect(out.rgba, src, reason: t.displayName);
    }
  });

  test('intensity 1 menerapkan gain channel R >= asli (semua filter)', () {
    const center = (1 * 3 + 1) * 4;
    // Matahari Terbenam menambahkan vignette strength 0.25: piksel tengah
    // berjarak dist=sqrt(2)/3 sehingga r = round(255 * (1 - 0.25 * dist)) = 225.
    const mtCenterExpected = 225;
    for (final t in FilterType.values.skip(1)) {
      final out = FilterEngine.apply(red3x3(), 3, 3, t, 1.0);
      final expected =
          t == FilterType.matahariTerbenam ? mtCenterExpected : 255;
      expect(out.rgba[center], expected, reason: t.displayName);
      expect(out.rgba.length, 36);
    }
  });

  test('vignette menggelapkan sudut pada matahari terbenam', () {
    const w = 9, h = 9;
    final px = grey9x9();
    for (var i = 3; i < px.length; i += 4) {
      px[i] = 255;
    }
    final out = FilterEngine.apply(px, w, h, FilterType.matahariTerbenam, 1.0);
    final center = (4 * w + 4) * 4;
    final corner = 0;
    expect(out.rgba[center], greaterThan(out.rgba[corner]));
  });

  test('alpha tidak pernah berubah', () {
    for (final t in FilterType.values.skip(1)) {
      final out = FilterEngine.apply(redRgba(), 2, 1, t, 1.0);
      expect(out.rgba[3], 255, reason: t.displayName);
      expect(out.rgba[7], 255, reason: t.displayName);
    }
  });

  Uint8List noisyGradient(int w, int h) {
    final px = Uint8List(w * h * 4);
    var seed = 42;
    int next() {
      seed = (seed * 1103515245 + 12345) & 0x7fffffff;
      return seed % 256;
    }

    for (var i = 0; i < px.length; i += 4) {
      px[i] = next();
      px[i + 1] = next() ~/ 2;
      px[i + 2] = next() ~/ 4;
      px[i + 3] = 255;
    }
    return px;
  }

  test('aiPortrait menghaluskan piksel skin-tone (nilai mendekati rata-rata)', () {
    const w = 40, h = 40;
    final src = noisyGradient(w, h);
    final out = FilterEngine.apply(src, w, h, FilterType.aiPortrait, 1.0);

    // Total variasi (sum |delta| antar piksel horizontal) harus turun.
    int variation(Uint8List p) {
      var sum = 0;
      for (var y = 0; y < h; y++) {
        for (var x = 1; x < w; x++) {
          final i = (y * w + x) * 4;
          sum += (p[i] - p[i - 4]).abs();
        }
      }
      return sum;
    }

    expect(variation(out.rgba), lessThan(variation(src)),
        reason: 'smoothing harus meredam variasi');
  });

  test('aiRelight menerangi area dekat sumber cahaya lebih terang dari sudut', () {
    const w = 9, h = 9;
    final px = grey9x9();
    for (var i = 3; i < px.length; i += 4) {
      px[i] = 255;
    }
    final out = FilterEngine.apply(px, w, h, FilterType.aiRelight, 1.0);
    // Sumber di (4, 3.15) → piksel (4,3); sudut kiri-atas (0,0).
    final nearLight = (3 * w + 4) * 4;
    expect(out.rgba[nearLight], greaterThan(out.rgba[0]));
    // Alpha tak tersentuh.
    expect(out.rgba[nearLight + 3], 255);
  });
}
