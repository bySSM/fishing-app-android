# Fishing App Android

Android-приложение для рыболовов. Java + Retrofit + Picasso + osmdroid.

## 📱 Функционал

### Реализовано:
- [x] Регистрация и логин (JWT)
- [x] Выход из аккаунта
- [x] Автологин
- [x] Аквариум с анимированными рыбами (топ-5)
- [x] Просмотр аквариумов других пользователей
- [x] Список уловов с карточками
- [x] Добавление улова (с фото из галереи или камеры)
- [x] Редактирование улова
- [x] Удаление улова (с подтверждением)
- [x] Камера (CameraX)
- [x] Геолокация (GPS) — обязательна для создания улова
- [x] Скрытие геолокации
- [x] Рейтинг топ-100 (по сумме 15 самых тяжёлых рыб)
- [x] Поиск пользователей (с позицией в рейтинге)
- [x] Настройки (профиль, о программе, связь)
- [x] Карта (OpenStreetMap/osmdroid)
- [x] Метки уловов на карте 🐟
- [x] Метка местоположения 📍

## 🗺 Карта
- Использует OpenStreetMap (osmdroid) — бесплатно, без API ключей
- Источник тайлов: OpenTopo (детальная для природы)
- Уловы отображаются эмодзи 🐟
- Местоположение — эмодзи 📍
- Кнопка "Уловы рядом" — загружает уловы в радиусе 100 км

## 🛠 Стек
- Java 17
- Android SDK (minSdk 24, targetSdk 34)
- Retrofit 2.9.0
- Gson
- Picasso (загрузка изображений)
- CameraX (камера)
- Google Play Services Location (GPS)
- osmdroid (OpenStreetMap)

## 📁 Структура

app/src/main/java/com/example/fishingapp/
├── MainActivity.java — логин
├── RegisterActivity.java — регистрация
├── AquariumActivity.java — мой аквариум
├── UserAquariumActivity.java — чужой аквариум
├── CatchListActivity.java — мои уловы
├── AddCatchActivity.java — добавление
├── EditCatchActivity.java — редактирование
├── CameraActivity.java — камера
├── RatingActivity.java — рейтинг
├── SearchActivity.java — поиск
├── SettingsActivity.java — настройки
├── MapActivity.java — карта (osmdroid)
├── CatchAdapter.java — адаптер списка
├── api/
│ ├── ApiClient.java
│ └── FishingApi.java
├── model/
│ ├── Catch.java
│ └── User.java
└── utils/
├── Config.java — BASE_URL
├── TokenManager.java
└── LocationHelper.java


## 📋 TODO
- [ ] Увеличить размер маркеров на карте
- [ ] Клик по метке улова — показать фото
- [ ] Выбор точки на карте при создании улова
- [ ] Лайки в приложении
- [ ] Комментарии в приложении
- [ ] Детальная страница улова
- [ ] Улучшение дизайна аквариума
- [ ] Публикация в Google Play