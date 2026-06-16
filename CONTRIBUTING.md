# Guía de Contribución — MenúSemana

Gracias por contribuir. Esta guía resume **cómo trabajamos**. Las convenciones detalladas de ramas y commits están en [`GITFLOW.md`](./GITFLOW.md).

---

## 🔧 Requisitos de entorno

- **Android Studio** Meerkat o superior.
- **JDK 21** (el daemon de Gradle está fijado a Java 21 en `gradle/gradle-daemon-jvm.properties`). El JBR que trae Android Studio sirve.
- **Android SDK** con la plataforma de `compileSdk` instalada.

```bash
# Tests unitarios + lint
./gradlew :app:testDebugUnitTest :app:lintDebug

# APK debug
./gradlew :app:assembleDebug
```

> Si Gradle no encuentra el SDK, creá `local.properties` con `sdk.dir=<ruta-al-SDK>` (ese archivo está gitignored).

---

## 🌿 Ramas

Usamos **GitFlow** (ver [`GITFLOW.md`](./GITFLOW.md)):

```
main        → producción (protegida, no se commitea directo)
develop     → integración (base de las features)
feature/*   → nuevas funcionalidades (parten de develop)
bugfix/*    → corrección de bugs
release/*   → preparación de versión
hotfix/*    → fixes urgentes desde main
```

Nombre: `<tipo>/<descripcion-en-kebab-case>` (ej. `feature/HU-06.4-seed-datos`).

---

## ✍️ Commits

**Conventional Commits**: `<tipo>(<scope>): <descripción en imperativo>`

Tipos: `feat`, `fix`, `refactor`, `style`, `test`, `docs`, `chore`, `perf`, `revert`.

---

## 🔀 Pull Requests

1. Partí de `develop` (o `main` para hotfix) actualizado.
2. Abrí el PR usando la [plantilla](./.github/PULL_REQUEST_TEMPLATE.md).
3. Requisitos para mergear:
   - ✅ **El CI pasa** (lint + tests).
   - ✅ **Al menos 1 reviewer** aprobó.
   - ✅ La rama está al día con la base.
4. No hacer `push --force` a ramas compartidas ni mergear sin review.

---

## ✅ Checklist antes del PR

- [ ] Compila y pasan los tests (`./gradlew :app:testDebugUnitTest`).
- [ ] Se agregaron tests si corresponde.
- [ ] Se actualizó la documentación si cambió el comportamiento.
- [ ] El PR describe **qué** y **por qué**.
- [ ] No se subieron secretos ni keystores (ver [`keystore.properties.template`](./keystore.properties.template)).
