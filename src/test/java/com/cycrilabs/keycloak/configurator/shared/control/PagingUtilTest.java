package com.cycrilabs.keycloak.configurator.shared.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class PagingUtilTest {
    @Test
    void testListAllLoadsEveryPage() {
        final List<Integer> all = IntStream.range(0, 523).boxed().toList();

        final List<Integer> loaded = PagingUtil.listAll(
                (first, max) -> all.subList(first, Math.min(first + max, all.size())));

        assertEquals(all, loaded);
    }

    @Test
    void testListAllWithExactMultipleOfPageSize() {
        final List<Integer> all = IntStream.range(0, 2 * PagingUtil.PAGE_SIZE).boxed().toList();

        final List<Integer> loaded = PagingUtil.listAll(
                (first, max) -> all.subList(first, Math.min(first + max, all.size())));

        assertEquals(all, loaded);
    }
}
