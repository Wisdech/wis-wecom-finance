package com.wisdech.utils4j.response.helper;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostIPHelper {
    public static String IP() {
        try {
            InetAddress address = InetAddress.getLocalHost();

            return address.getHostAddress();

        } catch (UnknownHostException e) {

            return e.getMessage();

        }

    }
}
