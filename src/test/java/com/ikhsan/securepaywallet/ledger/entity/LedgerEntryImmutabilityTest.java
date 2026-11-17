package com.ikhsan.securepaywallet.ledger.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerEntryImmutabilityTest {

    @Test
    @DisplayName("I3: entri ledger tidak punya satu pun mutator")
    void ledgerEntryEntity_shouldNotExposeAnyMutator() {
        List<String> mutators = Arrays.stream(LedgerEntryEntity.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName)
                .filter(name -> name.startsWith("set"))
                .toList();

        assertTrue(mutators.isEmpty(),
                "LedgerEntryEntity tidak boleh punya mutator karena ledger bersifat append-only: " + mutators);
    }
}
