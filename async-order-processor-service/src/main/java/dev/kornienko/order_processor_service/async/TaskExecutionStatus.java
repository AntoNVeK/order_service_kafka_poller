package dev.kornienko.order_processor_service.async;

import dev.kornienko.order_processor_service.utils.EnumUtils;

public enum TaskExecutionStatus implements EnumUtils.StringEnum {

    SUCCESS("SUCCESS"),
    RETRYABLE_ERROR("RETRYABLE_ERROR"),
    NON_RETRYABLE_ERROR("NON_RETRYABLE_ERROR"),;

    private final String value;

    TaskExecutionStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
