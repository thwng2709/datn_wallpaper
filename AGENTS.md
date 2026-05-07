# AGENTS.md

## Project snapshot
- Android app in `app/` with one module (`settings.gradle` includes only `:app`).
- Main namespace/package is `com.tkd.wallpaper`; the launcher flow starts in `App.kt` and `ui/component/splash/view/SplashLoadingActivity.kt` → `ui/component/MainActivity.kt`.
- Core stacks: Hilt DI, Room, WorkManager, Firebase (Analytics, Crashlytics, Remote Config, Messaging, Storage, App Check), Glide, data binding, plus PuppyAds/AdMob mediation, Facebook SDK, UMP, Media3, and PRDownloader.

## Architecture map
- `App.kt` bootstraps Firebase App Check, PRDownloader, ad initialization, `TopActivityHolder`, and daily onboarding re-entry logic.
- `ui/base/BaseActivity.kt` and `ui/base/BaseFragment.kt` are the house style for screens: they wire navigation, locale updates, keyboard helpers, and download/permission handling.
- Data access is layered through repositories in `repository/` backed by DAOs in `data/dao/` and entities in `data/entity/`.
- Shared state lives in `local/LocalStorage.kt` / `local/LocalData.kt`; remote config and region logic live in `remote/RemoteConfig.kt` and `data/CommonInfo`.
- `ui/component/*` is split by feature: `splash/view`, `home/{fragment,adapter,viewmodel}`, `history/{fragment,adapter,model,viewmodel}`, `download/fragment`, `permission`, `preview`, `search`, `setting/view`, `viewWallpaper`, `wallpaperByCat`, and `zipper/zipperlock`; non-UI code lives in `notification/localNotification/*`, `server/Network.kt`, `service/SettingWallpaperService.kt`, and `workManager/LocaleSyncWorker.kt`.

## Conventions to follow
- Keep language changes consistent with the existing pattern: `attachBaseContext()` plus `AppConfig.updateResourcesLocale(...)` / `AppConfig.updateResources(...)`, then refresh in `onResume()` when needed.
- Prefer repository calls over direct DAO access from UI; `StorageModule.kt` already binds `WallpapersRepositoryImp`, `CategoriesRepositoryImp`, and `ZipperRepositoryImp`.
- Feature folders commonly separate `fragment/`, `adapter/`, `viewmodel/`, and `view/` subpackages; follow that layout when adding or moving UI code.
- Keep event/logging keys centralized in `utils/Constants.kt` and analytics calls routed through `AppConfig.logEventTracking(...)`.
- Resource names are localized per `res/values-xx/strings.xml`; many display strings are intentionally `translatable="false"` for language labels and product names.

## Integration points
- FCM behavior is in `notification/MyFirebaseMessagingService.kt`; local/alarm notifications live in `notification/localNotification/AlarmReceiver.kt` and `MyAlarmManager.kt`. Notifications suppress when `localStorage.isNotification` is false, use topic names like `weekly_<lang>`, and deep-link `myapp://view_wallpaper`.
- Locale sync is handled by `workManager/LocaleSyncWorker.kt`, which fetches Firebase Storage JSON, writes Room tables in transactions, and keeps category ordering via `localStorage.categorySort`.
- Manifest-critical components include `SettingWallpaperService`, the zipper foreground services, the `androidx.startup.InitializationProvider` setup, the alarm receiver, and the `FileProvider` paths in `res/xml/provider_paths.xml`.

## Build / debug workflow
- Use `./gradlew :app:assembleAppDevDebug` for the current dev flavor (`appDev` in `app/build.gradle`); the app currently compiles/targets SDK 35 and Java 11.
- `./gradlew :app:bundleRelease` also triggers a custom `buildApks` task that expects `BUNDLETOOL_JAR` from `gradle.properties` and keystore data under `~/TKKeyStore/AutoClicker/keystore.properties`.
- Release builds are minified/shrunk; debug builds are not. `repositoriesMode` is `FAIL_ON_PROJECT_REPOS`, so add dependencies in Gradle files only.
- There is no `src/test` or `src/androidTest` tree in the repo right now, so validation is mainly build-time/manual.

## Edit carefully
- Treat `AndroidManifest.xml`, `app/build.gradle`, `App.kt`, `MainActivity.kt`, `ui/component/splash/view/SplashLoadingActivity.kt`, `ui/component/setting/view/SettingActivity.kt`, `notification/localNotification/AlarmReceiver.kt`, `server/Network.kt`, and `Constants.kt` as high-impact files.
- Preserve existing package structure and screen naming under `ui/component/*`; many flows depend on exact activity/fragment class names and string keys.
- If you add a new screen, check whether it also needs locale refresh, analytics events, permission prompts, or a manifest entry.

