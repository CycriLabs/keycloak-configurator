package com.cycrilabs.keycloak.configurator.shared.control;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class StringUtil {
    public static boolean isBlank(final String str) {
        return !isNotBlank(str);
    }

    public static boolean isNotBlank(final String str) {
        return str != null && !"".equals(str.trim());
    }
}
