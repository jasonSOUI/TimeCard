package com.mg;

import com.mg.enums.TimeCardStatus;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

/**
 * Unit test for App class.
 */
@DisplayName("App 應用程式進入點測試")
class AppTest {

    /**
     * A helper method to make the private static method `getCheckinStatus` accessible for testing.
     */
    private Pair<TimeCardStatus, Boolean> invokeGetCheckinStatus(String[] args) throws Exception {
        Method method = App.class.getDeclaredMethod("getCheckinStatus", String[].class);
        method.setAccessible(true);
        // As it's a static method, the first argument to invoke is null.
        return (Pair<TimeCardStatus, Boolean>) method.invoke(null, (Object) args);
    }

    @Test
    @DisplayName("測試上班打卡參數 (-s on)")
    void testGetCheckinStatus_On() throws Exception {
        String[] args = {"-s", "on"};
        Pair<TimeCardStatus, Boolean> result = invokeGetCheckinStatus(args);
        assertNotNull(result, "結果不應為 null");
        assertEquals(TimeCardStatus.ON, result.getLeft(), "打卡狀態應為 ON");
        assertFalse(result.getRight(), "除錯模式應為 false");
    }

    @Test
    @DisplayName("測試下班打卡參數 (-s off)")
    void testGetCheckinStatus_Off() throws Exception {
        String[] args = {"-s", "off"};
        Pair<TimeCardStatus, Boolean> result = invokeGetCheckinStatus(args);
        assertNotNull(result, "結果不應為 null");
        assertEquals(TimeCardStatus.OFF, result.getLeft(), "打卡狀態應為 OFF");
        assertFalse(result.getRight(), "除錯模式應為 false");
    }

    @Test
    @DisplayName("測試上班打卡並啟用除錯模式 (-s on -d)")
    void testGetCheckinStatus_OnWithDebug() throws Exception {
        String[] args = {"-s", "on", "-d"};
        Pair<TimeCardStatus, Boolean> result = invokeGetCheckinStatus(args);
        assertNotNull(result, "結果不應為 null");
        assertEquals(TimeCardStatus.ON, result.getLeft(), "打卡狀態應為 ON");
        assertTrue(result.getRight(), "除錯模式應為 true");
    }

    @Test
    @DisplayName("測試下班打卡並啟用除錯模式 (-s off -d)")
    void testGetCheckinStatus_OffWithDebug() throws Exception {
        String[] args = {"-s", "off", "-d"};
        Pair<TimeCardStatus, Boolean> result = invokeGetCheckinStatus(args);
        assertNotNull(result, "結果不應為 null");
        assertEquals(TimeCardStatus.OFF, result.getLeft(), "打卡狀態應為 OFF");
        assertTrue(result.getRight(), "除錯模式應為 true");
    }
}