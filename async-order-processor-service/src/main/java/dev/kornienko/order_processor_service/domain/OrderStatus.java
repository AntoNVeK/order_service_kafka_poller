package dev.kornienko.order_processor_service.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import dev.kornienko.order_processor_service.utils.EnumUtils;

public enum OrderStatus implements EnumUtils.IntEnum, EnumUtils.StringEnum {

    CREATION_PENDING(1, "CREATION_PENDING"),
    CREATED_SUCCESSFULLY(2, "CREATED_SUCCESSFULLY"),
    CANCELLED(3, "CANCELLED");

    private final int code;
    private final String stringValue;

    OrderStatus(int code, String stringValue) {
        this.code = code;
        this.stringValue = stringValue;
    }

    @JsonValue
    @Override
    public String getValue() {
        return stringValue;
    }

    @JsonCreator
    public static OrderStatus fromStringValue(int code) {
        return EnumUtils.fromCode(OrderStatus.class, code);
    }

    public static OrderStatus fromCode(int code) {
        return EnumUtils.fromCode(OrderStatus.class, code);
    }

    @Override
    public int getCode() {
        return code;
    }
}
