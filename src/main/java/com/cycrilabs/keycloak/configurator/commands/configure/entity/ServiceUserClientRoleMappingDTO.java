package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceUserClientRoleMappingDTO {
    private String client;
    private List<String> roles;
}
