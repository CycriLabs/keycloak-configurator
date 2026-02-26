package com.cycrilabs.keycloak.configurator.shared.entity;

import com.cycrilabs.keycloak.configurator.shared.control.StringUtil;

import lombok.Getter;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

@Getter
public class KeycloakConfiguration {
	private String server;
	private String username;
	private String password;
	private boolean dryRun;

	public KeycloakConfiguration() {
		// required to avoid "No default constructor for class" error
	}

	public KeycloakConfiguration(final ParseResult parseResult) {
		server = getMatchedOption(parseResult, "-s");
		username = getMatchedOption(parseResult, "-u");
		password = getMatchedOption(parseResult, "--password");
		if (StringUtil.isBlank(password)) {
			String passwordEnv = getMatchedOption(parseResult, "--password:env");
			password = System.getenv(passwordEnv);
		}
		if (StringUtil.isBlank(password)) {
			CommandLine.Model.CommandSpec spec = parseResult.subcommand().commandSpec();
			throw new CommandLine.ParameterException(spec.commandLine(), "Password required");
		}
		dryRun = this.<Boolean>getMatchedOption(parseResult, "--dry-run").booleanValue();
	}

	protected <T> T getMatchedOption(final ParseResult parseResult, final String name) {
		return parseResult.subcommand().commandSpec().optionsMap().get(name).getValue();
	}
}
