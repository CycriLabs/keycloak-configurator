package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.Difference;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.DifferenceKind;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.FieldDifference;

import io.quarkus.logging.Log;

/**
 * Collects all detected differences and renders them as a report grouped by realm, entity type
 * and entity.
 */
@ApplicationScoped
public class DiffReporter {
    private static final String REALM_UNKNOWN = "<unknown realm>";

    private final List<Difference> differences = new ArrayList<>();
    private int checkedEntities;

    public void add(final Difference difference) {
        differences.add(difference);
    }

    /**
     * Records that an entity was compared against the server. Used for the report summary only.
     */
    public void entityChecked() {
        checkedEntities++;
    }

    /**
     * The number of detected differences. A value of zero means the server matches the local
     * configuration.
     *
     * @return the number of differences
     */
    public int getDifferenceCount() {
        return differences.size();
    }

    /**
     * Writes the report of all collected differences.
     */
    public void render() {
        if (differences.isEmpty()) {
            Log.infof("No differences found. %d entities are in sync with the local configuration.",
                    Integer.valueOf(checkedEntities));
            return;
        }

        final Map<String, List<Difference>> differencesByRealm = differences.stream()
                .collect(Collectors.groupingBy(DiffReporter::realmOf, TreeMap::new,
                        Collectors.toList()));
        differencesByRealm.forEach(DiffReporter::renderRealm);

        renderSummary(differencesByRealm.size());
    }

    private static void renderRealm(final String realm, final List<Difference> realmDifferences) {
        Log.infof("=== %s ===", realm);

        renderEntityDifferences(realmDifferences.stream()
                .filter(difference -> difference.kind() != DifferenceKind.EXTRA_ON_SERVER)
                .toList());

        realmDifferences.stream()
                .filter(difference -> difference.kind() == DifferenceKind.EXTRA_ON_SERVER)
                .sorted(entityOrder())
                .forEach(difference -> Log.infof("  EXTRA on server: %s '%s'",
                        difference.entityType().getName(), difference.entity()));
    }

    private static void renderEntityDifferences(final List<Difference> entityDifferences) {
        // Preserve a stable order: entity types as they are imported, entities alphabetically.
        final Map<String, List<Difference>> groupedByEntity = entityDifferences.stream()
                .sorted(entityOrder())
                .collect(Collectors.groupingBy(
                        difference -> difference.entityType().getName() + " '"
                                + difference.entity() + "'",
                        LinkedHashMap::new, Collectors.toList()));

        groupedByEntity.forEach((entity, differencesOfEntity) -> {
            Log.infof("  %s", entity);
            differencesOfEntity.forEach(DiffReporter::renderDifference);
        });
    }

    private static void renderDifference(final Difference difference) {
        if (difference.kind() == DifferenceKind.MISSING_ON_SERVER) {
            Log.info("    MISSING on server");
            return;
        }

        final FieldDifference field = difference.field();
        switch (field.kind()) {
            case MISSING_ON_SERVER -> Log.infof("    ~ %s: not set on server (local=%s)",
                    field.path(), field.localValue());
            case VALUE_DIFFERENT -> Log.infof("    ~ %s: local=%s server=%s", field.path(),
                    field.localValue(), field.serverValue());
            case ONLY_IN_LOCAL -> Log.infof("    ~ %s: only in local: %s", field.path(),
                    field.localValue());
            case ONLY_ON_SERVER -> Log.infof("    ~ %s: only on server: %s", field.path(),
                    field.serverValue());
        }
    }

    private void renderSummary(final int affectedRealms) {
        final Set<String> affectedEntities = differences.stream()
                .map(difference -> realmOf(difference) + "/" + difference.entityType().getName()
                        + "/" + difference.entity())
                .collect(Collectors.toSet());

        Log.infof("%d differences across %d entities in %d realm(s). %d entities checked.",
                Integer.valueOf(differences.size()), Integer.valueOf(affectedEntities.size()),
                Integer.valueOf(affectedRealms), Integer.valueOf(checkedEntities));
    }

    private static Comparator<Difference> entityOrder() {
        return Comparator.<Difference>comparingInt(
                        difference -> difference.entityType().getPriority())
                .thenComparing(Difference::entity,
                        Comparator.nullsFirst(Comparator.naturalOrder()));
    }

    private static String realmOf(final Difference difference) {
        return difference.realm() == null
               ? REALM_UNKNOWN
               : difference.realm();
    }
}
