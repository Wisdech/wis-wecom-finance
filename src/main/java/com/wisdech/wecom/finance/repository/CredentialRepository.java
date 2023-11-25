package com.wisdech.wecom.finance.repository;

import com.wisdech.wecom.finance.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRepository extends JpaRepository<String, Credential> {
}
