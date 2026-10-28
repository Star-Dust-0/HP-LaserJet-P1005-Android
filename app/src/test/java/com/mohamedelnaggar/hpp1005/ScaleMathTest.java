package com.mohamedelnaggar.hpp1005;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Regression: scale ratios must use float division (integer division gave 0 and a blank preview). */
public class ScaleMathTest {
    @Test
    public void fitScaleIsFractional() {
        float s = SheetComposer.scaleFor(SheetComposer.ScaleMode.FIT, 100.0f, 1.0f, 2400, 3400, 1800, 2540);
        assertEquals(2540f / 3400f, s, 0.0001f);
    }

    @Test
    public void paperSizesAreDistinct() {
        assertEquals(215.9f, SheetComposer.paperMm("LETTER")[0], 0.01f);
        assertEquals(355.6f, SheetComposer.paperMm("LEGAL")[1], 0.01f);
        assertEquals(210.0f, SheetComposer.paperMm("A4")[0], 0.01f);
    }
}
