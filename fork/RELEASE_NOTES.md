**TL;DR:** Christians Lawnchair-Fork, Stand 23.11.2026. Gesten auf App-Symbolen funktionieren jetzt in alle vier Richtungen, auch auf Ordnern und pro Element. Sie können auch Verknüpfungen öffnen, etwa einen bestimmten Chat. Ins Internet geht der Launcher nur noch für Updates.

## Neu in diesem Stand
- **Gesten im Dock** funktionieren jetzt auch nach oben und unten.
- **Tasker-Tasks und andere selbst erstellte Verknüpfungen** als Gestenziel: Geste → „Verknüpfung einer anderen App öffnen“ → „Verknüpfung erstellen“.
- **Verknüpfungen nur für Gesten** bleiben erhalten, auch wenn sie nicht auf dem Startbildschirm liegen.
- **App für eine Geste wählen:** mit Suchfeld.
- **Eingefügte Verknüpfungen** (z. B. ein Claude-Chat oder ein Discord-Kontakt): Lange drücken → „Anpassen“ zum Umbenennen, Symbol ändern und Gesten einrichten.
- **Wiederherstellen:** Die Einrichtungsdialoge der Widgets öffnen sich nicht mehr von selbst nacheinander.
- **Automatische Sicherung:** Einstellungen → Sichern und Wiederherstellen. Ordner wählen, Abstand und Anzahl einstellen.
- **Gesten auf Symbolen:** Übersicht aller Wischgesten unter Einstellungen → Gesten, zum Ändern und Löschen.
- **Launcher-Einstellungen in der App-Suche:** z. B. „Symbolgröße“ in der App-Übersicht eintippen.
- **Suche in den Einstellungen:** Lupe oben im Einstellungsmenü. Ein Treffer öffnet die Seite und hebt die Einstellung kurz hervor.
- **Wisch-Hinweis:** Kleine Striche neben Symbolen und Ordnern zeigen, in welche Richtung eine Geste liegt. Abschaltbar unter Startbildschirm → Symbole.
- **Ordner:** Lange drücken bietet jetzt „Umbenennen“ und „Symbol ändern“ (Symbol aus einem Icon-Pack).
- **Verknüpfungen anderer Apps:** nach App gruppiert, zum Aufklappen, mit Suche.
- **Gesten pro Symbol:** Übernommene Gesten der App sind mit „(von der App)“ markiert.
- **Icon-Größe in 5-%-Schritten.**
- **Sicherung:** Die letzten Änderungen kommen mit, und das Wiederherstellen überschreibt keine Einstellungen mehr.

## Was der Fork gegenüber Lawnchair 16 kann
- **Wischen nach links und rechts** auf App-Symbolen funktioniert, auf dem Startbildschirm, im Dock und in Ordnern. Im offiziellen Lawnchair lässt sich das einstellen, löst aber nichts aus.
- **Verknüpfungen als Gestenziel:** Eine Geste kann statt einer App auch eine App-Verknüpfung öffnen. Ist die Verknüpfung nicht mehr verfügbar, erscheint ein Hinweis.
- **Gesten pro Element:** Dieselbe App kann auf dem Startbildschirm und im Dock unterschiedliche Gesten haben. Einrichten über langes Drücken, dann „Anpassen“. Ohne eigene Geste gilt die Geste der App.
- **Gesten auf Ordnern:** Ordner lange drücken, dann „Gesten“. Hoch, runter, links und rechts sind einzeln belegbar.
- **Internet nur für Updates:**
  - Abgeschaltet sind Websuche-Vorschläge, Ankündigungen von Lawnchair, der Abruf der Mitwirkenden und der Log-Upload.
  - Der Updater prüft nur die Releases dieses Forks.
- **Weniger Berechtigungen:** Lesezeichen und Verlauf sowie der Vordergrunddienst sind entfernt.
- **Automatisch aktuell:** Jeden Montag wird der Fork mit Lawnchair abgeglichen und neu gebaut. Der Updater in den Einstellungen unter „Über“ zeigt die neue Version an.

## Installieren
- **Offizielle Lawnchair Nightly installiert:**
  1. Dort eine Sicherung erstellen und Lawnchair deinstallieren.
  2. Diese APK installieren und die Sicherung wiederherstellen.

  Beide Apps haben denselben Paketnamen, aber eine andere Signatur.
- **Play Protect blockiert:** „Apps mit Play Protect scannen“ kurz ausschalten, installieren und danach wieder einschalten.
