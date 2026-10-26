package com.example.blog.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private IpUtil() {
    }

    /**
     * 依次从 X-Real-IP、X-Forwarded-For（取第一个）、remoteAddr 取真实 IP
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (isBlankOrUnknown(ip)) {
            ip = request.getHeader("X-Forwarded-For");
            if (isBlankOrUnknown(ip)) {
                ip = request.getRemoteAddr();
            } else {
                int idx = ip.indexOf(',');
                ip = (idx > 0 ? ip.substring(0, idx) : ip).trim();
            }
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    private static boolean isBlankOrUnknown(String s) {
        return s == null || s.isBlank() || "unknown".equalsIgnoreCase(s);
    }
}
