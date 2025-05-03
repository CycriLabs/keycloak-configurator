package com.cycrilabs.keycloak.configurator.commands.secrets.control;

@FunctionalInterface
public interface SecretFileContentWriter {
    void provideContent(final String derivedFileName, final String fileContent);
}
