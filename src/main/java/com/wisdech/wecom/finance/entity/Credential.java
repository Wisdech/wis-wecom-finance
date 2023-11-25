package com.wisdech.wecom.finance.entity;

import jakarta.persistence.Id;
import lombok.Data;

@Data
public class Credential {

    @Id
    String corpId;

    String secret;
    String privateKey;

}
