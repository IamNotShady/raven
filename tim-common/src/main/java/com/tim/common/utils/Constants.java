package com.tim.common.utils;

/**
 * Author zxx Description 常量 Date Created on 2018/6/4
 */
public class Constants {

    /**
     * redis key
     */
    public static final String USER_ROUTE_KEY = "user_route_key";

    public static final String ACCESS_SERVER_ROUTE_KEY = "access_server_route_key_";

    public static final String PREFIX_CONVERSATION_ID = "converid_";
    public static final String PREFIX_CONVERSATION_LIST = "converlist_";
    public static final String PREFIX_MESSAGE_ID = "msg_";
    /*
     * Authentication.
     * */
    public static final String AUTH_APP_KEY = "AppKey";

    public static final String AUTH_NONCE = "Nonce";

    public static final String AUTH_TIMESTAMP = "Timestamp";

    public static final String AUTH_SIGNATURE = "Sign";

    public static final String AUTH_TOKEN = "Token";

    public static final long TOKEN_CACHE_DURATION = 7; // 7 days

    /**
     * Default cipher algorithm
     */
    public static final String DEFAULT_CIPHER_ALGORITHM = "DES";

    /**
     * Default separate sign.
     */
    public static final String DEFAULT_SEPARATES_SIGN = ":";

    /**
     * Global config.
     */
    public static final String CONFIG_NETTY_PORT = "netty-port";
}
