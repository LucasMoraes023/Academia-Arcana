# Academia Arcana — jogo de cartas e aventura mágica (Android nativo, Java)

Jogo 100% offline, sem bibliotecas externas, sem internet, sem servidor.
Toda a arte, os sons e a música são gerados em código (Canvas e áudio procedural).

## Como gerar o APK (escolha UMA opção)

**A) Script, sem Gradle** (Linux, macOS, WSL ou Termux com Android SDK):

    export ANDROID_HOME=/caminho/do/Android/Sdk      # precisa de platforms;android-34 e build-tools;34.0.0
    ./build_apk.sh                                   # gera AcademiaArcana-debug.apk

**B) GitHub Actions (sem instalar nada):** envie esta pasta para um repositório no GitHub →
aba **Actions** → **Gerar APK** → **Run workflow** → baixe o artefato `AcademiaArcana-debug-apk`.

**C) Android Studio:** *Open* nesta pasta → *Build > Build APK(s)* →
`app/build/outputs/apk/debug/app-debug.apk`.

## Instalar no celular
1. Copie o APK para o aparelho (cabo, Drive, WhatsApp, etc.) e toque nele.
2. Permita "instalar apps de fontes desconhecidas" quando o Android pedir.
3. Abra **Academia Arcana**. (Android 5.0 ou superior.)

## Conta ADMIN
- Usuário: `ADMIN` — senha inicial: `ADMIN123` (altere em *Painel Admin > ALTERAR SENHA ADMIN*).
- Somente o ADMIN vê o **PAINEL ADMIN** (tela inicial > botão vermelho) e executa comandos.
- Comandos do console (*CONSOLE DE COMANDOS*):

      /givecoins 999999    /givexp 999999    /setlevel 50     /unlockall
      /givecard 001        /maxlevel         /godmode         /completequest 001
      /resetprogress       /help

## O que tem no jogo
- Tela inicial, cadastro/login local (senha com hash SHA-256 + sal), sessão e sair da conta.
- Quiz da casa (10 perguntas com pesos) → "SEU DESTINO FOI REVELADO" → 4 casas originais
  (Ignis, Maris, Sylva, Lunaris) com bônus e cartas exclusivas.
- Nível 0 — Novo Aprendiz (tutorial completo) → 50 níveis com XP e recompensas.
- Capítulo 1 com 10 fases (+ capítulos 2 e 3), fases bloqueadas até concluir a anterior (ADMIN acessa tudo).
- 60 cartas, 8 categorias, 6 raridades (lendárias e míticas com brilho animado).
- Coleção, deck (10 cartas), batalhas por turnos (atacar, defender, habilidade, poção, passar turno), chefe do capítulo 1.
- 38 perguntas de quiz, 18 enigmas (sequência, símbolos, código, lógica, combinação, memória).
- Missões principais/secundárias/diárias, inventário, 5 tipos de baú com animação, 14 conquistas.
- Salvamento automático: o jogo continua exatamente de onde você parou (inclusive no meio de uma fase).

## Estrutura
    app/src/main/java/br/com/lucasmoraes/arcana/   código Java (Data, Player, Battle, Admin, telas, Gfx, Audio...)
    app/src/main/res/                              manifesto, estilos e ícone
    build_apk.sh · build.gradle · .github/workflows/build-apk.yml

Criado por Lucas Moraes
