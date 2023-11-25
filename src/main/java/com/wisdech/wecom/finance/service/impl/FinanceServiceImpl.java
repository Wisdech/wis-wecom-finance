package com.wisdech.wecom.finance.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencent.wework.Finance;
import com.wisdech.wecom.finance.entity.Credential;
import com.wisdech.wecom.finance.exception.FinanceSDKException;
import com.wisdech.wecom.finance.helper.PrivateKeyHelper;
import com.wisdech.wecom.finance.service.FinanceService;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class FinanceServiceImpl implements FinanceService {

    private Credential credential;
    private Long financeSdk;

    @Override
    public void initSdk(Credential credential) throws Exception {

        this.credential = credential;

        initTencentFinance();
    }

    void initTencentFinance() {
        int ret;
        long sdk = Finance.NewSdk();

        ret = Finance.Init(sdk, credential.getCorpId(), credential.getSecret());
        if (ret != 0) {
            Finance.DestroySdk(sdk);
        }

        financeSdk = sdk;
    }

    @Override
    public List<JSONObject> getMessage(int seq, int limit) throws Exception {
        if (ObjectUtil.isNull(financeSdk)) {
            return null;
        }

        long slice = Finance.NewSlice();
        int ret = Finance.GetChatData(financeSdk, seq, limit, "", "", 30, slice);
        if (ret != 0) {
            Finance.FreeSlice(slice);
            throw new FinanceSDKException(ret);
        }
        String result = Finance.GetContentFromSlice(slice);
        Finance.FreeSlice(slice);

        JSONObject jsonObject = JSONUtil.parseObj(result);

        if (!jsonObject.get("errcode").equals(0)) {
            throw new Exception(String.valueOf(jsonObject.get("errmsg")));
        }

        List<JSONObject> encryptMessages = (new JSONArray(jsonObject.get("chatdata"))).toList(JSONObject.class);
        List<JSONObject> decryptMessages = new ArrayList<>();
        for (JSONObject object : encryptMessages) {

            JSONObject decryptMessage = decryptData(object);

            decryptMessages.add(decryptMessage);

        }
        return decryptMessages;
    }


    private JSONObject decryptData(JSONObject object) throws FinanceSDKException, NoSuchAlgorithmException, InvalidKeySpecException {

        if (ObjectUtil.isNull(financeSdk)) {
            return null;
        }

        String encryptRandomKey = (String) object.get("encrypt_random_key");
        String encryptMessage = (String) object.get("encrypt_chat_msg");
        Long seq = Long.valueOf((Integer) object.get("seq"));

        String encryptKey = decryptRandomKey(encryptRandomKey, PrivateKeyHelper.fromString(credential.getPrivateKey()));

        long msg = Finance.NewSlice();
        int ret = Finance.DecryptData(financeSdk, encryptKey, encryptMessage, msg);
        if (ret != 0) {
            Finance.FreeSlice(msg);
            throw new FinanceSDKException(ret);
        }
        String result = Finance.GetContentFromSlice(msg);
        Finance.FreeSlice(msg);

        JSONObject resultJson = JSONUtil.parseObj(result);
        resultJson.append("seq", seq);

        return resultJson;

    }

    private String decryptRandomKey(String encryptRandomKey, RSAPrivateKey privateKey) {

        String decryptRandomKey = null;

        try {

            byte[] encryptRandomKeyBytes = Base64.decodeBase64(encryptRandomKey);
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            decryptRandomKey = new String(cipher.doFinal(encryptRandomKeyBytes));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return decryptRandomKey;
    }

    private final String filepath="/tmp/";
    @Override
    public String getMedia(String sdkFileId) throws Exception {

        String outputFileName = UUID.randomUUID().toString();

        String indexbuf = "";

        File outputFile = new File(filepath + outputFileName);

        while (true) {
            long media_data = Finance.NewMediaData();
            int ret = Finance.GetMediaData(financeSdk, indexbuf, sdkFileId, "", "", 30, media_data);
            if (ret != 0) {
                Finance.FreeMediaData(media_data);
                throw new FinanceSDKException(ret);
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(outputFile, true);
                outputStream.write(Finance.GetData(media_data));
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Finance.IsMediaDataFinish(media_data) == 1) {
                Finance.FreeMediaData(media_data);
                return "文件名";
            } else {
                indexbuf = Finance.GetOutIndexBuf(media_data);
                Finance.FreeMediaData(media_data);
            }
        }
    }


    @Override
    public Boolean destroySdk() {
        Finance.DestroySdk(financeSdk);
        return true;
    }
}
