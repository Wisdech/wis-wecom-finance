package com.wisdech.wecom.finance.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.tencent.wework.Finance;
import com.wisdech.wecom.finance.exception.FinanceSDKException;
import com.wisdech.wecom.finance.helper.PrivateKeyHelper;
import com.wisdech.wecom.finance.service.FinanceService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileOutputStream;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceServiceImpl implements FinanceService {
    Long financeSdk;
    String corpId;
    String corpSecret;
    String corpPrivateKey;

    @Value("${app.url}")
    String appUrl;

    @Value("${cos.enable}")
    Boolean cosEnable;

    @Value("${cos.bucket}")
    String cosBucket;

    @Value("${cos.region}")
    String cosRegion;

    @Value("${cos.secret_id}")
    String cosSecretId;

    @Value("${cos.secret_key}")
    String cosSecretKey;

    @Value("${cos.cdn_url}")
    String cosCdnUrl;


    @Override
    public void init(String corpId, String secret, String privateKey) throws FinanceSDKException {

        this.corpId = corpId;
        this.corpSecret = secret;
        this.corpPrivateKey = privateKey;

        initTencentFinance();
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

    @Override
    public String getMedia(String sdkFileId, String md5, String filename) throws Exception {

        String outputFileName = (md5 + "." + filename).toUpperCase();

        File outputFile = new File(getFilePath() + outputFileName);

        if (cosEnable ? cosExist(outputFile) : outputFile.exists()) {
            return getFileUrl(outputFileName);
        } else {
            return getRemoteMedia(sdkFileId, outputFile);
        }
    }

    @Override
    public void destroySdk() {
        Finance.DestroySdk(financeSdk);
    }


    void initTencentFinance() throws FinanceSDKException {
        int ret;
        long sdk = Finance.NewSdk();

        ret = Finance.Init(sdk, corpId, corpSecret);

        if (ret != 0) {
            Finance.DestroySdk(sdk);
            throw new FinanceSDKException(ret);
        }

        financeSdk = sdk;
    }

    JSONObject decryptData(JSONObject object) throws Exception {

        if (ObjectUtil.isNull(financeSdk)) {
            return null;
        }

        String encryptRandomKey = (String) object.get("encrypt_random_key");
        String encryptMessage = (String) object.get("encrypt_chat_msg");
        Long seq = Long.valueOf((Integer) object.get("seq"));

        String encryptKey = decryptRandomKey(encryptRandomKey, PrivateKeyHelper.fromString(corpPrivateKey));

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

    String decryptRandomKey(String encryptRandomKey, RSAPrivateKey privateKey) throws Exception {

        String decryptRandomKey;

        byte[] encryptRandomKeyBytes = Base64.decodeBase64(encryptRandomKey);
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        decryptRandomKey = new String(cipher.doFinal(encryptRandomKeyBytes));

        return decryptRandomKey;
    }

    String getFilePath() throws Exception {

        String filepath = "./message-assets/" + corpId + "/";

        File file = new File(filepath);

        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new Exception("文件目录创建失败");
            }
        }

        return filepath;
    }

    String getFileUrl(String filename) {
        return (cosEnable ? cosCdnUrl + "/" : (appUrl + "/message-assets/")) + corpId + "/" + filename;
    }

    String getRemoteMedia(String sdkFileId, File outputFile) throws Exception {
        String indexbuf = "";

        while (true) {
            long media_data = Finance.NewMediaData();
            int ret = Finance.GetMediaData(financeSdk, indexbuf, sdkFileId, "", "", 30, media_data);
            if (ret != 0) {
                Finance.FreeMediaData(media_data);
                throw new FinanceSDKException(ret);
            }

            FileOutputStream outputStream = new FileOutputStream(outputFile, true);
            outputStream.write(Finance.GetData(media_data));
            outputStream.close();

            if (Finance.IsMediaDataFinish(media_data) == 1) {
                Finance.FreeMediaData(media_data);
                return cosEnable ? cosStore(outputFile) : getFileUrl(outputFile.getName());
            } else {
                indexbuf = Finance.GetOutIndexBuf(media_data);
                Finance.FreeMediaData(media_data);
            }
        }
    }

    String cosStore(File file) {

        COSClient cosClient = initClient();

        String key = corpId + "/" + file.getName();
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosBucket, key, file);
        cosClient.putObject(putObjectRequest);

        if (!file.delete()) {

            final String loggerName = "Request:%s";

            final Logger logger = LoggerFactory.getLogger(
                    String.format(loggerName, MDC.get("TRACE_ID")));

            logger.error("COS Store Success but local file Delete Failed");
        }

        return getFileUrl(file.getName());
    }

    Boolean cosExist(File file) {

        COSClient cosClient = initClient();

        String key = corpId + "/" + file.getName();

        return cosClient.doesObjectExist(cosBucket, key);

    }

    COSClient initClient() {
        COSCredentials cred = new BasicCOSCredentials(cosSecretId, cosSecretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(cosRegion));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(cred, clientConfig);
    }
}
