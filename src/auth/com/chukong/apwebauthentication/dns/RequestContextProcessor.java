package com.chukong.apwebauthentication.dns;

import java.net.DatagramPacket;

/**
 * User: cairne
 * Date: 13-5-11
 * Time: 下午7:59
 */
public class RequestContextProcessor {

    public static void processRequest(DatagramPacket datagramPacket) {
        RequestContext.setClientIps(datagramPacket.getAddress().getHostAddress());
    }
}
