# Dialer Guru

**Dialer Guru** is a classic, senior-friendly Android Default Dialer replacement designed for elderly and non-reading users. It replaces complex menus and tiny number pads with a high-contrast **2-column photo grid**, **vintage mechanical rotary sound effects**, **Text-to-Speech voice prompts**, and a **full-screen Yes/No confirmation screen** to prevent accidental pocket dials.

## Key Features

- **Automatic Contact & Portrait Loading**: Automatically queries Android's `ContactsContract` provider, deduplicating phone numbers and sorting **Starred (Favorite)** contacts and contacts with photos to the top.
- **Classic 2-Column Photo Grid**: High-contrast warm ivory (`#FDF8EE`) and vintage brass (`#C5A059`) card layout with tactile spring-scale press animations (`animateFloatAsState`).
- **Accidental-Tap Protection**: Tapping any contact card opens a full-screen portrait confirmation overlay with oversized **YES** (Green) and **NO** (Red) buttons.
- **Rotary Sound & Voice Announcements**: Synthesizes a mechanical rotary pulse train in memory using 16-bit PCM `AudioTrack` (zero external MP3 dependencies) and announces `"Call [Name]? Tap green for Yes, or red for No"` using Android `TextToSpeech` at a clear `0.9x` speech rate.
- **Full Default Dialer Replacement (`ROLE_DIALER` & `InCallService`)**: Implements `GuruInCallService` and `InCallActivity` to handle incoming and ongoing phone calls with giant **ANSWER**, **HANG UP**, and **SPEAKER** buttons, even over the lock screen.

## Project Structure

```text
Dialer-Guru/
├── settings.gradle.kts
├── build.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/example/gurudialer/
            ├── ContactsRepository.kt
            ├── SoundAndSpeechHelper.kt
            ├── DialerScreen.kt
            ├── GuruInCallService.kt
            ├── InCallActivity.kt
            └── MainActivity.kt
```

## Getting Started in Android Studio

1. Clone this repository and open it in **Android Studio (Jellyfish / Koala or newer)**.
2. Sync Gradle (`Minimum SDK: API 26 / Android 8.0`, `Target SDK: 34`).
3. Run the app on a physical Android device.
4. Grant **Contacts** and **Phone** permissions when prompted, and tap **Set Default Phone** in the top bar to register **Dialer Guru** as your device's default dialer.
