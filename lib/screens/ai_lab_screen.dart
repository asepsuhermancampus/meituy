import 'package:flutter/material.dart';

class AiLabScreen extends StatelessWidget {
  const AiLabScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // ponytail: AI Portrait & AI Relight masih placeholder; butuh ML Kit/face detection, tambah saat fitur dipesan.
    final cards = [
      (Icons.auto_fix_high, 'AI Enhance', 'Auto improve your photo'),
      (Icons.face_retouching_natural, 'AI Portrait', 'Coming soon'),
      (Icons.style, 'Style Studio', 'Preset styles one tap'),
      (Icons.wb_sunny_outlined, 'AI Relight', 'Coming soon'),
    ];

    return Scaffold(
      appBar: AppBar(title: const Text('AI Lab'), backgroundColor: Colors.black),
      body: GridView.count(
        crossAxisCount: 2,
        padding: const EdgeInsets.all(16),
        mainAxisSpacing: 16,
        crossAxisSpacing: 16,
        children: [
          for (final (icon, title, subtitle) in cards)
            _Card(
              icon: icon,
              title: title,
              subtitle: subtitle,
              onTap: () {
                if (title == 'AI Portrait' || title == 'AI Relight') {
                  ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('$title coming soon')));
                  return;
                }
                Navigator.pushNamed(context, '/editor');
              },
            ),
        ],
      ),
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
