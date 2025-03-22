package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import java.nio.file.Path;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ConfigurationFile {
    private Path file;
    private String realmName;
    private String clientId;
    private String serviceUsername;
}
