package com.samuilolegovich.dto;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для HistoryPaymentDto — объект передачи данных об одной транзакции.
 * Гарантирует, что адрес, сумма и tag правильно сохраняются и читаются.
 */
public class HistoryPaymentDtoTest {

    private static final String ADDRESS = "rGrEJZaBFYhPGuyM7NiJbJw2yXVB9vJHah";
    private static final String AMOUNT  = "XRP 1.000000";
    private static final String TAG     = "212";

    /** Конструктор устанавливает все три поля */
    @Test
    public void constructor_setsAllFields() {
        HistoryPaymentDto dto = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        assertEquals(ADDRESS, dto.getAddress());
        assertEquals(AMOUNT, dto.getAmount());
        assertEquals(TAG, dto.getTag());
    }

    /** Сеттер адреса обновляет значение */
    @Test
    public void setAddress_updatesValue() {
        HistoryPaymentDto dto = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        dto.setAddress("rNewAddress123");
        assertEquals("rNewAddress123", dto.getAddress());
    }

    /** Сеттер суммы обновляет значение */
    @Test
    public void setAmount_updatesValue() {
        HistoryPaymentDto dto = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        dto.setAmount("XRP 5.500000");
        assertEquals("XRP 5.500000", dto.getAmount());
    }

    /** Сеттер tag обновляет значение */
    @Test
    public void setTag_updatesValue() {
        HistoryPaymentDto dto = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        dto.setTag("213");
        assertEquals("213", dto.getTag());
    }

    /** Поля принимают null без исключения */
    @Test
    public void constructor_acceptsNullFields() {
        HistoryPaymentDto dto = new HistoryPaymentDto(null, null, null);
        assertNull(dto.getAddress());
        assertNull(dto.getAmount());
        assertNull(dto.getTag());
    }

    /** Два объекта с одинаковыми данными — разные экземпляры */
    @Test
    public void twoInstancesWithSameData_areNotSameObject() {
        HistoryPaymentDto dto1 = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        HistoryPaymentDto dto2 = new HistoryPaymentDto(ADDRESS, AMOUNT, TAG);
        assertNotSame(dto1, dto2);
        assertEquals(dto1.getAddress(), dto2.getAddress());
    }
}