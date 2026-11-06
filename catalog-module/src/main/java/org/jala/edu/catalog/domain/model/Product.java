package org.jala.edu.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.core.domain.exception.DomainException;
import org.jala.edu.core.domain.model.BaseModel;

/**
 * Todas las reglas de negocio relativas a un producto válido residen aquí.
 * Hace referencia a su comercio únicamente mediante un id, tal como exigen los límites del módulo.
 */
@Getter
@ToString(callSuper = true)
public class Product extends BaseModel<ProductId> {

    private final UUID commerceId;
    private String name;
    private String category;
    private BigDecimal price;

    private Product(ProductId id, UUID commerceId, String name, String category, BigDecimal price,
                    Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.commerceId = Objects.requireNonNull(commerceId, "Commerce is required");
        this.name = requireText(name, "Name");
        this.category = requireText(category, "Category");
        this.price = requirePositive(price);
    }

    /**
     * Crea un producto nuevo perteneciente al comercio indicado.
     */
    public static Product register(UUID commerceId, String name, String category, BigDecimal price) {
        Instant now = now();
        return new Product(ProductId.newId(), commerceId, name, category, price, now, now);
    }

    /**
     * Modifica los datos editables del producto. Valida todo ANTES de asignar, para que un dato
     * inválido no deje el producto a medio modificar (el repositorio en memoria guarda la misma instancia).
     */
    public void update(String name, String category, BigDecimal price) {
        String validName = requireText(name, "Name");
        String validCategory = requireText(category, "Category");
        BigDecimal validPrice = requirePositive(price);

        this.name = validName;
        this.category = validCategory;
        this.price = validPrice;
        touch();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new DomainException(field + " cannot be blank");
        }
        return value.strip();
    }

    private static BigDecimal requirePositive(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new DomainException("Price must be greater than zero");
        }
        return price;
    }
}
