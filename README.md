# Код Soueast S07

Android-приложение для генерации кодов магнитолы Soueast S07.

## Скачать

**[Скачать soueast_adb_code.apk](https://github.com/sergkursk-lgtm/s07-radio-code-android/releases/download/v1.0/soueast_adb_code.apk)** (v1.0)

## Возможности

- Автоматическая генерация кода по дате и времени
- Таймер обратного отсчёта до следующего часа
- Ручной ввод (месяц, день, час)
- Инструкция по входу в меню ADB

## Как попасть в меню ADB

1. Откройте звонилку
2. Наберите `*#20230730#*`
3. Выберите предпоследний пункт
4. Введите код

## Установка

1. Скачайте APK-файл
2. На устройстве разрешите установку из неизвестных источников
3. Установите приложение

## Сборка из исходников

```bash
# Установите JDK 17 и Android SDK
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools

# Соберите APK
./gradlew assembleRelease

# APK будет в app/build/outputs/apk/release/
```

## Технологии

- Kotlin
- Jetpack Compose
- Material 3
- Min SDK 26 / Target SDK 34
