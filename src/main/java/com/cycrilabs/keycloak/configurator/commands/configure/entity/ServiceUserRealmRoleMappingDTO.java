package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import io.quarkus.runtime.annotations.RegisterForReflection;

@Getter
@Setter
@RegisterForReflection
public class ServiceUserRealmRoleMappingDTO {
    private List<String> roles;
}
