package com.wisdech.wecom.finance.service;

import cn.hutool.json.JSONObject;

import java.util.List;

public interface FinanceService {

    void init(String corpId, String secret, String privateKey) throws Exception;

    List<JSONObject> getMessage(int seq, int limit) throws Exception;

    String getMedia(String sdkFileId, String md5, String filename) throws Exception;

    void destroySdk();
}
