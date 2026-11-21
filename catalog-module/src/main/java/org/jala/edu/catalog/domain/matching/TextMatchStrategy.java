package org.jala.edu.catalog.domain.matching;
/*
  Estrategia para decidir si un texto de búsqueda coincide con un texto candidato.
 Permite cambiar el criterio de búsqueda sin modificar codigo ni lso casos de uso
 */
@FunctionalInterface
public interface TextMatchStrategy {

    boolean matches(String candidate, String query);
}