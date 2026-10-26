package com.cycrilabs.keycloak.configurator.shared.control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PagingUtil {
    /**
     * Matches the server-side default page size of Keycloak list endpoints, so a full page is never
     * mistaken for the last one.
     */
    public static final int PAGE_SIZE = 100;

    /**
     * Loads all entities of a paged Keycloak endpoint.
     *
     * @param pageLoader receives the index of the first result and the page size
     */
    public static <T> List<T> listAll(final BiFunction<Integer, Integer, List<T>> pageLoader) {
        final List<T> result = new ArrayList<>();
        List<T> page;
        do {
            page = pageLoader.apply(result.size(), PAGE_SIZE);
            result.addAll(page);
        } while (page.size() == PAGE_SIZE);
        return result;
    }
}
