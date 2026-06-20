package com.fabriciosanches.domain.port.output;

import java.math.BigDecimal;

public interface CatalogClientPort {

    BigDecimal getProductPrice(String productId);

    void validateStock(String productId, int quantity);
}
