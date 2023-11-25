package com.wisdech.wecom.finance.exception;

import java.util.Map;

public class FinanceSDKException extends Exception {

    private static final Map<Integer, String> errorMap = Map.of(
            10000, "Param Error",
            10001, "Network Error",
            10002, "Data Parsing Failed",
            10003, "System Call Error, Please Retry",
            10005, "SDK File ID Error",
            10006, "Decrypt Failed",
            10008, "Decrypt Message Error",
            10009, "IP Not In Whitelist",
            10010, "Data Expired",
            10011, "SSL Error"
    );


    public FinanceSDKException(int ret) {
        super("FinanceSDK Exception:" + errorMap.get(ret));
    }
}
