#!/usr/bin/env bash
# Gera o APK debug SEM Gradle, usando as ferramentas do Android SDK.
# Requisitos: JDK 17+, zip, e o Android SDK com
#   "platforms;android-34" e "build-tools;36.0.0" (ou mais recente).
# Uso: ANDROID_HOME=/caminho/do/sdk ./build_apk.sh
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ]; then
  for d in "$HOME/Android/Sdk" "$HOME/Library/Android/sdk" /usr/lib/android-sdk /opt/android-sdk; do
    if [ -d "$d" ]; then SDK="$d"; break; fi
  done
fi
[ -n "$SDK" ] && [ -d "$SDK" ] || { echo "ERRO: defina ANDROID_HOME apontando para o Android SDK."; exit 1; }

BT="$(ls -d "$SDK"/build-tools/* 2>/dev/null | sort -V | tail -1)"
PLAT="$(ls -d "$SDK"/platforms/android-* 2>/dev/null | sort -V | tail -1)"
[ -n "$BT" ] && [ -n "$PLAT" ] || { echo "ERRO: instale build-tools e uma platform (sdkmanager \"platforms;android-34\" \"build-tools;36.0.0\")."; exit 1; }
BT_VERSION="$(basename "$BT")"
MIN_BT_VERSION=36.0.0
if [ "$(printf '%s\n' "$MIN_BT_VERSION" "$BT_VERSION" | sort -V | head -1)" != "$MIN_BT_VERSION" ]; then
  echo "ERRO: build-tools $BT_VERSION não é compatível com o D8 desta compilação."
  echo "Instale build-tools $MIN_BT_VERSION ou mais recente."
  exit 1
fi
AJAR="$PLAT/android.jar"
echo "SDK: $SDK | build-tools: $(basename "$BT") | $(basename "$PLAT")"

OUT=build
rm -rf "$OUT"
mkdir -p "$OUT/gen" "$OUT/classes" "$OUT/dex"

# AGP usa 'namespace'; para o aapt2 puro precisamos do atributo package no manifesto.
sed 's#<manifest #<manifest package="br.com.lucasmoraes.arcana" #' app/src/main/AndroidManifest.xml > "$OUT/AndroidManifest.xml"

echo "1/6 Recursos (aapt2)"
"$BT/aapt2" compile --dir app/src/main/res -o "$OUT/res.zip"
"$BT/aapt2" link -o "$OUT/base.apk" -I "$AJAR" --manifest "$OUT/AndroidManifest.xml" \
  --min-sdk-version 21 --target-sdk-version 34 --version-code 1 --version-name 1.0 \
  --java "$OUT/gen" "$OUT/res.zip"

echo "2/6 Compilando Java"
javac -encoding UTF-8 -source 8 -target 8 -Xlint:-options -cp "$AJAR" -d "$OUT/classes" \
  $(find app/src/main/java "$OUT/gen" -name '*.java')

echo "3/6 Dex (d8)"
D8="$BT/d8"
[ -x "$D8" ] || D8="$(command -v d8)"
"$D8" --min-api 21 --lib "$AJAR" --output "$OUT/dex" $(find "$OUT/classes" -name '*.class')

echo "4/6 Montando APK"
cp "$OUT/base.apk" "$OUT/unsigned.apk"
(cd "$OUT/dex" && zip -q -j ../unsigned.apk classes.dex)
"$BT/zipalign" -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"

echo "5/6 Assinando (chave debug)"
KS="$HOME/.android/debug.keystore"
if [ ! -f "$KS" ]; then
  mkdir -p "$(dirname "$KS")"
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android -alias androiddebugkey \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" >/dev/null
fi
APK=AcademiaArcana-debug.apk
"$BT/apksigner" sign --ks "$KS" --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias androiddebugkey --out "$APK" "$OUT/aligned.apk"

echo "6/6 Verificando"
"$BT/apksigner" verify --verbose "$APK" | head -5
ls -l "$APK"
echo "PRONTO: $(pwd)/$APK"
