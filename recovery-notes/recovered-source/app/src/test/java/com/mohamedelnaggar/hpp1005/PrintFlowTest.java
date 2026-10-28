package com.mohamedelnaggar.hpp1005;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class PrintFlowTest {
    private List<Integer> pages(int n) {
        ArrayList<Integer> result = new ArrayList<>();
        for(int i=0;i<n;i++) result.add(i);
        return result;
    }
    @Test public void singlePageCompletesWithoutReinsertForBothBindings() {
        for(var binding:SheetComposer.Binding.values()) {
            var plan=PrintFlow.booklet(pages(1),binding);
            assertEquals(1,PrintFlow.select(plan,"booklet-front").size());
            assertTrue(PrintFlow.select(plan,"booklet-back").isEmpty());
            assertEquals(PrintFlow.Next.COMPLETE,PrintFlow.after("booklet-front",false,1,1,false,false));
        }
    }
    @Test public void rangeMapsToOriginalPagesWithoutDroppingContent() {
        for(var binding:SheetComposer.Binding.values()) {
            var plan=PrintFlow.booklet(Arrays.asList(7,2,4,0,9),binding);
            ArrayList<Integer> seen=new ArrayList<>();
            for(var sheet:plan)for(int page:sheet.pages)if(page>=0)seen.add(page);
            Collections.sort(seen);
            assertEquals(Arrays.asList(0,2,4,7,9),seen);
        }
    }
    @Test public void bookletCountMatrixNoEmptyScheduledSidesOrLostPages() {
        for(int n=1;n<=256;n++)for(var binding:SheetComposer.Binding.values()) {
            var plan=PrintFlow.booklet(pages(n),binding);
            int[] seen=new int[n];
            for(var sheet:plan) {
                assertTrue(sheet.pages[0]>=0 || sheet.pages[1]>=0);
                for(int page:sheet.pages)if(page>=0)seen[page]++;
            }
            for(int count:seen)assertEquals(1,count);
            boolean hasBack=!PrintFlow.select(plan,"booklet-back").isEmpty();
            assertEquals(n>1,hasBack);
            assertEquals(hasBack?PrintFlow.Next.REINSERT_BOOKLET:PrintFlow.Next.COMPLETE,
                PrintFlow.after("booklet-front",hasBack,1,1,false,false));
            var back=PrintFlow.select(plan,"booklet-back");
            int last=plan.size();
            for(var sheet:back) {assertTrue(sheet.bookletBack); assertTrue(plan.indexOf(sheet)<last);last=plan.indexOf(sheet);}
        }
    }
    @Test public void copiesFinishFrontOnlyOrFrontThenBackInOrder() {
        for(int copies:new int[]{1,2,3,10})for(boolean hasBack:new boolean[]{false,true}) {
            for(int copy=1;copy<=copies;copy++) {
                var afterFront=PrintFlow.after("booklet-front",hasBack,copy,copies,false,false);
                if(hasBack)assertEquals(PrintFlow.Next.REINSERT_BOOKLET,afterFront);
                var end=hasBack?PrintFlow.after("booklet-back",true,copy,copies,false,false):afterFront;
                assertEquals(copy<copies?PrintFlow.Next.NEXT_COPY:PrintFlow.Next.COMPLETE,end);
            }
        }
    }
    @Test public void manualDuplexSplitUsesComposedSheetsAndReversesBacks() {
        for(int count=1;count<=32;count++) {
            var plan=new ArrayList<PrintFlow.Sheet>();
            for(int i=0;i<count;i++){var s=new PrintFlow.Sheet();s.pages=new int[]{i};plan.add(s);}
            var all=PrintFlow.select(plan,"all");assertEquals(plan,all);
            var fronts=PrintFlow.select(plan,"odd");var backs=PrintFlow.select(plan,"even");
            assertEquals((count+1)/2,fronts.size());assertEquals(count/2,backs.size());
            for(int i=0;i<fronts.size();i++)assertSame(plan.get(i*2),fronts.get(i));
            for(int i=0;i<backs.size();i++)assertSame(plan.get(((count/2)-1-i)*2+1),backs.get(i));
        }
    }
    @Test public void cancellationAndFailuresNeverPromptForAnotherSideOrCopy() {
        for(String pass:new String[]{"all","odd","even","booklet-front","booklet-back"})
            for(boolean backs:new boolean[]{true,false}) {
                assertEquals(PrintFlow.Next.STOP,PrintFlow.after(pass,backs,1,3,true,false));
                assertEquals(PrintFlow.Next.STOP,PrintFlow.after(pass,backs,1,3,false,true));
            }
    }
    @Test public void retryHasNoPreviousFailureOrCancelState() {
        assertEquals(PrintFlow.Next.STOP,PrintFlow.after("booklet-front",true,1,1,true,false));
        assertEquals(PrintFlow.Next.REINSERT_BOOKLET,PrintFlow.after("booklet-front",true,1,1,false,false));
    }
}
