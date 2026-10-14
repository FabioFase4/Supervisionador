package com.fabio.eagleeyes

import com.fabio.eagleeyes.vinculo.VinculoPolicy
import org.junit.Assert.*
import org.junit.Test

class VinculoPolicyTest {
    @Test fun trimsWithoutChangingUidCase() {
        assertEquals("AbC123xyZ", VinculoPolicy.normalizarId("  AbC123xyZ\n"))
    }

    @Test fun rejectsEmptyIdsEmailsAndFirebasePaths() {
        listOf("", "  ", "pai@example.com", "usuarios/pais/abc", "a#b", "a\$b", "a[b]", "ab cd").forEach { id ->
            assertThrows(IllegalArgumentException::class.java) { VinculoPolicy.normalizarId(id) }
        }
    }

    @Test fun sameLinkIsIdempotentButAnotherParentCannotOverwriteIt() {
        assertTrue(VinculoPolicy.podeVincular(null, "pai1"))
        assertTrue(VinculoPolicy.podeVincular("", "pai1"))
        assertTrue(VinculoPolicy.podeVincular("pai1", "pai1"))
        assertFalse(VinculoPolicy.podeVincular("pai1", "pai2"))
    }
}

