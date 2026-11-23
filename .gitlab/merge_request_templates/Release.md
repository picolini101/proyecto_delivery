<!--
MR de la versión 1.0.0 o de una corrección del periodo de Integración (Lab 9), con GitHub Flow:
rama corta desde main actualizado y MR hacia main. Desde el congelamiento acordado por los SM
solo entran correcciones y ramas de versión: ninguna historia nueva.
Título: "Release: user-module 1.0.0" o "Fix: <qué se corrige>".
Rama: FEAT-<issue>/release/<módulo>-1.0.0 (versión) o ISSUE-<issue>/bug/<descripcion> (corrección).
-->

## Tipo de cambio

- [ ] **Versión:** POM del módulo en `1.0.0`, propiedad `<módulo>.version` en `delivery-system/pom.xml` y pipeline del módulo.
- [ ] **Corrección:** error encontrado en el periodo de Integración (issue con la plantilla **Bug**).

## Qué cambia

<!-- Versión: archivos y líneas cambiadas. Corrección: causa del error y cómo se corrigió. -->

## Cómo probar

1. `cd <módulo> && ./mvnw verify`
2. `cd delivery-system && docker compose up -d && ./mvnw javafx:run -Pjpa` con la base vacía.
3. <!-- Pasos del flujo con los datos de prueba (SampleUsers, SampleCatalog, SamplePaymentMethods). -->

## Referencias

Closes #

## Checklist del autor

- [ ] La rama sale de `main` actualizado (`git pull --rebase origin main`) y el MR apunta a `main`.
- [ ] Commits pequeños con el formato `FEAT-<issue>: <qué y por qué>` (o `ISSUE-<issue>:` en una corrección) y el correo institucional.
- [ ] El MR resuelve un solo issue y no agrega historias nuevas.
- [ ] Pipeline en verde: linter, pruebas, cobertura y artefacto. En `check-release-versions` ya no aparece mi módulo.
- [ ] Probado con la aplicación armada y MySQL, no solo con las pruebas unitarias.
- [ ] Aprobado por el Scrum Master del equipo (otro SM si el autor es el SM).

## Definition of Done

- [ ] El Product Owner (practitioner) valida y el issue se cierra con `Closes #`.
- [ ] Notas de la versión (`Modulos/<módulo>/Release-1.0.0`) y `Seguimiento/Integracion` actualizados en la wiki.
