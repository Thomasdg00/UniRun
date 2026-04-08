# UniRun

App Android di fitness tracking sviluppata per il corso di Programmazione Mobile.

## Tech Stack
- Kotlin + Android SDK (API 26+)
- MVVM + Repository Pattern
- Room (SQLite locale)
- Firebase Authentication + Cloud Firestore
- Mapbox SDK v11 (mappe e polyline)
- FusedLocationProviderClient (GPS)
- Kotlin Coroutines + StateFlow
- MPAndroidChart (grafici velocità)
- DataStore (preferenze utente)

## Setup
1. Clona il repository
2. Aggiungi `google-services.json` nella cartella `app/` (non incluso per sicurezza)
3. Crea o modifica `local.properties` nella root del progetto con:
   ```properties
   # Token pubblico (pk.*): usato nell'app a runtime per caricare le mappe
   MAPBOX_PUBLIC_TOKEN=pk.eyJ...

   # Token segreto (sk.*): usato da Gradle per scaricare le dipendenze Mapbox SDK
   MAPBOX_SECRET_TOKEN=sk.eyJ...
   ```
   - `MAPBOX_PUBLIC_TOKEN` → https://account.mapbox.com → Tokens → token pubblico (inizia con `pk.`)
   - `MAPBOX_SECRET_TOKEN` → https://account.mapbox.com → Tokens → token segreto con scope `DOWNLOADS:READ` (inizia con `sk.`)
4. Attiva Firebase Authentication (Email/Password) nella Firebase Console
5. Sincronizza Gradle e avvia su **dispositivo fisico** (il GPS non funziona correttamente sull'emulatore)

## Note di Sviluppo
- `local.properties` e `google-services.json` sono nel `.gitignore` — non vengono mai committati
- Per testare il tracking GPS usa un dispositivo reale e muoviti fisicamente

## Licenza
MIT – vedi file [LICENSE](LICENSE)
