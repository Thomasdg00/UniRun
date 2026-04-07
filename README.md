# UniRun

App Android di fitness tracking sviluppata per il corso di Programmazione Mobile.

## Tech Stack
- Kotlin + Android SDK (API 26+)
- MVVM + Repository Pattern
- Room (SQLite locale)
- Firebase Authentication + Cloud Firestore
- Mapbox SDK (mappe e polyline)
- FusedLocationProviderClient (GPS)
- Kotlin Coroutines + StateFlow

## Setup
1. Clona il repository
2. Aggiungi `google-services.json` nella cartella `app/` (non incluso per sicurezza)
3. Aggiungi il tuo Mapbox token in `local.properties`:
   ```properties
   MAPBOX_DOWNLOADS_TOKEN=sk.eyJ...
   MAPBOX_ACCESS_TOKEN=pk.eyJ...
   ```
4. Sincronizza Gradle e avvia su dispositivo fisico (il GPS non funziona sull'emulatore)

## Licenza
MIT – vedi file [LICENSE](LICENSE)
