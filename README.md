# Код Soueast S07 awd

Android-приложение для генерации кодов ГУ Soueast S07 awd.

## Скачать

**Обновления приходят через RuStore** — ссылка на карточку приложения появится здесь после публикации.
Сами по себе обновления приложение больше не проверяет: сетевых запросов и разрешения `INTERNET` в нём нет.

Ручная установка (например, на головное устройство): [soueast_adb_code_v1.9.apk](https://github.com/sergkursk-lgtm/s07-radio-code-android/releases/download/v1.9/soueast_adb_code_v1.9.apk) (v1.9)

> **Важно:** начиная с v1.9 приложение подписано новым ключом. Если установлена версия 1.8 или старше,
> её нужно **удалить** и установить v1.9 заново — Android не устанавливает обновление с другим сертификатом.

## Возможности

- Автоматическая генерация кода по текущему времени
- Таймер обратного отсчёта до следующего часа
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

## Что изменилось в v1.9

- Убрана встроенная проверка обновлений через GitHub Releases и весь связанный код
  (`UpdateChecker`, диалог обновления, `FileProvider`, `file_paths.xml`, разрешение `INTERNET`).
  Причиной стало ложное окно «Доступно обновление»: релизы v1.6–v1.8 содержали один и тот же APK,
  который внутри сообщал версию 1.6, а последним релизом числился v1.8.
- Генератор кода вынесен в `CodeGenerator.kt` и больше не зависит от локали устройства
  (на локалях с не-ASCII цифрами приложение падало при запуске).
- Пароли подписи больше не хранятся в репозитории — только в `local.properties` (см. ниже).

## Разработка

Сборка:

```sh
./gradlew :app:testDebugUnitTest      # юнит-тесты
./gradlew :app:assembleRelease        # подписанный APK
./gradlew :app:bundleRelease          # AAB для RuStore
```

Реквизиты подписи берутся из `local.properties` (файл в `.gitignore`), шаблон — `local.properties.example`:

```properties
sdk.dir=/Users/<user>/Library/Android/sdk
RELEASE_STORE_FILE=release-key.jks
RELEASE_STORE_PASSWORD=<пароль>
RELEASE_KEY_ALIAS=s07radio
RELEASE_KEY_PASSWORD=<пароль>
```

Если ключа нет, release-сборка получается неподписанной — публиковать её нельзя.
Ключ `release-key.jks` хранится локально и в резервной копии; терять его нельзя:
Android и RuStore требуют один сертификат для всех версий приложения.

### Чек-лист релиза

```sh
# 1. Поднять versionName/versionCode в app/build.gradle.kts (versionCode только растёт)
# 2. Собрать
./gradlew :app:assembleRelease :app:bundleRelease
# 3. Проверить, что версия в APK совпадает с тегом релиза — именно рассинхрон версии и тега
#    был причиной ложного окна обновления
~/Library/Android/sdk/build-tools/34.0.0/aapt2 dump badging app/build/outputs/apk/release/app-release.apk | head -1
# 4. Проверить подпись
~/Library/Android/sdk/build-tools/34.0.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
# 5. Выложить APK в GitHub-релиз с тегом, равным versionName
```

## Публикация в RuStore

Приложение распространяется только через RuStore, поэтому в нём нет встроенной проверки обновлений.

**Готовый комплект для карточки — в [`rustore-ready/`](rustore-ready):** тексты описания, иконка 512×512,
скриншоты 9:16 и 16:9, комментарий модератору, обоснование разрешений и разбор соответствия
требованиям RuStore. Что куда загружать — в `rustore-ready/ЧТО-ЗАГРУЖАТЬ.txt`.

Подпись и сборку для RuStore готовит один скрипт:

```sh
./rustore-upload.sh <encryptionkey из окна «Загрузка подписи приложения»>
```

Он делает `pepk_out.zip`, сертификат ключа загрузки `rustore-signature.pem` и AAB, подписанный
ключом загрузки, — то есть все три файла, которые просит консоль.

Есть два пути, оба дают пользователям одинаковую подпись приложения (`release-key.jks`, `CN=Soueast`):

**1. Загрузить APK (проще).** Берётся `app/build/outputs/apk/release/app-release.apk`, он уже подписан
ключом подписи приложения. Никаких дополнительных файлов не нужно.

**2. Загрузить AAB (RuStore сам собирает APK под устройства).** В формате AAB магазин требует
дополнительно загрузить подпись, а сам AAB должен быть подписан отдельным ключом загрузки
(подробности в [документации RuStore](https://www.rustore.ru/help/developers/publishing-and-verifying-apps/app-publication/new-version-app/upload-aab)):

```sh
# 1. ZIP с ключом подписи приложения, зашифрованный на ключ из RuStore Консоль
#    (pepk.jar и строку --encryptionkey выдаёт окно «Загрузка подписи приложения»)
java -jar pepk.jar --keystore release-key.jks --alias s07radio \
  --output pepk_out.zip --include-cert --encryptionkey=<из консоли RuStore>

# 2. Сертификат ключа загрузки (upload-key.jks создаётся один раз и хранится рядом с release-key.jks)
keytool -exportcert -alias upload -keystore upload-key.jks -rfc -file uploadcert.pem

# 3. AAB, подписанный ключом загрузки: снимаем подпись ключа приложения и подписываем upload-ключом
cp app/build/outputs/bundle/release/app-release.aab app-release-rustore.aab
zip -d app-release-rustore.aab "META-INF/*.SF" "META-INF/*.RSA"
jarsigner -keystore upload-key.jks -sigalg SHA256withRSA -digestalg SHA-256 \
  app-release-rustore.aab upload
jarsigner -verify app-release-rustore.aab        # должно быть «jar verified.»
```

Затем в RuStore Консоль: `pepk_out.zip` и `uploadcert.pem` — в окне «Загрузка подписи приложения»,
`app-release-rustore.aab` — как файл сборки. Требование RuStore к ключу подписи — RSA не меньше 2048 бит;
оба ключа этому соответствуют.

**Ключи:** `release-key.jks` (подпись приложения — менять нельзя никогда) и `upload-key.jks`
(подпись AAB — перевыпускается через поддержку RuStore, если потеряется). Оба лежат локально,
в git не попадают, резервная копия — в iCloud.

## Технологии

- Kotlin
- Jetpack Compose
- Material 3
- Min SDK 26 / Target SDK 34
