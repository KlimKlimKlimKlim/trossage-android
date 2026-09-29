# Trossage Android

Android-клиент мессенджера Trossage, созданный в команде из двух разработчиков. Этот репозиторий содержит клиентскую часть; [backend](https://github.com/GlaciemArgentum/trossage-backend) разрабатывался отдельно другим участником команды.

## Личный вклад

- Полная разработка Android-клиента.
- Совместное согласование с backend-разработчиком API-контрактов, форматов запросов и сценариев авторизации.
- Реализация экранов, сетевого слоя, локального кэша, WebSocket-взаимодействия и unit-тестов.

## Возможности

- Регистрация и авторизация.
- Список чатов и экран переписки.
- Поиск пользователей.
- Обмен сообщениями и отображение набираемого собеседником текста в реальном времени.
- JWT-аутентификация с access/refresh token.
- Локальное кэширование чатов и сообщений в Room.
- Постраничная загрузка истории сообщений.

## Технологии

- Kotlin
- Jetpack Compose и Material 3
- MVVM и разделение на `data`, `domain`, `ui`
- Retrofit, OkHttp и REST API
- OkHttp WebSocket
- Coroutines и Flow
- Room
- AndroidX Security Crypto
- JUnit, MockK, Coroutines Test и Turbine

## Архитектура

```text
app/src/main/java/com/klim/trossage_android/
├── data/       # API, WebSocket, Room, DTO и реализации репозиториев
├── domain/     # модели и контракты репозиториев
└── ui/         # Compose-экраны и ViewModel
```

Сетевой слой добавляет access token к запросам. При ответе 401 authenticator обновляет токен, синхронизирует параллельные refresh-запросы, повторяет исходный запрос и завершает сессию при невозможности обновления.

WebSocket-соединение реализовано через OkHttp и адаптировано к Flow с помощью `callbackFlow`.

## Требования

- Android Studio с поддержкой Kotlin 2.x
- JDK 11
- Android SDK 35
- Android 8.0 (API 26) и выше
- Запущенный Trossage backend

## Настройка и запуск

1. Клонируйте репозиторий:

   ```bash
   git clone https://github.com/KlimKlimKlimKlim/trossage-android.git
   cd trossage-android
   ```

2. Откройте проект в Android Studio и дождитесь Gradle Sync.
3. Проверьте адреса REST API и WebSocket в сетевой конфигурации приложения.
4. Запустите debug-сборку на устройстве или эмуляторе.

Текущая версия использует развёрнутый backend проекта. Для локального запуска backend потребуется заменить адреса сервера в Android-клиенте.

## Тесты и проверки

В проекте есть unit-тесты JWT-парсера, auth-interceptor и ViewModel авторизации.

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Эти команды запускаются GitHub Actions для pull request и изменений основной ветки.

## Ограничения текущей версии

- Адреса сервера пока задаются в коде.
- Автоматическое переподключение WebSocket с backoff ещё не реализовано.
- Тестами покрыты ключевые части авторизации, но не все пользовательские сценарии.

## Связанные репозитории

- [Trossage backend](https://github.com/GlaciemArgentum/trossage-backend)

## Автор Android-клиента

Клим Трофимов — [GitHub](https://github.com/KlimKlimKlimKlim)
