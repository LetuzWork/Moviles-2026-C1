# 📱 MenuSemana — App de Planificación de Comidas

**IFTS 18 · Desarrollo de Aplicaciones para Dispositivos Móviles · 2° Año "B" · 2026**

### Equipo

| Integrante | Rol |
|---|---|
| Jeampierre Gonzalez | Tech Lead |
| Rosana Sosa | UX/UI |
| Facundo Palmaricciotti | QA / DevOps |
| Martin Laguna | PO / Tech Lead |

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Repository + Use Cases |
| Inyección de dependencias | Hilt |
| Base de datos local | Room (KSP) |
| Red | Retrofit 2 + OkHttp |
| Imágenes | Coil 3 |
| Preferencias | DataStore |
| Testing | JUnit 4, MockK, Turbine |
| Min SDK | 26 (Android 8.0) — Target SDK 35 |

---

## ⚙️ Setup y comandos

**Requisitos:** Android Studio Meerkat o superior, JDK 17.

```bash
# Clonar el repo
git clone https://github.com/LetuzWork/Moviles-2026-C1.git
cd Moviles-2026-C1

# Compilar y ejecutar tests unitarios
./gradlew test

# Generar APK debug
./gradlew assembleDebug

# Generar APK release (requiere keystore configurado)
./gradlew assembleRelease

# Instalar en dispositivo/emulador conectado
./gradlew installDebug
```

---

## 🔗 Links del proyecto

| | |
|---|---|
| Tablero | [GitHub Projects](https://github.com/users/LetuzWork/projects/2/views/1) |
| Presentación H2 | [Slides en Canva](https://www.canva.com/design/DAHMGQdBGDU/qL3_5P6IeviRpPbiyFMUQw/view?utm_content=DAHMGQdBGDU&utm_campaign=designshare&utm_medium=link&utm_source=viewer) |
| Prototipo UI | [Figma](https://www.figma.com/design/IaeaVzp4HpRV4P37Gr0s1N/) |

---

## 📄 Documentación del proyecto

En la carpeta [`docs/`](./docs) se encuentran los documentos base del proyecto:

| Archivo | Descripción |
|---|---|
| [`consigna.md`](./docs/consigna.md) | Requisitos funcionales, no funcionales y arquitectónicos planteados por la cátedra (IFTS 18 — Desarrollo de Aplicaciones Móviles, 2° Año "B") |
| [`menusemana_user_flow.svg`](./docs/menusemana_user_flow.svg) | Flujo de pantallas de la aplicación |
| [`diagrama_arquitectura.md`](./docs/diagrama_arquitectura.md) | Diagrama de alto nivel de la arquitectura de la solución |
| [`documentacion_tecnica.md`](./docs/documentacion_tecnica.md) | Documentación técnica del proyecto |
| [`documentacion_usuario.md`](./docs/documentacion_usuario.md) | Documentación orientada al usuario final |
| [`checklist_nielsen.md`](./docs/checklist_nielsen.md) | Evaluación de usabilidad contra las 10 heurísticas de Nielsen |
| [`metricas_performance.md`](./docs/metricas_performance.md) | Resultados de Macrobenchmark (cold start y scroll) |
| [`declaracion_ia.md`](./docs/declaracion_ia.md) | Declaración de uso responsable de IA (consigna 0.1) |

---

## 📦 Entregables — APK y AAB

Los artefactos de release se encuentran en la carpeta [`release/`](./release):

| Archivo | Versión | Tipo | Descripción |
|---|---|---|---|
| [`menusemana-v1.0-release.aab`](./release/menusemana-v1.0-release.aab) | 1.0 | Release (AAB) | **Android App Bundle firmado** — entregable RC de H2 (formato de publicación en Play Store) |
| [`menusemana-v1.0-release.apk`](./release/menusemana-v1.0-release.apk) | 1.0 | Release (firmado) | APK de release **firmado** y minificado (R8) — instalable directamente |

> **Firma:** el keystore y sus credenciales **no se versionan**. Ver [`keystore.properties.template`](./keystore.properties.template) para configurar la firma localmente o en CI (vía Secrets).

---

## 🌿 Flujo de trabajo Git

- Cómo contribuir (setup, ramas, PRs, checklist): [`CONTRIBUTING.md`](./CONTRIBUTING.md).
- Convenciones detalladas de ramas y commits: [`GITFLOW.md`](./GITFLOW.md).
