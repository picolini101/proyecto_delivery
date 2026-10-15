<!--
Título del MR: claro y conciso, con el prefijo del tipo de cambio.
  Ejemplos: "Feat: registrar comercio con estado Deshabilitado"
            "Fix: validar precio mayor a cero al editar producto"
-->

## Contexto

<!-- ¿Por qué se hace este cambio? ¿Qué requerimiento o problema resuelve? (FUN-xxx / INT-xxx) -->

## Cambios realizados

<!-- Descripción de alto nivel de las clases creadas o modificadas, por capa. -->

- `domain`:
- `application`:
- `infrastructure`:

## Cómo probar

<!-- Pasos para que el revisor verifique el cambio: comandos, datos de prueba, tests incluidos. -->

1. `cd <modulo> && ./mvnw verify`
2.

## Capturas

<!-- Compilación y tests en verde en local; capturas o GIF de la pantalla si hay cambios visuales. -->

## Referencias

Closes #

## Checklist del autor

- [ ] La rama sale de `main` actualizado (`git pull --rebase origin main`) y sigue el formato
      `FEAT-<issue>/feature/<descripcion>` o `ISSUE-<issue>/bug/<descripcion>`.
- [ ] Commits pequeños y atómicos, con el formato `FEAT-<issue>: <qué y por qué>`
      (ej. `FEAT-54: Add classes to map user information.`).
- [ ] El MR resuelve un solo issue y no mezcla cambios ajenos.
- [ ] `./mvnw verify` pasa en local y el pipeline está en verde.
- [ ] Asigné al menos un revisor de mi equipo.
- [ ] Respondí todos los comentarios y los hilos quedaron resueltos.
