package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;

import com.cycrilabs.keycloak.configurator.commands.diff.boundary.AbstractDiffer;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.DiffCommandConfiguration;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.ServerUnreachableException;

import io.quarkus.logging.Log;

/**
 * Runs all differs and renders the resulting report.
 */
@ApplicationScoped
public class DiffRunner {
    private final DiffCommandConfiguration configuration;
    private final Instance<AbstractDiffer<?>> differs;
    private final DiffReporter reporter;
    private final Keycloak keycloak;

    @Inject
    public DiffRunner(
            final DiffCommandConfiguration configuration,
            final Instance<AbstractDiffer<?>> differs,
            final DiffReporter reporter,
            final Keycloak keycloak
    ) {
        this.configuration = configuration;
        this.differs = differs;
        this.reporter = reporter;
        this.keycloak = keycloak;
    }

    /**
     * Compares the server against the local configuration. The server is only read, never
     * modified.
     *
     * @return the number of detected differences
     * @throws ServerUnreachableException
     *         if the server cannot be reached or the credentials are rejected
     */
    public int run() {
        Log.infof("Comparing server %s against configuration %s. The server is not modified.",
                configuration.getServer(), configuration.getConfigDirectory());

        verifyServerIsReachable();

        // Differs run in the order the entities are imported, so that findings are reported from
        // the realm down to its details.
        final List<AbstractDiffer<?>> sortedDiffers = differs.stream()
                .sorted(Comparator.comparingInt(AbstractDiffer::getPriority))
                .toList();
        for (final AbstractDiffer<?> differ : sortedDiffers) {
            differ.runDiff();
        }

        reporter.render();
        return reporter.getDifferenceCount();
    }

    /**
     * Fails before anything is compared if the server cannot be reached or the credentials are
     * rejected. Without this check every entity would look as if it did not exist on the server,
     * which is indistinguishable from a configuration that was never applied.
     */
    private void verifyServerIsReachable() {
        try {
            keycloak.tokenManager().getAccessToken();
        } catch (final RuntimeException e) {
            throw new ServerUnreachableException(
                    "Could not authenticate against server '%s' as user '%s': %s".formatted(
                            configuration.getServer(), configuration.getUsername(),
                            e.getMessage()), e);
        }
    }
}
