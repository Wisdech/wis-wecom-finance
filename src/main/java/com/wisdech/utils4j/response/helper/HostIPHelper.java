package com.wisdech.utils4j.response.helper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class HostIPHelper {

    public String hostIP() {
        try {

            if (Objects.isNull(System.getenv("APP_HOST"))) {
                InetAddress address = InetAddress.getLocalHost();
                return address.getHostAddress();
            } else {
                return System.getenv("APP_HOST");
            }

        } catch (UnknownHostException e) {

            return e.getMessage();

        }
    }

    public static String getHostIP() {
        return (new HostIPHelper()).hostIP();
    }
}
