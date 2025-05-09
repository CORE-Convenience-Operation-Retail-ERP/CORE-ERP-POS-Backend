package com.core.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderProductResponseDTO {

    private Integer productId;
    private String productName;
    private Integer unitPrice;
    private Integer stockQty;
    private Integer proStockLimit;
    private Integer isPromo;
}
