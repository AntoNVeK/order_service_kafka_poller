package dev.kornienko.stub_service;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties("stub")
public class StubProperties {

    // inventory properties
    private boolean inventoryRejectEnabled = true;

    @PositiveOrZero
    @Max(30_000)
    private int inventoryLatencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int inventoryLatencyMaxMillis;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double inventoryRejectProbability;

    // pricing properties
    private boolean pricingExceptionEnabled;

    @PositiveOrZero
    @Max(30_000)
    private int pricingLatencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int pricingLatencyMaxMillis;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double pricingExceptionProbability;

    // shipping properties
    private boolean shippingExceptionEnabled;

    @PositiveOrZero
    @Max(30_000)
    private int shippingLatencyMinMillis;

    @PositiveOrZero
    @Max(30_000)
    private int shippingLatencyMaxMillis;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double shippingExceptionProbability;
}
