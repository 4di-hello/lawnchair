**TL;DR:** Christians Lawnchair-Fork, Stand 23.11.2026, Korrekturen an der Sicherung. Gesten auf App-Symbolen funktionieren jetzt in alle vier Richtungen, auch auf Ordnern und pro Element. Sie können auch Verknüpfungen öffnen, etwa einen bestimmten Chat. Ins Internet geht der Launcher nur noch für Updates.

## Neu in diesem Stand
- **Sicherung enthält die letzten Änderungen:** Änderungen an Startbildschirm, Symbolen und Ordnern der App-Übersicht konnten noch im Zwischenspeicher der Datenbank liegen und fehlten dann in der Sicherung. Vor dem Sichern werden sie jetzt in die Datenbank übernommen.
- **Wiederherstellen behält die Einstellungen:** Bei einem anderen Raster als in der Sicherung konnte die Wiederherstellung einen Teil der Einstellungen, z. B. das Icon-Pack, mit den alten Werten überschreiben.
- **Widget-Einrichtung nach dem Wiederherstellen** startet jetzt zuverlässig.

## Was der Fork gegenüber Lawnchair 16 kann
- **Wischen nach links und rechts** auf App-Symbolen funktioniert, auf dem Startbildschirm, im Dock und in Ordnern. Im offiziellen Lawnchair lässt sich das einstellen, löst aber nichts aus.
- **Verknüpfungen als Gestenziel:** Eine Geste kann statt einer App auch eine App-Verknüpfung öffnen. Ist die Verknüpfung nicht mehr verfügbar, erscheint ein Hinweis.
- **Gesten pro Element:** Dieselbe App kann auf dem Startbildschirm und im Dock unterschiedliche Gesten haben. Einrichten über langes Drücken, dann „Anpassen“. Ohne eigene Geste gilt die Geste der App.
- **Gesten auf Ordnern:** Ordner lange drücken, dann „Gesten“. Hoch, runter, links und rechts sind einzeln belegbar.
- **Internet nur für Updates:**
  - Abgeschaltet sind Websuche-Vorschläge, Ankündigungen von Lawnchair, der Abruf der Mitwirkenden und der Log-Upload.
  - Der Updater prüft nur die Releases dieses Forks.
- **Weniger Berechtigungen:** Lesezeichen und Verlauf sowie der Vordergrunddienst sind entfernt.
- **Widgets nach dem Wiederherstellen:** Nach einer Wiederherstellung öffnet der Launcher den Einrichtungsdialog der Widgets nacheinander, statt dass jedes einzeln angetippt werden muss.
- **Automatisch aktuell:** Jeden Montag wird der Fork mit Lawnchair abgeglichen und neu gebaut. Der Updater in den Einstellungen unter „Über“ zeigt die neue Version an.

## Installieren
- **Offizielle Lawnchair Nightly installiert:**
  1. Dort eine Sicherung erstellen und Lawnchair deinstallieren.
  2. Diese APK installieren und die Sicherung wiederherstellen.

  Beide Apps haben denselben Paketnamen, aber eine andere Signatur.
- **Play Protect blockiert:** „Apps mit Play Protect scannen“ kurz ausschalten, installieren und danach wieder einschalten.
