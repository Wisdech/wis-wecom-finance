package com.wisdech.wecom.finance.service;

import cn.hutool.json.JSONObject;
import com.wisdech.wecom.finance.entity.Credential;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FinanceService {

    void initSdk(Credential credential) throws Exception;

    List<JSONObject> getMessage(int seq, int limit) throws Exception;

    String getMedia(String sdkFileId) throws Exception;

    Boolean destroySdk();
}
