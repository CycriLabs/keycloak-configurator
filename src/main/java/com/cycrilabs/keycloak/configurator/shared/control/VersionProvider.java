package com.cycrilabs.keycloak.configurator.shared.control;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        final ConfigValue appVersion = ConfigProvider.getConfig()
                .getConfigValue("quarkus.application.version");
        return new String[] { appVersion.getValue() };
    }
}
