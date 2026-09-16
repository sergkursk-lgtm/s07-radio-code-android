#!/usr/bin/env bash
#
# Подготовка комплекта для публикации AAB в RuStore.
#
# RuStore для формата AAB требует два файла подписи и саму сборку:
#
#   1. pepk_out.zip          — ключ ПОДПИСИ приложения (release-key.jks),
#                              зашифрованный ключом RuStore. Именно им RuStore
#                              будет подписывать APK, которые отдаёт пользователям.
#   2. rustore-signature.pem — публичный сертификат ключа ЗАГРУЗКИ,
#                              которым подписан сам AAB.
#   3. AAB, подписанный ключом загрузки (upload-key.jks).
#
# Первый файл собирается утилитой pepk.jar, которую RuStore выдаёт в консоли
# вместе с уникальным encryptionkey. Пароли берутся из local.properties
# (файл в .gitignore), в скрипте они не хранятся.
#
# Использование:
#     ./rustore-upload.sh <encryptionkey из консоли RuStore>
#
set -euo pipefail

cd "$(dirname "$0")"

RELEASE_CERT_NAME="rustore-signature.pem"
PEPK="pepk.jar"
PEPK_OUT="pepk_out.zip"
READY_DIR="rustore-ready"

usage() {
    cat <<'USAGE'
Не хватает токена шифрования.

В консоли RuStore, на шаге «Загрузка подписи приложения»:
  1. нажмите «Скачать» и положите pepk.jar рядом с этим скриптом;
  2. оттуда же скопируйте значение encryptionkey;
  3. выполните:

     ./rustore-upload.sh ВАШ_ENCRYPTIONKEY
USAGE
}

[ $# -ge 1 ] || { usage; exit 1; }
ENCRYPTION_KEY="$1"

# --- реквизиты из local.properties ------------------------------------------
[ -f local.properties ] || { echo "Не найден local.properties"; exit 1; }
prop() { grep -E "^$1=" local.properties | head -1 | cut -d= -f2-; }

RELEASE_STORE="$(prop RELEASE_STORE_FILE)"; RELEASE_STORE="${RELEASE_STORE:-release-key.jks}"
RELEASE_PASS="$(prop RELEASE_STORE_PASSWORD)"
RELEASE_ALIAS="$(prop RELEASE_KEY_ALIAS)"; RELEASE_ALIAS="${RELEASE_ALIAS:-s07radio}"
UPLOAD_STORE="$(prop UPLOAD_STORE_FILE)"; UPLOAD_STORE="${UPLOAD_STORE:-upload-key.jks}"
UPLOAD_PASS="$(prop UPLOAD_STORE_PASSWORD)"
UPLOAD_ALIAS="$(prop UPLOAD_KEY_ALIAS)"; UPLOAD_ALIAS="${UPLOAD_ALIAS:-upload}"

[ -n "$RELEASE_PASS" ] || { echo "В local.properties нет RELEASE_STORE_PASSWORD"; exit 1; }
[ -n "$UPLOAD_PASS" ]  || { echo "В local.properties нет UPLOAD_STORE_PASSWORD"; exit 1; }
[ -f "$RELEASE_STORE" ] || { echo "Не найден ключ подписи $RELEASE_STORE"; exit 1; }
[ -f "$UPLOAD_STORE" ]  || { echo "Не найден ключ загрузки $UPLOAD_STORE"; exit 1; }
if [ ! -f "$PEPK" ]; then
    cat <<EOF
Не найден $PEPK

Его выдаёт консоль RuStore на шаге «Загрузка подписи» — нажмите там «Скачать»
и положите файл в эту папку: $(pwd)
EOF
    exit 1
fi

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
KEYTOOL_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}keytool"
JARSIGNER_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}jarsigner"
command -v "$JAVA_BIN" >/dev/null 2>&1 || { echo "Не найден java. Укажите JAVA_HOME."; exit 1; }

VERSION="$(grep -oE 'versionName = "[^"]+"' app/build.gradle.kts | head -1 | sed -E 's/.*"(.*)"/\1/')"
VERSION_CODE="$(grep -oE 'versionCode = [0-9]+' app/build.gradle.kts | head -1 | awk '{print $3}')"
[ -n "$VERSION" ] || { echo "Не смог прочитать versionName из app/build.gradle.kts"; exit 1; }
AAB_NAME="soueast_adb_code_v${VERSION}.aab"

# --- шаг 1: ключ подписи, зашифрованный ключом RuStore ----------------------
echo "==> 1/3 Собираю $PEPK_OUT (ключ подписи приложения)"
# pepk отказывается перезаписывать существующий файл — убираем старый
rm -f "$PEPK_OUT"
"$JAVA_BIN" -jar "$PEPK" \
    --keystore "$RELEASE_STORE" --alias "$RELEASE_ALIAS" \
    --keystore-pass "$RELEASE_PASS" --key-pass "$RELEASE_PASS" \
    --output "$PEPK_OUT" --include-cert \
    --encryptionkey="$ENCRYPTION_KEY" >/dev/null
[ -f "$PEPK_OUT" ] || { echo "pepk.jar не создал $PEPK_OUT"; exit 1; }
echo "    готово: $PEPK_OUT ($(wc -c < "$PEPK_OUT" | tr -d ' ') байт)"

# --- шаг 2: сертификат ключа загрузки ---------------------------------------
echo "==> 2/3 Готовлю $RELEASE_CERT_NAME (сертификат ключа загрузки)"
"$KEYTOOL_BIN" -exportcert -alias "$UPLOAD_ALIAS" -keystore "$UPLOAD_STORE" \
    -storepass "$UPLOAD_PASS" -rfc -file "$RELEASE_CERT_NAME" >/dev/null
echo "    готово: $RELEASE_CERT_NAME"

# --- шаг 3: AAB, подписанный ключом загрузки --------------------------------
echo "==> 3/3 Собираю AAB и подписываю его ключом загрузки"
./gradlew :app:bundleRelease --console=plain -q
mkdir -p "$READY_DIR"
cp "app/build/outputs/bundle/release/app-release.aab" "$READY_DIR/$AAB_NAME"
# снимаем подпись ключа подписи приложения: в AAB должна остаться только подпись ключом загрузки
zip -q -d "$READY_DIR/$AAB_NAME" "META-INF/*.SF" "META-INF/*.RSA" "META-INF/*.DSA" "META-INF/*.EC" 2>/dev/null || true
"$JARSIGNER_BIN" -keystore "$UPLOAD_STORE" -storepass "$UPLOAD_PASS" \
    -sigalg SHA256withRSA -digestalg SHA-256 \
    "$READY_DIR/$AAB_NAME" "$UPLOAD_ALIAS" >/dev/null
"$JARSIGNER_BIN" -verify "$READY_DIR/$AAB_NAME" | head -1
echo "    готово: $READY_DIR/$AAB_NAME"

# --- итог -------------------------------------------------------------------
TMPCERT="$(mktemp)"
unzip -p "$PEPK_OUT" certificate.pem > "$TMPCERT"
APP_FP="$("$KEYTOOL_BIN" -printcert -file "$TMPCERT" 2>/dev/null | awk '/SHA256:/{print $2}')"
rm -f "$TMPCERT"
UPLOAD_FP="$("$KEYTOOL_BIN" -printcert -jarfile "$READY_DIR/$AAB_NAME" 2>/dev/null | awk '/SHA256:/{print $2}')"
cat <<EOF

Готово. Что загружать в RuStore Консоль:

  Окно «Загрузка подписи приложения»:
    подпись приложения .......... $PEPK_OUT
    сертификат ключа загрузки ... $RELEASE_CERT_NAME
  Файл сборки:
    $READY_DIR/$AAB_NAME   (versionName $VERSION, versionCode ${VERSION_CODE:-?})

Отпечатки SHA-256 для сверки:
  ключ подписи приложения (внутри $PEPK_OUT): $APP_FP
  ключ загрузки (он же в подписи AAB):        $UPLOAD_FP
EOF
