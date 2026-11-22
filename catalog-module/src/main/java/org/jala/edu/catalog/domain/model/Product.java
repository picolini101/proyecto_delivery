package org.jala.edu.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.ToString;
import org.jala.edu.catalog.domain.matching.TextMatchStrategy;
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
    private int stock;
    private ProductStatus status;

    private Product(ProductId id, UUID commerceId, String name, String category, BigDecimal price,
                    Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.commerceId = Objects.requireNonNull(commerceId, "Commerce is required");
        this.name = requireText(name, "Name");
        this.category = requireText(category, "Category");
        this.price = requirePositive(price);
        this.stock = 0;
        this.status = ProductStatus.AVAILABLE;
    }

    /**
     * Crea un producto nuevo perteneciente al comercio indicado.
     */
    public static Product register(UUID commerceId, String name, String category, BigDecimal price) {
        Instant now = now();
        return new Product(ProductId.newId(), commerceId, name, category, price, now, now);
    }

    /**
     * Restaura un producto existente desde la persistencia. Recibe todos los campos guardados
     * (8), por eso se justifica la regla ParameterNumber.
     */
    @SuppressWarnings("checkstyle:ParameterNumber")
    public static Product restore(
            ProductId id,
            UUID commerceId,
            String name,
            String category,
            BigDecimal price,
            int stock,
            Instant createdAt,
            Instant updatedAt) {

        if (stock < 0) {
            throw new DomainException("Stock cannot be negative");
        }

        Product product = new Product(
                id, commerceId, name, category, price, createdAt, updatedAt);

        product.stock = stock;
        product.status = ProductStatus.AVAILABLE;
        return product;
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
    public void adjustStock(int quantity) {
        if (quantity < 0) {
            throw new DomainException("Stock cannot be negative");
        }

        this.stock = quantity;
        touch();
    }
    public void reserveStock(int quantity) {
        requirePositiveQuantity(quantity);

        if (stock < quantity) {
            throw new DomainException("Insufficient stock");
        }

        this.stock -= quantity;
        touch();
    }
    public void releaseStock(int quantity) {
        requirePositiveQuantity(quantity);

        try {
            this.stock = Math.addExact(stock, quantity);
        } catch (ArithmeticException exception) {
            throw new DomainException("Stock exceeds the maximum allowed value");
        }

        touch();
    }

    public void enable() {
        if (status != ProductStatus.DISABLED) {
            throw new DomainException("Product is not disabled");
        }

        this.status = stock > 0
                ? ProductStatus.AVAILABLE
                : ProductStatus.OUT_OF_STOCK;
        touch();
    }

    public void disable() {
        this.status = ProductStatus.DISABLED;
        touch();
    }

    public void updateStatus(ProductStatus newStatus) {
        if (status == ProductStatus.DISABLED) {
            throw new DomainException("Disabled product cannot be reactivated");
        }

        if (newStatus != ProductStatus.AVAILABLE
                && newStatus != ProductStatus.OUT_OF_STOCK) {
            throw new DomainException("Invalid product status");
        }

        this.status = newStatus;
        touch();
    }

    public boolean isAvailable() {
        return status == ProductStatus.AVAILABLE;
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new DomainException("Quantity must be greater than zero");
        }
    }

    /**
     * Un producto es comprable si está Disponible y tiene stock (FUN-005).
     * Regla única reutilizada por la habilitación del comercio y por el catálogo (DRY).
     */
    public boolean isPurchasable() {
        return isAvailable() && stock > 0;
    }

    /*
     Coincidencia de búsqueda del catálogo un texto vacío coincide con todo
     */

    /**
     * Coincidencia de búsqueda según la estrategia indicada; un texto vacío coincide con todo.
     * El texto se limpia de espacios y se compara con el nombre y con la categoría.
     */
    public boolean matches(String text, TextMatchStrategy strategy) {
        Objects.requireNonNull(strategy, "Strategy is required");
        if (text == null || text.isBlank()) {
            return true;
        }
        String query = text.strip();
        return strategy.matches(name, query) || strategy.matches(category, query);
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