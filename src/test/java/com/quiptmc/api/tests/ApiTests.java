package com.quiptmc.api.tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ApiTests {

    @Test
    public void testAlwaysPass() {
        assertTrue(true);
        assertFalse(false);
        assertNull(null);
        assertNotNull(new Object());
        assertEquals(1, 1);

    }
}
