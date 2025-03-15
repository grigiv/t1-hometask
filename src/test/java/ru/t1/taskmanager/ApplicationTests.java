package ru.t1.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ApplicationTests {

    @Test
    void contextLoads() {
        // Проверяем, что приложение запускается с test-профилем
    }
}
