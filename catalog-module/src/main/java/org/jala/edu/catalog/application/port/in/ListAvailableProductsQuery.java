package org.jala.edu.catalog.application.port.in;

import java.util.UUID;

public record ListAvailableProductsQuery(
        UUID commerceId,
        int page,
        String searchText){
    //Consulta sin busqueda: catalogo completo
        public ListAvailableProductsQuery(UUID commerceId, int page) {
            this(commerceId, page, null);
        }
}