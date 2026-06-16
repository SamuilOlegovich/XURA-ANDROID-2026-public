package com.samuilolegovich.enums;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Тесты для RouletteBetCode — преобразование тегов ставок рулетки в компактные
 * memo-коды и обратно. Ошибка здесь приводит либо к неверно прочитанной ставке
 * на сервере, либо к неверно начисленному выигрышу (множитель).
 */
public class RouletteBetCodeTest {

    // -------------------------------------------------------------------------
    // tagToCode — полный тег -> компактный код
    // -------------------------------------------------------------------------

    @Test
    public void tagToCode_simpleBets_mapToExpectedCodes() {
        assertEquals("r", RouletteBetCode.tagToCode("RED"));
        assertEquals("b", RouletteBetCode.tagToCode("BLACK"));
        assertEquals("o", RouletteBetCode.tagToCode("ODD"));
        assertEquals("e", RouletteBetCode.tagToCode("EVEN"));
        assertEquals("l", RouletteBetCode.tagToCode("LOW"));
        assertEquals("h", RouletteBetCode.tagToCode("HIGH"));
        assertEquals("d1", RouletteBetCode.tagToCode("D1"));
        assertEquals("d2", RouletteBetCode.tagToCode("D2"));
        assertEquals("d3", RouletteBetCode.tagToCode("D3"));
        assertEquals("c1", RouletteBetCode.tagToCode("C1"));
        assertEquals("c2", RouletteBetCode.tagToCode("C2"));
        assertEquals("c3", RouletteBetCode.tagToCode("C3"));
    }

    @Test
    public void tagToCode_numberBets_prependN() {
        assertEquals("n0", RouletteBetCode.tagToCode("N:0"));
        assertEquals("n5", RouletteBetCode.tagToCode("N:5"));
        assertEquals("n36", RouletteBetCode.tagToCode("N:36"));
    }

    @Test
    public void tagToCode_unknownTag_returnsLowerCase() {
        assertEquals("foo", RouletteBetCode.tagToCode("FOO"));
    }

    @Test
    public void tagToCode_null_returnsEmptyString() {
        assertEquals("", RouletteBetCode.tagToCode(null));
    }

    // -------------------------------------------------------------------------
    // codeToTag — компактный код -> полный тег (обратное преобразование)
    // -------------------------------------------------------------------------

    @Test
    public void codeToTag_simpleCodes_mapBackToFullTags() {
        assertEquals("RED", RouletteBetCode.codeToTag("r"));
        assertEquals("BLACK", RouletteBetCode.codeToTag("b"));
        assertEquals("D1", RouletteBetCode.codeToTag("d1"));
        assertEquals("C3", RouletteBetCode.codeToTag("c3"));
    }

    @Test
    public void codeToTag_isCaseInsensitive() {
        assertEquals("RED", RouletteBetCode.codeToTag("R"));
        assertEquals("D1", RouletteBetCode.codeToTag("D1"));
    }

    @Test
    public void codeToTag_numberCodes_convertBackToNTag() {
        assertEquals("N:0", RouletteBetCode.codeToTag("n0"));
        assertEquals("N:5", RouletteBetCode.codeToTag("n5"));
        assertEquals("N:36", RouletteBetCode.codeToTag("n36"));
    }

    @Test
    public void codeToTag_numberOutOfRange_fallsBackToUpperCase() {
        // n37 не входит в диапазон 0..36 рулетки, поэтому это не валидный N-код
        assertEquals("N37", RouletteBetCode.codeToTag("n37"));
    }

    @Test
    public void codeToTag_unknownCode_returnsUpperCase() {
        assertEquals("ZZ", RouletteBetCode.codeToTag("zz"));
    }

    @Test
    public void codeToTag_null_returnsEmptyString() {
        assertEquals("", RouletteBetCode.codeToTag(null));
    }

    @Test
    public void tagToCode_and_codeToTag_areInverse_forAllConstants() {
        for (RouletteBetCode b : RouletteBetCode.values()) {
            assertEquals(b.code, RouletteBetCode.tagToCode(b.fullTag));
            assertEquals(b.fullTag, RouletteBetCode.codeToTag(b.code));
        }
    }

    // -------------------------------------------------------------------------
    // multiplierForTag — множитель выигрыша
    // -------------------------------------------------------------------------

    @Test
    public void multiplierForTag_evenChanceBets_equalTwo() {
        assertEquals(2, RouletteBetCode.multiplierForTag("RED"));
        assertEquals(2, RouletteBetCode.multiplierForTag("BLACK"));
        assertEquals(2, RouletteBetCode.multiplierForTag("ODD"));
        assertEquals(2, RouletteBetCode.multiplierForTag("EVEN"));
        assertEquals(2, RouletteBetCode.multiplierForTag("LOW"));
        assertEquals(2, RouletteBetCode.multiplierForTag("HIGH"));
    }

    @Test
    public void multiplierForTag_dozenAndColumnBets_equalThree() {
        assertEquals(3, RouletteBetCode.multiplierForTag("D1"));
        assertEquals(3, RouletteBetCode.multiplierForTag("D2"));
        assertEquals(3, RouletteBetCode.multiplierForTag("D3"));
        assertEquals(3, RouletteBetCode.multiplierForTag("C1"));
        assertEquals(3, RouletteBetCode.multiplierForTag("C2"));
        assertEquals(3, RouletteBetCode.multiplierForTag("C3"));
    }

    @Test
    public void multiplierForTag_numberBets_equalThirtySix() {
        assertEquals(36, RouletteBetCode.multiplierForTag("N:0"));
        assertEquals(36, RouletteBetCode.multiplierForTag("N:17"));
        assertEquals(36, RouletteBetCode.multiplierForTag("N:36"));
    }

    @Test
    public void multiplierForTag_unknownOrNullTag_defaultsToTwo() {
        assertEquals(2, RouletteBetCode.multiplierForTag("NOT_A_TAG"));
        assertEquals(2, RouletteBetCode.multiplierForTag(null));
    }
}
