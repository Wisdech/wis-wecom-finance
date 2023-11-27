package com.wisdech.utils4j.response;

import lombok.Getter;

@Getter
public enum ARShowType {

    SILENT(1),
    MESSAGE_WARN(2),
    MESSAGE_ERROR(3),
    NOTIFICATION(4),
    PAGE(9);

    private final Integer type;

    ARShowType(Integer type) {
        this.type = type;
    }

}
