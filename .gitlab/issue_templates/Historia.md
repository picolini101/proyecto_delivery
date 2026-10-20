<!--
Título del issue: [MÓDULO] + verbo en infinitivo + qué. El número lo pone GitLab (#n).
  [CAT] catálogo · [ORD] pedidos · [DEL] entregas · [PAY] pagos · [USR] usuarios (practitioner)
  Ejemplo: [CAT] Registrar comercio con estado Deshabilitado
Modelo completo: issues #17 y #18 del proyecto.
Al crearla queda en la columna Open del board (Product Backlog). Pasa a To Do cuando
el equipo la elige para el sprint: Milestone = Sprint n y se arrastra de Open a To Do.
-->

## Historia de usuario

Como **<rol>**, quiero **<acción>**, para **<beneficio>**.

<!-- Si es una historia técnica (sin pantalla para el usuario final), explícalo en una línea. -->

## Requerimiento origen

- **<FUN-xxx / INT-xxx>** (<EPIC-XXX-001>): <texto corto del requerimiento>.
- **Decisiones / addendum:** <D-xx o AD-xx que aplican, o "ninguna">.
- **Dependencias:** <issues o contratos que necesita (#n, CatalogService…), o "ninguna">.

## Caso de uso

<!-- CU-xxx-nn · Nombre → clase en application/usecase (ej. CU-USR-05 · Verificar acceso de usuario → CheckUserAccessUseCase).
     Si no implementa un caso de uso de negocio, escribe "No aplica" y explica por qué. -->

**<CU-xxx-nn> · <nombre>** → `<NombreUseCase>`

## Criterios de aceptación

<!-- Uno por regla del Example Mapping: el caso exitoso y al menos un caso alterno que falla. -->

- [ ] **Dado** <contexto>, **cuando** <acción>, **entonces** <resultado esperado>.
- [ ] **Dado** <contexto alterno>, **cuando** <acción>, **entonces** <rechazo o mensaje esperado>.

## Tareas

<!-- Por capa: domain, application (caso de uso), infrastructure (adaptadores, persistencia), pantalla, pruebas. -->

- [ ] 
- [ ] Abrir el MR con la plantilla y `Closes #<n>`.

## Estimación

<!-- Story points acordados con Scrum Poker, en el campo Weight: 0, 1, 2, 3, 5, 8, 13 o 21.
     Más de 21 no se usa: divide la historia. Se estima complejidad y esfuerzo relativos, no horas. -->

Story points (Weight): **<n>**. <Con qué historia se comparó y por qué.>

## Definition of Ready (antes de pasar a "In Progress")

- [ ] Historia "Como…, quiero…, para…" con su **FUN o INT de origen**.
- [ ] **Criterios de aceptación** claros y verificables.
- [ ] Estimada con **Scrum Poker** en **Weight** (story points 0–21).
- [ ] **Pequeña**: 21 puntos o menos; si no, se divide.
- [ ] **Dependencias** con otros módulos identificadas (contrato o fallback disponible).
- [ ] **Coherente con el addendum de requerimientos** (decisiones acordadas en clase, wiki: reporte de la semana 2, sección 12).
- [ ] **Epic**, **milestone**, **prioridad** y **persona asignada** (el **Health status** On track lo pone la plantilla).

## Definition of Done (antes de cerrar)

- [ ] Cumple **todos los criterios de aceptación**.
- [ ] **Pruebas unitarias** y `./mvnw verify` en verde (captura en el MR).
- [ ] **Pruebas funcionales**: probada en `delivery-system`.
- [ ] **Revisión de código** aprobada, hilos resueltos y pipeline en verde.
- [ ] **Documentación** actualizada, si aplica.
- [ ] **Aprobación del Product Owner** (practitioner).

/label ~Feature
/health_status on_track
