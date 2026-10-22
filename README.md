# AnimeFix Lite

Lightweight Android movie/anime catalogue prototype, minimum Android 6.0 (API 23).

## Build
Open GitHub **Actions** → **Build AnimeFix Lite APK** → **Run workflow**. After a successful run, download the `AnimeFixLite-debug-apk` artifact. Extract the ZIP to get `app-debug.apk`.

## Catalogue JSON
The app accepts an HTTPS URL returning a JSON array, or an object with a `movies` or `results` array. Each entry supports:
- `title` or `name`
- `language` or `lang`
- `year`
- `url`, `stream_url`, or `link`

Example:
```json
[
  {"title":"Example Anime","language":"Japanese","year":"2025","url":"https://example.com/watch"}
]
```

Only use catalogues and media you are authorized to access. This prototype is a catalogue app, not a bundled streaming library; it does not bypass logins, DRM, paywalls, or source restrictions.
