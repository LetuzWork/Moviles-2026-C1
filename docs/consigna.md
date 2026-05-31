# IFTS 18 - Desarrollo de Software

**Fecha:** April 22, 2026  
**Materia:** Desarrollo de Aplicaciones para Dispositivos Móviles  
**Trabajo:** Trabajo Práctico Grupal Integrador  
**Turno:** Martes y Miércoles Turno Noche  
**Comisión:** 2° Año "B"

---

# Encuadre General

El presente trabajo práctico propone el desarrollo desde el diseño hasta la puesta en producción de una aplicación para dispositivos móviles con persistencia, debiendo para esto aplicar buenas prácticas de arquitectura, diseño, pruebas, integración y documentación.

La consigna es de tipo abierta, es decir, los equipos de trabajo deberán elegir el dominio real, justificar adecuadamente el problema que se pretende resolver y diseñar la solución.

El trabajo contará con 2 entregas, las cuales resultarán de carácter obligatorio, siendo la primera de forma asincrónica y la última sincrónica y presencial.

## Hitos

### H1 - Obligatorio
- Figma
- Flujo de pantallas
- Repositorio inicializado
- Tablero de seguimiento
- APK Demo (puede ser mockeado)
- Diagrama inicial de arquitectura con descripción de las tecnologías elegidas

### H2 - Obligatorio
- Feature set completo
- APK/AAB RC
- Documentación final
- Defensa

---

# Gestión del Proyecto

Se deberán tener en cuenta los siguientes criterios:

1. Equipo con roles (rotativos o fijos):
   - Product Owner
   - Tech Lead
   - UX/UI
   - Backend Lead
   - QA/DevOps
   - Otros que el equipo considere

2. Tablero de seguimiento del proyecto con acceso docente:
   - Jira
   - GitHub Projects

---

# Objetivos de Aprendizaje

1. Aplicar el concepto de diseño centrado en el usuario:
   - Investigación breve
   - Prototipado
   - Validación

2. Implementar arquitecturas modernas:
   - MVVM
   - Capas
   - Repositorios

3. Diseñar e integrar persistencia:
   - Persistencia local
   - API REST / GraphQL
   - Backend server

4. Diseñar e implementar:
   - Pruebas unitarias
   - Métricas de calidad

5. Elaborar:
   - Documentación técnica
   - Documentación de usuario

6. Presentar y defender técnicamente el producto solución.

---

# Requisitos Funcionales

La aplicación deberá implementar mínimamente los siguientes requisitos funcionales:

1. Flujo de onboarding inicial al momento de la instalación.

2. Al menos 2 flujos de pantallas diferentes, incluyendo:
   - Un CRUD completo del dominio principal.

3. Al menos un listado compuesto por cards view.

4. Uso de al menos:
   - Un sensor
   - Y/o dispositivo de captura:
     - Audio
     - Cámara

5. Los tamaños de fuente deberán ser escalables.

---

# Requisitos No Funcionales

1. Performance:
   - Cold start menor a 2.5 segundos
   - Dispositivo objetivo:
     - Google Pixel 9 Pro
     - 4GB RAM
     - 2 cores
   - Scroll fluido:
     - Mayor a 54 FPS

2. Buen manejo de errores de conectividad.

3. Evaluar correctamente el mínimo API Level según el público objetivo.

---

# Requisitos Arquitectónicos

1. Lenguaje:
   - Kotlin

2. Patrón:
   - MVVM + Repository

3. UI:
   - Jetpack Compose

4. Consumo de APIs:
   - retrofit2
   - Gson
   - O similares

5. Accesibilidad:
   - Material 3
   - Dark mode
   - Dynamic color

---

# Requisitos UI/UX y CX

1. Aplicar:
   - Material Design 3
   - Heurísticas de Nielsen
   - Evidenciar en checklist

2. Wireframes:
   - Prototipo de alta fidelidad en Figma

---

# Ciclo de Desarrollo y Colaboración

1. Repositorio:
   - GitHub
   - GitLab
   - Público o privado con acceso docente

2. Estrategia de ramas:
   - Trunk-based
   - GitFlow
   - Definir política de PRs
   - Evidenciar trabajo en grupo

3. Diagrama de alto nivel de arquitectura de la solución.

---

# Lineamientos para la Presentación Final

1. Demo en vivo:
   - 10 minutos
   - QA:
     - 5 minutos

2. Pitch:
   - Problema
   - Usuarios
   - Métricas
   - Arquitectura
   - Decisiones clave
   - Aprendizajes

3. Entrega:
   - Release Candidate
   - Documentación completa

4. Puntaje mínimo:
   - 60/100
   - Cumplimiento obligatorio de H1 y H2

5. Repos accesibles, builds reproducibles y demo funcional.

---

# Uso Responsable de IA

Se permite apoyo con IA (copilots, LLMs) declarando:

- Prompts relevantes
- Fragmentos generados
- Revisión humana realizada

Prohibido subir:
- Claves
- Datos de terceros