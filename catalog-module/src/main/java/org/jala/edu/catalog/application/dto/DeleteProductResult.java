package org.jala.edu.catalog.application.dto;

/**
 * Resultado de eliminar un producto. Permite a la pantalla informar si, además,
 * el comercio pasó a Deshabilitado (no es un error, es un aviso).
 */
public enum DeleteProductResult {
    DELETED,
    DELETED_COMMERCE_DISABLED
}