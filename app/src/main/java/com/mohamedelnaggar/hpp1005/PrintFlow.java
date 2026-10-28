package com.mohamedelnaggar.hpp1005;

import com.mohamedelnaggar.hpp1005.SheetComposer;
import java.util.ArrayList;
import java.util.List;

/* loaded from: classes.dex */
final class PrintFlow {

    enum Next {
        STOP,
        REINSERT_DUPLEX,
        REINSERT_BOOKLET,
        NEXT_COPY,
        COMPLETE
    }

    static class Sheet {
        boolean bookletBack;
        int[] pages;
        int posterCols;
        int posterRows;
        int posterTile = -1;
        String label = "";

        Sheet() {
        }
    }

    static <T extends Sheet> ArrayList<T> select(List<T> list, String str) {
        ArrayList<T> arrayList = new ArrayList<>();
        if ("even".equals(str) || "booklet-back".equals(str)) {
            for (int size = list.size() - 1; size >= 0; size--) {
                if ("even".equals(str) ? (size & 1) == 1 : list.get(size).bookletBack) {
                    arrayList.add(list.get(size));
                }
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                T t = list.get(i);
                if ("odd".equals(str) ? (i & 1) == 0 : (!"booklet-front".equals(str) || !t.bookletBack)) {
                    arrayList.add(t);
                }
            }
        }
        return arrayList;
    }

    static Next after(String str, boolean z, int i, int i2, boolean z2, boolean z3) {
        if (z2 || z3) {
            return Next.STOP;
        }
        if ("odd".equals(str)) {
            return Next.REINSERT_DUPLEX;
        }
        if ("booklet-front".equals(str) && z) {
            return Next.REINSERT_BOOKLET;
        }
        if (("booklet-front".equals(str) || "booklet-back".equals(str)) && i < i2) {
            return Next.NEXT_COPY;
        }
        return Next.COMPLETE;
    }

    static ArrayList<Sheet> booklet(List<Integer> list, SheetComposer.Binding binding) {
        int[][] bookletOrder = SheetComposer.bookletOrder(list.size(), binding);
        if (!SheetComposer.verifyFold(bookletOrder, list.size(), binding)) {
            throw new IllegalStateException("Booklet order validation failed. The job stopped before sending any data.");
        }
        ArrayList<Sheet> arrayList = new ArrayList<>();
        for (int i = 0; i < bookletOrder.length; i++) {
            int i2 = bookletOrder[i][0];
            int intValue = i2 < 0 ? -1 : list.get(i2).intValue();
            int i3 = bookletOrder[i][1];
            int intValue2 = i3 >= 0 ? list.get(i3).intValue() : -1;
            if (intValue >= 0 || intValue2 >= 0) {
                Sheet sheet = new Sheet();
                sheet.pages = new int[]{intValue, intValue2};
                sheet.bookletBack = (i & 1) == 1;
                StringBuilder sb = new StringBuilder("Booklet · ");
                sb.append(sheet.bookletBack ? "Back side" : "Front side");
                sb.append(" · sheet ");
                sb.append((i / 2) + 1);
                sheet.label = sb.toString();
                arrayList.add(sheet);
            }
        }
        return arrayList;
    }

    private PrintFlow() {
    }
}
