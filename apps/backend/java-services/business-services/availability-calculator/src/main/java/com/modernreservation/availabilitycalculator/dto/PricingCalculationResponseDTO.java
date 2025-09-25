package com.modernreservation.availabilitycalculator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for pricing calculations
 */
@Schema(description = "Pricing calculation response")
public record PricingCalculationResponseDTO(

    @Schema(description = "Base rate per night", example = "200.00")
    BigDecimal baseRate,

    @Schema(description = "Current rate after adjustments", example = "180.00")
    BigDecimal currentRate,

    @Schema(description = "Number of nights", example = "2")
    Integer nights,

    @Schema(description = "Subtotal before taxes and fees", example = "360.00")
    BigDecimal subtotal,

    @Schema(description = "Applied discount amount", example = "20.00")
    BigDecimal discountAmount,

    @Schema(description = "Discount percentage", example = "5.56")
    BigDecimal discountPercentage,

    @Schema(description = "Tax amount", example = "43.20")
    BigDecimal taxAmount,

    @Schema(description = "Tax percentage", example = "12.0")
    BigDecimal taxPercentage,

    @Schema(description = "Service fee amount", example = "18.00")
    BigDecimal serviceFeeAmount,

    @Schema(description = "Service fee percentage", example = "5.0")
    BigDecimal serviceFeePercentage,

    @Schema(description = "Total amount including all fees and taxes", example = "421.20")
    BigDecimal totalAmount,

    @Schema(description = "Currency code", example = "USD")
    String currency,

    @Schema(description = "Applied promotional codes")
    List<String> appliedPromoCodes,

    @Schema(description = "Pricing method used", example = "DYNAMIC")
    String pricingMethod,

    @Schema(description = "Detailed breakdown of pricing components")
    List<PricingBreakdownItem> priceBreakdown
) {

    /**
     * Nested record for pricing breakdown items
     */
    @Schema(description = "Individual pricing component")
    public record PricingBreakdownItem(
        @Schema(description = "Component name", example = "Base Rate")
        String name,

        @Schema(description = "Component amount", example = "200.00")
        BigDecimal amount,

        @Schema(description = "Component description", example = "Standard room rate")
        String description
    ) {}

    /**
     * Calculates the savings amount
     */
    public BigDecimal getSavings() {
        if (baseRate == null || currentRate == null || nights == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal originalTotal = baseRate.multiply(BigDecimal.valueOf(nights));
        BigDecimal currentTotal = currentRate.multiply(BigDecimal.valueOf(nights));
        return originalTotal.subtract(currentTotal);
    }

    /**
     * Gets the savings percentage
     */
    public BigDecimal getSavingsPercentage() {
        if (baseRate == null || currentRate == null || baseRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = baseRate.subtract(currentRate);
        return savings.divide(baseRate, 4, java.math.RoundingMode.HALF_UP)
                     .multiply(BigDecimal.valueOf(100));
    }
}
