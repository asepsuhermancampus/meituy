import 'package:flutter/material.dart';

import 'screens/ai_lab_screen.dart';
import 'screens/editor_screen.dart';
import 'screens/home_screen.dart';

void main() {
  runApp(const MeituyApp());
}

class MeituyApp extends StatelessWidget {
  const MeituyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Meituy',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: Colors.black,
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.orange,
          brightness: Brightness.dark,
        ),
      ),
      initialRoute: '/',
      routes: {
        '/': (_) => const HomeScreen(),
        '/editor': (_) => const EditorScreen(),
        '/ai-lab': (_) => const AiLabScreen(),
      },
    );
  }
}
