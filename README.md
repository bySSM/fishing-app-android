# Fishing App Android

Android-клиент приложения для рыболовов. Java + Retrofit + JWT.

## 📱 Идея приложения

Рыбаки фотографируют улов, коллекционируют как покемонов, смотрят уловы других, лайкают, соревнуются в рейтинге.

## 🛠 Стек технологий

- Java
- Android Gradle Plugin
- Retrofit 2 + Gson
- Picasso (загрузка изображений)
- CameraX
- osmdroid (карты)
- Google Play Services Location
- EncryptedSharedPreferences (androidx.security.crypto)

## ✅ Уже реализовано

- [x] Регистрация и логин (JWT)
- [x] Создание улова (с фото через камеру, геолокацией)
- [x] Просмотр своих и чужих уловов
- [x] Уловы рядом (карта)
- [x] Лайки и комментарии
- [x] Рейтинг (топ-100)
- [x] Аквариум (топ-5 рыб пользователя)
- [x] Поиск пользователей

## 🔒 Безопасность

Проект прошёл security-ревью, устранены следующие проблемы:

- **Разделение dev/prod конфигурации сети**: адрес бэкенда (`BASE_URL`) задаётся через `buildConfigField` отдельно для `debug` и `release` сборок (см. `app/build.gradle`).
- **Cleartext HTTP разрешён только в debug**: `network_security_config.xml` в `src/debug/res/xml` разрешает HTTP только на конкретные адреса локальной разработки (эмулятор, localhost, локальный IP). В `src/release/res/xml` cleartext-трафик запрещён полностью — релизная сборка не сможет случайно уйти в прод с открытым HTTP.
- **Шифрованное хранилище токена**: JWT-токен и логин хранятся в `EncryptedSharedPreferences` (AES-256), а не в обычных `SharedPreferences` открытым текстом.
- **`android:allowBackup="false"`**: исключает восстановление данных приложения (включая токен) через `adb backup` на устройствах без root.
- Убрана неиспользуемая библиотека `org.apache.http.legacy` (лишняя поверхность атаки).
- Обработка HTTP `429` (rate limit) от бэкенда — отдельное сообщение пользователю вместо общего "неверный логин или пароль".

## ⚙️ Конфигурация окружения

### Debug (локальная разработка)
Адрес бэкенда задаётся в `app/build.gradle`, блок `buildTypes.debug`:
```groovy
buildConfigField "String", "BASE_URL", "\"http://ВАШ_ЛОКАЛЬНЫЙ_IP:8080/\""
```
Разрешённые для HTTP адреса дополнительно перечислены в `app/src/debug/res/xml/network_security_config.xml` — при смене IP нужно обновить оба места.

### Release (продакшен)
```groovy
buildConfigField "String", "BASE_URL", "\"https://ВАШ_ДОМЕН/\""
```
Backend должен быть доступен по HTTPS — cleartext HTTP в release-сборке запрещён на уровне `network_security_config.xml`.

## 🗂 Структура пакетов

```
com.example.fishingapp
├── *Activity.java        — экраны (Main, Register, CatchList, AddCatch, EditCatch,
│                           Aquarium, Settings, Camera, Rating, UserAquarium, Search,
│                           Map, Comments, CatchDetail)
├── api/                  — ApiClient, FishingApi (Retrofit)
├── model/                — модели данных (User, Catch, ...)
└── utils/                — Config (BuildConfig-based BASE_URL), TokenManager (EncryptedSharedPreferences)
```

## 📋 TODO

- [ ] Certificate pinning (после появления HTTPS-домена)
- [ ] Обработка специфичных HTTP-кодов (404/403) в UI с понятными сообщениями
- [ ] ИИ-распознавание рыбы (клиентская часть)
- [ ] Push-уведомления