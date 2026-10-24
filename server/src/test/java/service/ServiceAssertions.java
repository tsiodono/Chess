package service;

import org.junit.jupiter.api.function.Executable;
import static org.junit.jupiter.api.Assertions.*;

final class ServiceAssertions {
    private ServiceAssertions() { }

    static void assertStatus(int expected, Executable action) {
        ServiceException error = assertThrows(ServiceException.class, action);
        assertEquals(expected, error.statusCode());
        assertNotNull(error.getMessage());
    }
}
