enum FilterType {
  original('Original'),
  riconFlash('Ricon Flash'),
  flashFilm('Flash Film'),
  g7x('G7X'),
  fujiFlash('Fuji Flash'),
  goldenHour('Golden Hour'),
  matahariTerbenam('Matahari Terbenam'),
  lampuKilatIphone('Lampu Kilat iPhone'),
  aiPortrait('AI Portrait'),
  aiRelight('AI Relight');

  final String displayName;
  const FilterType(this.displayName);

  String get fileTag => displayName.replaceAll(' ', '_');
}
