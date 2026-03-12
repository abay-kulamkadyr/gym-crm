package com.epam.infrastructure.security.core;

public final class FeignTokenHolder {
    private static volatile String token;

    public static void set(String t) {
        token = t;
    }

    public static String get() {
        return token;
    }

    public static void clear() {
        token = null;
    }
}
