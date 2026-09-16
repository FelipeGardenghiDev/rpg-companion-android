# Grimório RPG — Tabletop RPG Companion for Android

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=android)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-success.svg)](#arquitetura)
[![Offline-First](https://img.shields.io/badge/Design-100%25%20Offline--First-orange.svg)](#premissas)
[![Zero Cloud Cost](https://img.shields.io/badge/Cost-Zero%20Server%20Cost-brightgreen.svg)](#premissas)

Assistente nativo para jogadores e mestres de RPG de mesa (D&D 5e, Tormenta20, Pathfinder, Call of Cthulhu e outros). Construído com foco em **privacidade total**, **resiliência sem conexão à internet** e **comunicação peer-to-peer (P2P)** na mesa de jogo.

---

## 🌟 Destaques e Funcionalidades

- 🎲 **Motor de Rolagem Avançado:**
  - Suporte a dados poliédricos padrão (d4, d6, d8, d10, d12, d20, d100) e arbitrários.
  - Parser léxico em Kotlin puro para fórmulas complexas (`1d20 + 5`, `2d6 + 1d4 + 3`).
  - Mecânica de **Vantagem e Desvantagem** (`2d20kh1`, `2d20kl1`).
  - Criação de atributos com descarte do menor dado (`4d6kh3`).
  - **Dados Explosivos** (`3d6!`).
  - Detecção automática de Acertos e Falhas Críticas com feedback tátil e sonoro de baixa latência (`SoundPool` e `VibrationEffect`).
- ⚔️ **Rastreador de Iniciativa e Combate (Combat Tracker):**
  - Ordenação automática de turnos, contador de rodadas e controle ágil de HP.
- 🧪 **Marcador de Recursos & Condições:**
  - Gestão de Pontos de Vida (HP), Mana/Spell Points, slots de magia e catálogo de condições com contadores de turnos.
- 📱 **Compartilhamento 100% Offline (QR Code & JSON):**
  - Exportação e importação instantânea de fichas, encontros e macros via QR Code gerado e lido pela câmera do celular sem necessidade de rede.
- 📡 **Mesa Local P2P sem Servidor (Google Nearby Connections):**
  - Conexão direta entre os aparelhos da mesa via Bluetooth / Wi-Fi Direct.
  - O Mestre recebe em tempo real os resultados de rolagens dos jogadores sem tráfego de dados na nuvem.
- 🎨 **Temas Dinâmicos (Material 3):**
  - Paletas personalizadas (Pergaminho Medieval, Cyberpunk Neon e AMOLED Dark).

---

## 📐 Arquitetura

O projeto adota as recomendações oficiais de arquitetura moderna para Android:
- **Clean Architecture** com separação clara de responsabilidades:
  - `presentation`: Telas declarativas em Jetpack Compose, Material 3 e ViewModels com `StateFlow`.
  - `domain`: Regras de negócio puras em Kotlin (UseCases, Parser matemático e modelos desacoplados do Android Framework).
  - `data`: Repositórios, persistência local com **Room (SQLite)** e camada de transporte com **Google Nearby Connections API**.
- **Unidirectional Data Flow (UDF)**: Estado da interface imutável emitido via `StateFlow` e eventos tratados via intents declarativas.

Consulte a especificação técnica completa em [ARCHITECTURE.md](ARCHITECTURE.md).

---

## 🛠️ Stack Tecnológica

| Componente | Tecnologia |
| :--- | :--- |
| Linguagem | Kotlin 2.x |
| Interface | Jetpack Compose + Material 3 |
| Banco de Dados | Room (SQLite com Kotlin Flow) |
| Comunicação Local | Google Play Services Nearby Connections |
| QR Code & Câmera | CameraX + ZXing |
| Serialização | kotlinx.serialization |
| Testes Unitários | JUnit 4 + Google Truth + Kotlin Coroutines Test |

---

## 🚀 Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/FelipeGardenghiDev/rpg-companion-android.git
   ```
2. Abra a pasta do projeto no **Android Studio** (Koala ou mais recente).
3. Aguarde o Gradle sincronizar as dependências.
4. Execute os testes unitários do motor de dados:
   ```bash
   ./gradlew test
   ```
5. Conecte um dispositivo Android ou inicie um emulador com Android 8.0+ (API 26+) e clique em **Run**.

---

## 📄 Licença

Este projeto está licenciado sob a licença [MIT](LICENSE).
