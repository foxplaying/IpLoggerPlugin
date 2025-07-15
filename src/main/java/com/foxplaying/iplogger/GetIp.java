// GetIp.java
package com.foxplaying.iplogger;

public class GetIp {
    private static IpLoggerPlugin plugin = IpLoggerPlugin.getInstance();

    public static String[] fetchBoth() {
        String ipv4 = IpUtil.fetch("http://4.ipw.cn");
        String ipv6 = IpUtil.fetch("http://6.ipw.cn");
        return new String[] {
            ipv4 != null ? ipv4.trim() : null,
            ipv6 != null ? ipv6.trim() : null
        };
    }

    public static String fetchBestIp() {
        String[] res = fetchBoth();
        if (res[0] != null && !res[0].isEmpty()) {
            return res[0];
        } else if (res[1] != null && !res[1].isEmpty()) {
            return res[1];
        } else {
            return plugin.getLangMessage("unknown"); 
        }
    }
}
