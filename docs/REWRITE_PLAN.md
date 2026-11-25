# План переписування ProPodcast → ProPodcastN

Джерело: https://github.com/PrimakPavel/ProPodcast (Udacity Capstone, 2019, ~46 комітів).
Ціль: перенести основний функціонал на сучасний стек — **Jetpack Compose** для UI, **Hilt** для DI.

---

## 1. Що є в старому проекті

### Дані
- **API:** Listen Notes REST v2 (`https://listen-api.listennotes.com/api/v2/`, заголовок `X-ListenAPI-Key`, ключ з `app/api.properties` → `BuildConfig.API_KEY`).

| Ендпоінт | Для чого |
|---|---|
| `GET genres` | фільтр жанрів |
| `GET regions` | фільтр регіонів |
| `GET best_podcasts?genre_id&page&region` | головний екран, посторінково |
| `GET podcasts/{id}?next_episode_pub_date&sort` | деталі подкасту + епізоди (довантаження за датою) |
| `GET podcasts/{id}/recommendations` | рекомендації на екрані деталей |
| `GET search?q&type=podcast&offset&language` | пошук, посторінково через offset |

- Список мов **захардкоджений** у `ServerMocksUtil` (у API є `GET languages`, але його не використовували).
- Обробка помилок API: 401 (ключ), 404, 429 (ліміт безкоштовного плану), 500.
- **Room** (`db_podcasts`, v1): `favorite_podcasts`, `favorite_episodes` (жанри — через TypeConverter).
- **SharedPreferences:** обрані фільтри (жанр / регіон / мова), останній трек для віджета й «продовжити».

### Екрани (Fragments + Navigation XML, нижня навігація з 3 вкладок)
1. **Best Podcasts** (стартовий): посторінковий список, кнопка фільтрів у тулбарі, popup «в обране / поділитися».
2. **Filters**: вкладки Жанр / Регіон (ViewPager), вибір зберігається в prefs.
3. **Search**: поле пошуку + фільтр мови, посторінкові результати.
4. **Podcast Details**: шапка подкасту, епізоди з «Завантажити ще», рекомендації (горизонтальний список), popup для подкасту й епізоду (обране / поділитися), тап по епізоду → відтворення.
5. **Favorites**: вкладки Подкасти / Епізоди з Room.
6. **Player**: панель у `MainActivity` над нижньою навігацією (обкладинка, назва, play/pause, seekbar, стоп) + FAB «продовжити останній трек».

### Фон і система
- **PlayerService** (foreground) + ExoPlayer 2.10, власні `MediaSessionHelper`, нотифікація, `AudioFocusHelper`; команди через Intent extras (`START_TRACK`, `PLAY`, `PAUSE`, `SEEK`, `STOP`, `CONTINUE_LAST_TRACK`); зв’язок з UI через **Otto event bus**.
- **App Widget** на домашній екран: останній трек, play / pause / відновити / стоп.
- **Firebase:** Analytics, Crashlytics (Fabric, мертвий SDK), FCM push (просто показує нотифікацію).
- Окремі layout'и для планшетів (`sw600dp`: сітки).

### Стек, від якого відмовляємось
Koin 1.0, RxJava2, LiveData + Paging 2, Kotlin synthetics, Otto, Picasso, Stetho, Multidex, Fabric Crashlytics, власні `AppExecutors`, частина коду ще на Java.

> ⚠️ У публічному старому репо лежать `keystore.jks` і `google-services.json`. У новий проект їх не переносимо; варто прибрати зі старого репо й не використовувати той keystore для нових релізів.

---

## 2. Відповідність технологій

| Було | Стане |
|---|---|
| Fragments + XML + synthetics | Compose + Material 3 |
| Navigation XML | Navigation Compose з type-safe routes (`@Serializable`) |
| BottomNavigationView, layouts `sw600dp` | `NavigationSuiteScaffold` (bottom bar / rail) + `WindowSizeClass`, адаптивні сітки |
| Koin | **Hilt** (+ `hilt-navigation-compose`), KSP |
| RxJava2 + LiveData + `StatesBatch` | Coroutines + `Flow` / `StateFlow`, UI state як `sealed`/data class |
| Paging 2 + DataSource | Paging 3 + `paging-compose` (`PagingSource` для best/search) |
| Retrofit 2.5 + Moshi reflection | Retrofit 3 + kotlinx.serialization, OkHttp 5, interceptor з API-ключем |
| Room 2.1 + LiveData | Room (KSP) + Flow, експорт схеми |
| SharedPreferences | DataStore Preferences |
| ExoPlayer 2 + власний сервіс + Otto + MediaSession/AudioFocus вручну | **Media3**: `MediaSessionService` + ExoPlayer + `MediaController`; нотифікація, audio focus і кнопки гарнітури — з коробки |
| Picasso | Coil 3 |
| AppWidget + RemoteViews | Glance App Widget |
| Timber | Timber (лишаємо) |
| Stetho | прибираємо (App Inspection у Studio) |
| Firebase / Fabric | відкладено до етапу 8 |

---

## 3. Архітектура

Один модуль `:app` на старті (обсяг невеликий), розбиття по пакетах так, щоб потім легко винести в модулі:

```
com.propodcast.app
├── core
│   ├── network      // ListenNotesApi, DTO, ApiKeyInterceptor, mapping помилок → AppError
│   ├── database     // Room: entities, DAO, AppDatabase
│   ├── datastore    // FiltersPreferences, LastPlayedPreferences
│   ├── data         // PodcastRepository, FavoritesRepository, FiltersRepository, PagingSources
│   ├── model        // доменні моделі: Podcast, Episode, Genre, Region, Filters
│   ├── player       // PlaybackService (Media3), PlayerController (обгортка над MediaController)
│   ├── designsystem // тема, спільні компоненти (PodcastCard, EpisodeRow, ErrorState, LoadingState)
│   └── di           // Hilt-модулі: NetworkModule, DatabaseModule, DataStoreModule, DispatchersModule
├── feature
│   ├── home         // Best podcasts
│   ├── filters      // Жанр / Регіон (bottom sheet або окремий екран)
│   ├── search       // Пошук + мова
│   ├── details      // Деталі подкасту
│   ├── favorites    // Обране
│   └── player       // Mini player + повноекранний плеєр
├── widget           // Glance-віджет
└── navigation       // AppNavHost, routes
```

Шари: `Screen (Compose) ← ViewModel (@HiltViewModel, StateFlow<UiState>) ← Repository ← Api / Dao / DataStore`. Мапимо DTO → доменні моделі; UI не бачить DTO й entity.

Ключ API: у `local.properties` (`LISTEN_NOTES_API_KEY=...`) → `BuildConfig`, не в git. Для розробки можна брати тестовий мок-сервер Listen Notes `https://listen-api-test.listennotes.com/api/v2/` (без ключа, фейкові дані) — base URL через buildType/flavor.

---

## 4. Етапи

Кожен етап — окремий коміт/PR, застосунок збирається й запускається після кожного.

**Етап 0. Фундамент** ✅
- Тулчейн: AGP 9.2.1 (вбудований Kotlin, без плагіна `kotlin-android`), Gradle 9.4.1, Kotlin 2.2.21, KSP 2.3.14, Hilt 2.59.2 (вимагає AGP 9).
- Залежності в `libs.versions.toml`: Hilt, KSP, Navigation Compose, kotlinx.serialization, Retrofit/OkHttp, Room, DataStore, Paging, Coil, Media3, Timber.
- `@HiltAndroidApp` Application, `@AndroidEntryPoint` MainActivity.
- Тема (кольори/типографіка), `NavigationSuiteScaffold` з 3 вкладками-заглушками: Головна, Обране, Пошук.
- Base URL і ключ через `BuildConfig`: debug → мок-сервер без ключа, release → продакшн-API з `LISTEN_NOTES_API_KEY` з `local.properties`.

**Етап 1. Шар даних** ✅
- OkHttp зафіксовано на 5.4.0: 5.5.x вимагає compileSdk 37 (на машині лише SDK 36).
- Тестовий сервер Listen Notes повертає ті самі фейкові дані на будь-який запит (той самий подкаст для будь-якого id, епізоди замість подкастів у пошуку), тож фікстури тестів для пошуку зібрані вручну у форматі реального API.
- `ListenNotesApi` (6 ендпоінтів + `languages`), DTO, мапери, `AppError` (NoKey / NotFound / QuotaExceeded / Server / Network).
- `PodcastRepository`, `BestPodcastsPagingSource` (page), `SearchPagingSource` (offset).
- Room: `FavoritePodcastEntity`, `FavoriteEpisodeEntity` (+ `podcastTitle`, `addedAt` для сортування), DAO на Flow.
- DataStore: фільтри (жанр, регіон, мова), останній трек (url, назва, автор, обкладинка, позиція).
- Юніт-тести: мапери, PagingSource (MockWebServer), репозиторії.

**Етап 2. Головна + фільтри** ✅
- Coil зафіксовано на 3.4.0: 3.6.x тягне Compose 1.12, якому потрібен compileSdk 37.
- У Listen Notes немає регіону «усі»: без параметра `region` API повертає США, тому «без фільтра» показується як «United States (default)».
- Попередження про квоту API: `ApiQuotaRepository.quota` (з заголовків `X-ListenAPI-Usage` / `X-ListenAPI-FreeQuota`), банер при `isRunningLow()` / `isExhausted`.
- Список best podcasts з Paging 3, стани loading / error + retry / empty.
- Фільтри Жанр/Регіон (bottom sheet з вкладками або окремий екран), збереження в DataStore, перезавантаження списку при зміні.
- Дії на картці: в обране, поділитися (`Intent.ACTION_SEND` з `listennotes_url`).

**Етап 3. Деталі подкасту**
- Шапка, опис, список епізодів з довантаженням через `next_episode_pub_date`, рекомендації (LazyRow) з переходом на інші деталі.
- Обране / поділитися для подкасту й епізоду; тап по епізоду → плеєр (на цьому етапі можна заглушку).

**Етап 4. Пошук**
- Поле пошуку з debounce, фільтр мови (список з `GET languages`), результати через Paging 3.

**Етап 5. Обране**
- Вкладки Подкасти / Епізоди, Flow з Room, видалення (swipe або кнопка), перехід у деталі / відтворення.

**Етап 6. Плеєр (Media3)**
- `PlaybackService : MediaSessionService` з ExoPlayer — нотифікація, lock screen, audio focus, кнопки гарнітури автоматично.
- `PlayerController` (Hilt singleton) над `MediaController`: `StateFlow<PlayerState>` (трек, isPlaying, buffering, position, duration, error).
- Mini player над нижньою навігацією + повноекранний плеєр (seek, ±15/30 с, швидкість — опційно).
- Збереження останнього треку й позиції → «Продовжити» (замість FAB старого проекту).
- Дозвіл `POST_NOTIFICATIONS` (Android 13+), `FOREGROUND_SERVICE_MEDIA_PLAYBACK`.

**Етап 7. Віджет (Glance)**
- Останній трек, play/pause/відновити, відкрити застосунок; оновлення з `PlayerController`.

**Етап 8. Полірування**
- Адаптивні сітки для планшетів/landscape, edge-to-edge, темна тема, accessibility (contentDescription).
- Compose UI-тести ключових екранів, R8-правила, release-підпис (новий keystore поза git).
- Рішення щодо Firebase (Analytics / Crashlytics / FCM) — відкладено до цього етапу.

---

## 5. Свідомо не переносимо
Otto, RxJava, Stetho, Multidex, `AppExecutors`, `ServerMocksUtil` (мови беремо з API), Fabric Crashlytics, ручні `MediaSessionHelper` / `AudioFocusHelper` / `MediaSessionNotificationsManager` / `UpdateByTimerHandler` (замінює Media3), старий `keystore.jks` і `google-services.json`.

## 6. Рішення (2026-11-25)
1. **Firebase** — відкладено; повертаємось до питання на етапі 8.
2. **API** — починаємо з мок-сервера `https://listen-api-test.listennotes.com/api/v2/` (без ключа) для debug. Справжній ключ пізніше: `LISTEN_NOTES_API_KEY` у `local.properties`, release ходить на продакшн-API.
3. **Пошук** — лише подкасти, як у старому застосунку. Пошук епізодів — можлива окрема задача пізніше.
4. **Офлайн-кеш** — не робимо.
5. **minSdk 26** (Android 8.0) — лишаємо.
