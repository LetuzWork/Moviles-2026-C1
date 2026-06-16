# Declaración de Uso Responsable de IA — MenúSemana

**Versión:** 1.0  
**Fecha:** 2026-06-15

En cumplimiento del punto **0.1 "Uso responsable de IA"** de la consigna, el equipo declara el uso de asistentes de IA durante el desarrollo, los fragmentos generados y la revisión humana realizada.

---

## 1. Herramientas utilizadas

| Herramienta | Uso principal |
|-------------|---------------|
| **Claude Code (Claude Opus)** | Asistencia en código, revisión de arquitectura, configuración de CI/CD, generación de documentación. |
| *(Completar por el equipo)* | Otros copilots/LLM usados durante H1/H2 (p. ej. ChatGPT, GitHub Copilot) y para qué. |

> ⚠️ **No se subieron** claves, tokens, credenciales ni datos de terceros a ninguna herramienta de IA. El backend consumido (TheMealDB) es una API pública sin autenticación.

---

## 2. Prompts relevantes

Ejemplos representativos de las consignas dadas a la IA (parafraseados):

- *"Revisá el estado del proyecto contra la consigna y decime qué falta para H2."*
- *"Configurá un workflow de GitHub Actions que corra lint + tests unitarios en cada PR."*
- *"Hay una falla al agregar una receta y volver al menú de recetas; revisá la arquitectura de navegación y proponé un fix."*
- *"Generá la checklist de heurísticas de Nielsen y la documentación de H2."*
- *"Configurá la firma de release (keystore + signingConfig) y generá el AAB."*

---

## 3. Fragmentos generados con asistencia de IA

| Artefacto | Descripción | Revisión humana |
|-----------|-------------|-----------------|
| `.github/workflows/ci.yml` | Workflow de CI (lint + tests, JDK 21). | Verificado: CI en verde sobre PR real (#48). |
| Fix de navegación (`RecipeDetailScreen`, `RecetasScreen`, `MainActivity`) | Corrección del back-stack al importar receta. | Diagnóstico validado leyendo el código; build local `BUILD SUCCESSFUL`. |
| `docs/checklist_nielsen.md`, `docs/declaracion_ia.md`, `docs/pitch_h2.md` | Documentación de H2. | Contenido revisado y ajustado al dominio real por el equipo. |
| Configuración de firma release + AAB | `signingConfig` y generación del artefacto RC. | Verificado con `apksigner verify`. |

> El detalle de fragmentos generados durante **H1** (estructura inicial, pantallas Compose, repositorios) debe completarlo el equipo según corresponda.

---

## 4. Revisión humana

- Todo el código asistido por IA fue **revisado vía Pull Request** antes de mergear a `main` (política GitFlow del repo, ver [`GITFLOW.md`](../GITFLOW.md)).
- Las afirmaciones de la IA (p. ej. el diagnóstico del bug de navegación) se **contrastaron contra el código fuente** y se **verificaron compilando y ejecutando** la app y los tests.
- El equipo es responsable final del código entregado; la IA se usó como herramienta de apoyo, no como reemplazo del criterio técnico.

---

## 5. Principios aplicados

1. **Transparencia:** se declara el uso de IA en esta entrega.
2. **No exposición de secretos:** ninguna clave ni dato sensible fue compartido con la IA.
3. **Verificación:** ningún fragmento se integró sin compilar, testear y revisar.
4. **Autoría responsable:** las decisiones de diseño y arquitectura las tomó el equipo.
