import 'package:flutter/material.dart';

import 'editor_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Meituy'),
        backgroundColor: Colors.black,
        actions: [
          IconButton(
            icon: const Icon(Icons.auto_awesome),
            tooltip: 'AI Lab',
            onPressed: () => Navigator.pushNamed(context, '/ai-lab'),
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Icon(Icons.photo_camera_outlined,
                size: 96, color: Colors.orange.shade300),
            const SizedBox(height: 8),
            const Text(
              'Photo Editor',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            Text(
              'Semua filter bekerja offline di device Anda',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey.shade500),
            ),
            const SizedBox(height: 48),
            FilledButton.icon(
              icon: const Icon(Icons.edit),
              label: const Padding(
                padding: EdgeInsets.symmetric(vertical: 14),
                child: Text('Edit Photo', style: TextStyle(fontSize: 16)),
              ),
              style: FilledButton.styleFrom(backgroundColor: Colors.orange),
              onPressed: () => Navigator.pushNamed(context, '/editor'),
            ),
            const SizedBox(height: 16),
            OutlinedButton.icon(
              icon: const Icon(Icons.photo_camera),
              label: const Padding(
                padding: EdgeInsets.symmetric(vertical: 14),
                child: Text('Camera', style: TextStyle(fontSize: 16)),
              ),
              style: OutlinedButton.styleFrom(
                foregroundColor: Colors.amberAccent,
                side: const BorderSide(color: Colors.amberAccent),
              ),
              onPressed: () => Navigator.pushNamed(context, '/editor',
                  arguments: EditorArgs(openCamera: true)),
            ),
          ],
        ),
      ),
    );
  }
}
