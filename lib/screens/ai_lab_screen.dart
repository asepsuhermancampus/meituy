import 'package:flutter/material.dart';

import '../models/filter_type.dart';
import 'editor_screen.dart';

class AiLabScreen extends StatelessWidget {
  const AiLabScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final cards = [
      (Icons.auto_fix_high, 'AI Enhance', 'Auto improve your photo',
          () => Navigator.pushNamed(context, '/editor')),
      (Icons.face_retouching_natural, 'AI Portrait', 'Skin smooth + bokeh',
          () => _open(context, FilterType.aiPortrait)),
      (Icons.style, 'Style Studio', 'Preset styles one tap',
          () => Navigator.pushNamed(context, '/editor')),
      (Icons.wb_sunny_outlined, 'AI Relight', 'Warm relighting effect',
          () => _open(context, FilterType.aiRelight)),
    ];

    return Scaffold(
      appBar: AppBar(title: const Text('AI Lab'), backgroundColor: Colors.black),
      body: GridView.count(
        crossAxisCount: 2,
        padding: const EdgeInsets.all(16),
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        children: [
          for (final (icon, title, subtitle, onTap) in cards)
            _Card(
              icon: icon,
              title: title,
              subtitle: subtitle,
              onTap: onTap,
            ),
        ],
      ),
    );
  }

  void _open(BuildContext context, FilterType filter) {
    Navigator.pushNamed(
      context,
      '/editor',
      arguments: EditorArgs(initialFilter: filter),
    );
  }
}

class _Card extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _Card({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.grey.shade900,
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 40, color: Colors.orange.shade300),
              const SizedBox(height: 12),
              Text(title,
                  style: const TextStyle(
                      fontSize: 16, fontWeight: FontWeight.bold)),
              const SizedBox(height: 4),
              Text(subtitle,
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade500)),
            ],
          ),
        ),
      ),
    );
  }
}
