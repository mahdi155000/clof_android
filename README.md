# CLOF Android

CLOF Android is an offline movie and TV-series collection manager. It stores the
library locally on the device, supports collection and genre organization, and
provides database-file backup and import so a library can be moved between CLOF
installations.

## Features

- Add and edit movies and series.
- Track series season and episode progress.
- Mark items watched, favorite, pinned, or unrated/rated from 1 to 5.
- Add notes, genres, and collections.
- Use the built-in `main` and `watched` collections, plus custom collections.
- Search, filter, sort, and view collection summaries.
- Move items between collections.
- Move items to Trash, restore them, or permanently delete them.
- Manage collections and genres, including renaming and removal.
- Toggle light and dark mode.
- Export the local Room database to a file.
- Import a compatible CLOF SQLite database while skipping duplicate titles.

## Architecture

The app is a single Android application module using Kotlin and Jetpack Compose.

```text
Compose screens
    |
    v
MovieViewModel
    |
    v
Repositories
    |
    v
Room DAOs
    |
    v
SQLite database: clof.db
```

- `MainActivity` initializes the Compose content and persists the dark-mode
  preference through `SettingsManager`.
- `ClofApp` owns navigation, the navigation drawer, dialogs, and database
  import/export launchers.
- `MovieViewModel` exposes database state as `StateFlow` and coordinates
  asynchronous operations in `viewModelScope`.
- `MovieRepository`, `CollectionRepository`, and `GenreRepository` keep database
  access separate from the UI.
- `AppDatabase` defines the Room database, default data, and migrations through
  version 10.
- `ClofDatabaseExporter` writes a checkpointed copy of `clof.db`.
- `ClofDatabaseImporter` reads a compatible SQLite `movies` table, normalizes
  titles, adds missing collections/genres, and skips duplicate titles.

## Android Studio setup

### Requirements

- Android Studio with Android SDK Platform 37 and Build Tools installed.
- JDK 17 or newer for the Android Gradle Plugin; the app source and bytecode
  compatibility are configured for Java 11.
- An Android device or emulator running API 27 or newer.
- Internet access on the first build so Gradle can download the configured
  Gradle and Maven dependencies.

### Open the project

1. Clone or copy this repository.
2. Open `D:\Android\StudioProjects` (the repository root) in Android Studio.
3. Allow Gradle sync to complete.
4. Install SDK Platform 37 if Android Studio reports it is missing.
5. Select the `app` run configuration and an API 27+ device or emulator.
6. Run the project.

The application ID is `com.mahdi155000.clof_android`.

### Signing configuration

Both debug and release variants use the `clof` signing configuration defined in
`app/build.gradle.kts`. The signing file is intentionally ignored by Git. Before
building locally, provide a root-level `keystore.properties` file containing the
values expected by the build script:

```properties
storeFile=path/to/clof.keystore
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

Keep the keystore and passwords private. Do not commit either the keystore or
`keystore.properties`.

## Build and test commands

Run these commands from the repository root in PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
.\gradlew.bat test
.\gradlew.bat connectedCheck
```

`connectedCheck` requires a running emulator or connected Android device.

Useful targeted commands:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

## Database structure

The database file is named `clof.db`, is managed by Room, and currently uses
schema version 10. Room schema snapshots are stored under `app/schemas`.

### `movies`

| Column | SQLite type | Description |
| --- | --- | --- |
| `id` | INTEGER, primary key | Auto-generated movie or series ID |
| `title` | TEXT, required | Unique for insert/update matching, case-insensitive after trimming |
| `createdAt` | INTEGER, required | Creation time in milliseconds since Unix epoch |
| `genre` | TEXT, required | Comma-separated genre names |
| `isSeries` | INTEGER, required | Boolean flag stored as `0` or `1` |
| `season` | INTEGER, required | Current season; `0` for movies |
| `episode` | INTEGER, required | Current episode; `0` for movies |
| `watched` | INTEGER, required | Boolean flag stored as `0` or `1` |
| `collection` | TEXT, required | Collection name, normally `main` or `watched` |
| `inTrash` | INTEGER, required | Boolean soft-delete flag stored as `0` or `1` |
| `notes` | TEXT, nullable | Optional personal notes |
| `pinned` | INTEGER, required | Boolean flag stored as `0` or `1` |
| `customOrder` | INTEGER, required | User-defined ordering value |
| `personalRating` | INTEGER, nullable | Optional rating from 1 through 5 |
| `favorite` | INTEGER, required | Boolean flag stored as `0` or `1` |

### `collections`

| Column | SQLite type | Description |
| --- | --- | --- |
| `name` | TEXT, primary key | Collection name |

The reserved collections are `main` and `watched`. They cannot be renamed or
removed. New installations also seed the default genres defined in
`AppDatabase.defaultGenres`.

### `genres`

| Column | SQLite type | Description |
| --- | --- | --- |
| `name` | TEXT, primary key | Genre name |

Room also maintains its internal `room_master_table`. Database migrations from
versions 1 through 9 are registered in `AppDatabase` and are validated by the
instrumentation tests.

## Import and export format

Import and export use a SQLite database file, not CSV or JSON.

### Export

Use **Settings > Export database** or the export action in the app bar. The
document picker suggests the filename `clof-export.db`. The exporter checkpoints
the WAL and copies the complete `clof.db` file to the selected destination.

### Import

Use **Settings > Import database** and select a compatible `.db` file. The
importer reads the `movies` table and requires a `title` column. It supports the
current camelCase column names and legacy snake_case aliases for fields such as
`createdAt`/`created_at`, `isSeries`/`is_series`, `customOrder`/`custom_order`,
and `personalRating`/`personal_rating`.

The following columns are recognized:

```text
title                         required
createdAt or created_at       optional INTEGER timestamp
genre                         optional TEXT, comma-separated
isSeries or is_series         optional INTEGER boolean
season                        optional INTEGER
episode                       optional INTEGER
watched                       optional INTEGER boolean
collection                    optional TEXT
notes                         optional TEXT
pinned                        optional INTEGER boolean
customOrder or custom_order   optional INTEGER
personalRating or personal_rating
                              optional INTEGER, constrained to 1..5
favorite                      optional INTEGER boolean
```

Blank titles are ignored. Titles are trimmed and compared case-insensitively,
so duplicates already in the app and duplicates within the imported file are
skipped. Missing collections and genres are created automatically. Blank
collections fall back to `main`; series season and episode values are
normalized to positive values.

For reliable backups, export from the app rather than copying the live
database while it is open.

## Generate an APK

### Debug APK

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is generated at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Install it with Android Debug Bridge:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Release APK

Ensure `keystore.properties` and the referenced keystore are configured, then
run:

```powershell
.\gradlew.bat assembleRelease
```

The release build enables R8 shrinking and resource shrinking. The signed APK
is generated at:

```text
app\build\outputs\apk\release\app-release.apk
```

Install the release APK with:

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

For a distributable release, verify the signing key, version code, version name,
and the APK output before sharing it.
