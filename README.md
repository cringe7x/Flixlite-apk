# AnimeFlix Lite — Android project

This is an Android WebView wrapper around the lightweight AnimeFlix Lite prototype.

## Build an APK
The build runner used to prepare this ZIP does not have Android SDK/Gradle installed, so an APK has not been compiled here. The included GitHub Actions workflow builds a debug APK for you.

1. Create a GitHub repository and upload the contents of this folder.
2. Open the repository's **Actions** tab.
3. Run **Build AnimeFlix Lite APK** (or push to the `main` branch).
4. Open the completed workflow run and download the `AnimeFlixLite-debug-apk` artifact.
5. Extract the ZIP and install `app-debug.apk` on your Android phone. You may need to allow installation from that source.

## Notes
- This is a prototype shell, not a finished streaming service.
- The demo movie cards are placeholders.
- Source loading needs a compatible HTTPS JSON catalog/API. Some source servers block cross-origin requests; those require a proper backend/API adapter.
- Only use media/catalog sources you are authorized to access.
