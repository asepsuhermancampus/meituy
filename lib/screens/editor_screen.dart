import 'dart:async';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:gal/gal.dart';
import 'package:image/image.dart' as img;
import 'package:image_picker/image_picker.dart';

import '../engine/filter_engine.dart';
import '../models/filter_type.dart';

class EditorArgs {
  final bool openCamera;
  final FilterType? initialFilter;
  const EditorArgs({this.openCamera = false, this.initialFilter});
}

class _FilterJob {
  final Uint8List rgba;
  final int width;
  final int height;
  final FilterType type;
  final double intensity;

  const _FilterJob(this.rgba, this.width, this.height, this.type, this.intensity);
}

FilteredImage _runJob(_FilterJob job) =>
    FilterEngine.apply(job.rgba, job.width, job.height, job.type, job.intensity);

class EditorScreen extends StatefulWidget {
  const EditorScreen({super.key});

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  static const _maxDim = 2048;

  final _picker = ImagePicker();

  Uint8List? _originalRgba;
  int _width = 0;
  int _height = 0;

  ui.Image? _preview;
  Map<FilterType, ui.Image>? _thumbnails;

  FilterType _filter = FilterType.original;
  double _intensity = 1.0;
  bool _busy = false;
  FilterType? _pendingInitialFilter;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final args =
          ModalRoute.of(context)?.settings.arguments as EditorArgs?;
      if (args == null) return;
      _pendingInitialFilter = args.initialFilter;
      if (args.openCamera) _pick(fromCamera: true);
    });
  }

  Future<void> _pick({bool fromCamera = false}) async {
    if (_busy) return;
    try {
      final xfile = await _picker.pickImage(
        source: fromCamera ? ImageSource.camera : ImageSource.gallery,
        maxWidth: _maxDim.toDouble(),
        maxHeight: _maxDim.toDouble(),
        imageQuality: 95,
      );
      if (xfile == null || !mounted) return;
      _setBusy(true);
      final bytes = await xfile.readAsBytes();
      final decoded = await compute(_decodeAndScale, bytes);
      if (decoded == null || !mounted) {
        _setBusy(false);
        _toast('Error loading image');
        return;
      }
      await _loadImage(decoded.$1, decoded.$2, decoded.$3);
    } catch (_) {
      if (mounted) _toast('Error loading image');
    } finally {
      _setBusy(false);
    }
  }

  static (Uint8List, int, int)? _decodeAndScale(Uint8List bytes) {
    final decoded = img.decodeImage(bytes);
    if (decoded == null) return null;
    var image = decoded;
    final maxSide = image.width > image.height ? image.width : image.height;
    if (maxSide > _maxDim) {
      image = img.copyResize(image, width: image.width >= image.height ? _maxDim : null,
          height: image.height > image.width ? _maxDim : null);
    }
    return (Uint8List.fromList(image.getBytes(order: img.ChannelOrder.rgba)),
        image.width, image.height);
  }

  Future<void> _loadImage(Uint8List rgba, int w, int h) async {
    final preview = await _toUiImage(rgba, w, h);
    final thumbs = await _buildThumbnails(rgba, w, h);
    if (!mounted) return;
      setState(() {
        _originalRgba = rgba;
        _width = w;
        _height = h;
        _preview = preview;
        _thumbnails = thumbs;
        _filter = FilterType.original;
        _intensity = 1.0;
      });
      final pending = _pendingInitialFilter;
      if (pending != null) {
        _pendingInitialFilter = null;
        await _selectFilter(pending);
      }
  }

  Future<Map<FilterType, ui.Image>> _buildThumbnails(
      Uint8List rgba, int w, int h) async {
    // ponytail: thumbnail dibuat sekali per gambar pada salinan kecil; kalau load terasa berat, cache per file.
    const thumbW = 96;
    final scale = thumbW / w;
    final th = (h * scale).round();
    final small = await compute(
      _resizeRgba,
      (rgba, w, h, thumbW, th),
    );
    final result = <FilterType, ui.Image>{};
    result[FilterType.original] = await _toUiImage(small, thumbW, th);
    for (final type in FilterType.values.skip(1)) {
      final out = await compute(
        _runJob,
        _FilterJob(small, thumbW, th, type, 1.0),
      );
      result[type] = await _toUiImage(out.rgba, thumbW, th);
    }
    return result;
  }

  static Uint8List _resizeRgba((Uint8List, int, int, int, int) args) {
    final (rgba, w, h, tw, th) = args;
    final src = img.Image.fromBytes(
        width: w, height: h, bytes: rgba.buffer, order: img.ChannelOrder.rgba);
    final resized = img.copyResize(src, width: tw, height: th);
    return Uint8List.fromList(resized.getBytes(order: img.ChannelOrder.rgba));
  }

  Future<ui.Image> _toUiImage(Uint8List rgba, int w, int h) {
    final completer = Completer<ui.Image>();
    ui.decodeImageFromPixels(
      rgba,
      w,
      h,
      ui.PixelFormat.rgba8888,
      completer.complete,
    );
    return completer.future;
  }

  Future<void> _selectFilter(FilterType type) async {
    if (_busy || _originalRgba == null) return;
    setState(() {
      _filter = type;
      if (type != FilterType.original) _intensity = 1.0;
    });
    await _render(type, _intensity);
  }

  Future<void> _onIntensityChanged(double value) async {
    if (_busy || _filter == FilterType.original) return;
    setState(() => _intensity = value);
    await _render(_filter, value);
  }

  Future<void> _render(FilterType type, double intensity) async {
    if (_originalRgba == null) return;
    _setBusy(true);
    try {
      final out = await compute(
        _runJob,
        _FilterJob(_originalRgba!, _width, _height, type, intensity),
      );
      final preview = await _toUiImage(out.rgba, out.width, out.height);
      if (!mounted) return;
      setState(() => _preview = preview);
    } catch (_) {
      if (mounted) _toast('Error applying filter');
    } finally {
      _setBusy(false);
    }
  }

  Future<void> _save() async {
    final rgba = _originalRgba;
    if (_busy || rgba == null) return;
    _setBusy(true);
    try {
      final jpeg = await compute(_encodeJpeg,
          (rgba, _width, _height, _filter.fileTag));
      await Gal.putImageBytes(
        jpeg,
        name: 'Meituy_${_filter.fileTag}_${_timestamp()}',
        album: 'Meituy',
      );
      if (mounted) _toast('Image saved to gallery');
    } on GalException catch (e) {
      if (mounted) _toast('Failed to save image: ${e.type.message}');
    } catch (e) {
      if (mounted) _toast('Failed to save image: $e');
    } finally {
      _setBusy(false);
    }
  }

  static Uint8List _encodeJpeg((Uint8List, int, int, String) args) {
    final (rgba, w, h, _) = args;
    final image = img.Image.fromBytes(
        width: w, height: h, bytes: rgba.buffer, order: img.ChannelOrder.rgba);
    return Uint8List.fromList(img.encodeJpg(image, quality: 90));
  }

  static String _timestamp() {
    final now = DateTime.now();
    String two(int n) => n.toString().padLeft(2, '0');
    return '${now.year}${two(now.month)}${two(now.day)}_${two(now.hour)}${two(now.minute)}${two(now.second)}';
  }

  void _reset() => _selectFilter(FilterType.original);

  void _setBusy(bool v) => mounted ? setState(() => _busy = v) : null;

  void _toast(String msg) =>
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(msg)));

  @override
  Widget build(BuildContext context) {
    final hasImage = _preview != null;
    return Scaffold(
      appBar: AppBar(
        title: Text(hasImage ? _filter.displayName : 'Edit Photo'),
        backgroundColor: Colors.black,
        actions: [
          if (hasImage)
            IconButton(icon: const Icon(Icons.restart_alt), tooltip: 'Reset', onPressed: _busy ? null : _reset),
          IconButton(
            icon: const Icon(Icons.auto_awesome),
            tooltip: 'AI Lab',
            onPressed: () => Navigator.pushNamed(context, '/ai-lab'),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(child: _buildPreview()),
          if (hasImage && _filter != FilterType.original)
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 8, 24, 0),
              child: Row(children: [
                const Text('Intensity'),
                Expanded(
                  child: Slider(
                    value: _intensity,
                    min: 0,
                    max: 1,
                    onChanged: _busy ? null : _onIntensityChanged,
                  ),
                ),
                Text('${(_intensity * 100).round()}%'),
              ]),
            ),
          if (hasImage) _buildFilterStrip(),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(children: [
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.upload),
                    label: const Text('Upload'),
                    onPressed: _busy ? null : () => _pick(),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.photo_camera),
                    label: const Text('Kamera'),
                    onPressed: _busy ? null : () => _pick(fromCamera: true),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton.icon(
                    icon: _busy
                        ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Icon(Icons.download),
                    label: const Text('Simpan'),
                    style: FilledButton.styleFrom(backgroundColor: Colors.orange),
                    onPressed: (_busy || !hasImage) ? null : _save,
                  ),
                ),
              ]),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPreview() {
    if (_preview == null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.image_outlined, size: 80, color: Colors.grey.shade700),
            const SizedBox(height: 12),
            Text('Belum ada foto. Upload atau jepret untuk mulai mengedit.',
                textAlign: TextAlign.center,
                style: TextStyle(color: Colors.grey.shade500)),
            const SizedBox(height: 24),
            FilledButton.icon(
              icon: const Icon(Icons.upload),
              label: const Padding(
                padding: EdgeInsets.symmetric(vertical: 12, horizontal: 8),
                child: Text('Upload Foto', style: TextStyle(fontSize: 16)),
              ),
              style: FilledButton.styleFrom(backgroundColor: Colors.orange),
              onPressed: _busy ? null : () => _pick(),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              icon: const Icon(Icons.photo_camera),
              label: const Text('Kamera'),
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.amberAccent,
                side: const BorderSide(color: Colors.amberAccent),
              ),
              onPressed: _busy ? null : () => _pick(fromCamera: true),
            ),
          ],
        ),
      );
    }
    return Stack(
      alignment: Alignment.center,
      children: [
        InteractiveViewer(maxScale: 5, child: RawImage(image: _preview)),
        if (_busy) const CircularProgressIndicator(),
      ],
    );
  }

  Widget _buildFilterStrip() {
    final thumbs = _thumbnails;
    if (thumbs == null) return const SizedBox(height: 104);
    return SizedBox(
      height: 104,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 12),
        itemCount: FilterType.values.length,
        separatorBuilder: (_, _) => const SizedBox(width: 8),
        itemBuilder: (context, i) {
          final type = FilterType.values[i];
          final selected = type == _filter;
          return GestureDetector(
            onTap: _busy ? null : () => _selectFilter(type),
            child: Column(
              children: [
                Container(
                  width: 72,
                  height: 72,
                  decoration: BoxDecoration(
                    border: Border.all(
                      color: selected ? Colors.orange : Colors.grey.shade800,
                      width: selected ? 3 : 1,
                    ),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: RawImage(image: thumbs[type], fit: BoxFit.cover),
                  ),
                ),
                const SizedBox(height: 4),
                SizedBox(
                  width: 76,
                  child: Text(
                    type.displayName,
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                        fontSize: 11,
                        color: selected ? Colors.orange : Colors.grey.shade400),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}
