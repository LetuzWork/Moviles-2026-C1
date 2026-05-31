# 📱 App Mobile — Guía de Contribución con GitFlow

## 📄 Documentación del proyecto

En la carpeta [`docs/`](./docs) se encuentran los documentos base del proyecto:

| Archivo | Descripción |
|---|---|
| [`consigna.md`](./docs/consigna.md) | Requisitos funcionales, no funcionales y arquitectónicos planteados por la cátedra (IFTS 18 — Desarrollo de Aplicaciones Móviles, 2° Año "B") |
| [`diagrama_arquitectura.md`](./docs/diagrama_arquitectura.md) | Diagrama de alto nivel de la arquitectura de la solución |
| [`documentacion_tecnica.md`](./docs/documentacion_tecnica.md) | Documentación técnica del proyecto |
| [`documentacion_usuario.md`](./docs/documentacion_usuario.md) | Documentación orientada al usuario final |

---

Este documento define las convenciones de **ramas** y **commits** que seguimos en el proyecto usando el flujo de trabajo **GitFlow**.

---

## 🌿 Estructura de Ramas

```
main
└── develop
    ├── feature/nombre-de-la-feature
    ├── bugfix/descripcion-del-bug
    ├── release/x.y.z
    └── hotfix/descripcion-del-fix
```

### Ramas principales

| Rama | Descripción |
|------|-------------|
| `main` | Código en producción. **Nunca se commitea directo aquí.** |
| `develop` | Rama de integración. Base para todas las features. |

### Ramas de soporte

| Rama | Origen | Destino | Descripción |
|------|--------|---------|-------------|
| `feature/*` | `develop` | `develop` | Nuevas funcionalidades |
| `bugfix/*` | `develop` | `develop` | Corrección de bugs en desarrollo |
| `release/*` | `develop` | `main` + `develop` | Preparación de una nueva versión |
| `hotfix/*` | `main` | `main` + `develop` | Correcciones urgentes en producción |

---

## 📐 Convención de Nombres de Ramas

```
<tipo>/<descripcion-en-kebab-case>
```

### Ejemplos

```bash
feature/login-con-google
feature/pantalla-perfil-usuario
bugfix/crash-al-abrir-notificaciones
bugfix/error-validacion-formulario
release/1.2.0
hotfix/token-expiracion-infinita
```

### Reglas

- Usar **kebab-case** (minúsculas con guiones).
- Ser descriptivo pero conciso (máximo 4-5 palabras).
- No usar tildes ni caracteres especiales.
- No usar números de ticket como único descriptor: ❌ `feature/JIRA-123` → ✅ `feature/JIRA-123-login-google`

---

## ✍️ Convención de Commits

Seguimos la especificación **Conventional Commits**:

```
<tipo>(<scope>): <descripción corta>

[cuerpo opcional]

[footer opcional]
```

### Tipos de commits

| Tipo | Cuándo usarlo |
|------|---------------|
| `feat` | Nueva funcionalidad para el usuario |
| `fix` | Corrección de un bug |
| `refactor` | Refactorización sin cambio de comportamiento |
| `style` | Cambios de formato, espaciado, etc. (sin lógica) |
| `test` | Agregar o modificar tests |
| `docs` | Cambios en documentación |
| `chore` | Tareas de mantenimiento (deps, configs, CI) |
| `perf` | Mejoras de rendimiento |
| `revert` | Revertir un commit anterior |

### Scope (opcional)

Indica el módulo o pantalla afectada:

```
feat(auth): agregar login con biometría
fix(home): corregir scroll infinito en feed
chore(deps): actualizar react-native a 0.74
```

### Ejemplos de commits correctos

```bash
feat(auth): implementar login con Google
fix(perfil): corregir crash al subir foto de perfil
refactor(api): extraer lógica de fetch a custom hook
style(home): alinear cards según diseño de Figma
test(carrito): agregar tests unitarios al reducer
docs: actualizar README con instrucciones de setup
chore(deps): actualizar dependencias de seguridad
```

### Reglas de escritura

- La descripción va en **minúscula** y sin punto final.
- Usar el **modo imperativo**: "agregar", "corregir", "actualizar" (no "agregué", "se corrigió").
- Máximo **72 caracteres** en la primera línea.
- Si el cambio es grande, usar el cuerpo para explicar el **por qué**, no el qué.

---

## 🔄 Flujo de Trabajo Paso a Paso

### Desarrollar una nueva feature

```bash
# 1. Asegurarse de estar en develop actualizado
git checkout develop
git pull origin develop

# 2. Crear la rama de feature
git checkout -b feature/nombre-de-la-feature

# 3. Trabajar y commitear
git add .
git commit -m "feat(scope): descripción del cambio"

# 4. Subir la rama al remoto
git push origin feature/nombre-de-la-feature

# 5. Abrir Pull Request hacia develop
# (Nunca mergear directo sin PR)
```

### Crear una release

```bash
# 1. Crear rama de release desde develop
git checkout develop
git pull origin develop
git checkout -b release/1.2.0

# 2. Ajustes finales (versión, changelogs, etc.)
git commit -m "chore(release): bump version a 1.2.0"

# 3. Mergear a main y develop
git checkout main
git merge --no-ff release/1.2.0
git tag -a v1.2.0 -m "Release 1.2.0"

git checkout develop
git merge --no-ff release/1.2.0

# 4. Eliminar la rama
git branch -d release/1.2.0
```

### Aplicar un hotfix

```bash
# 1. Crear rama de hotfix desde main
git checkout main
git pull origin main
git checkout -b hotfix/descripcion-del-fix

# 2. Corregir y commitear
git commit -m "fix(scope): descripción del fix crítico"

# 3. Mergear a main y develop
git checkout main
git merge --no-ff hotfix/descripcion-del-fix
git tag -a v1.2.1 -m "Hotfix 1.2.1"

git checkout develop
git merge --no-ff hotfix/descripcion-del-fix

# 4. Eliminar la rama
git branch -d hotfix/descripcion-del-fix
```

---

## 🚫 Qué NO hacer

- ❌ Commitear directamente en `main` o `develop`.
- ❌ Hacer `git push --force` en ramas compartidas.
- ❌ Mergear sin Pull Request / Code Review.
- ❌ Usar mensajes de commit vagos: `fix`, `cambios`, `wip`, `asdf`.
- ❌ Acumular muchos cambios no relacionados en un solo commit.
- ❌ Dejar ramas sin eliminar después de mergear.

---

## ✅ Checklist antes de abrir un Pull Request

- [ ] La rama parte desde la base correcta (`develop` o `main`).
- [ ] Los commits siguen la convención definida.
- [ ] El código compila y no rompe tests existentes.
- [ ] Se agregaron tests si corresponde.
- [ ] Se actualizó la documentación si hubo cambios de API o comportamiento.
- [ ] El PR tiene una descripción clara de qué se hizo y por qué.

---

> 💡 Ante dudas, consultá primero en el canal del equipo antes de crear ramas o mergear.
