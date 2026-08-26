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
}
