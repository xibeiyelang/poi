/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.Sheet;
import org.apache.poi.hssf.model.DrawingManager2;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ddf.EscherDgRecord;

/**
 * Tests HSSFSheet.  This test case is very incomplete at the moment.
 *
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Andrew C. Oliver (acoliver apache org)
 */
public final class TestHSSFSheet extends TestCase {

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }

    /**
     * Test the gridset field gets set as expected.
     */
    public void testBackupRecord() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();

        assertEquals(true, sheet.getGridsetRecord().getGridset());
        s.setGridsPrinted(true);
        assertEquals(false, sheet.getGridsetRecord().getGridset());
    }

    /**
     * Test vertically centered output.
     */
    public void testVerticallyCenter() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        VCenterRecord record = sheet.getPageSettings().getVCenter();

        assertEquals(false, record.getVCenter());
        s.setVerticallyCenter(true);
        assertEquals(true, record.getVCenter());

        // wb.write(new FileOutputStream("c:\\test.xls"));
    }

    /**
     * Test horizontally centered output.
     */
    public void testHorizontallyCenter() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        HCenterRecord record = sheet.getPageSettings().getHCenter();

        assertEquals(false, record.getHCenter());
        s.setHorizontallyCenter(true);
        assertEquals(true, record.getHCenter());
    }


    /**
     * Test WSBboolRecord fields get set in the user model.
     */
    public void testWSBool() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        Sheet sheet = s.getSheet();
        WSBoolRecord record =
                (WSBoolRecord) sheet.findFirstRecordBySid(WSBoolRecord.sid);

        // Check defaults
        assertEquals(true, record.getAlternateExpression());
        assertEquals(true, record.getAlternateFormula());
        assertEquals(false, record.getAutobreaks());
        assertEquals(false, record.getDialog());
        assertEquals(false, record.getDisplayGuts());
        assertEquals(true, record.getFitToPage());
        assertEquals(false, record.getRowSumsBelow());
        assertEquals(false, record.getRowSumsRight());

        // Alter
        s.setAlternativeExpression(false);
        s.setAlternativeFormula(false);
        s.setAutobreaks(true);
        s.setDialog(true);
        s.setDisplayGuts(true);
        s.setFitToPage(false);
        s.setRowSumsBelow(true);
        s.setRowSumsRight(true);

        // Check
        assertEquals(false, record.getAlternateExpression());
        assertEquals(false, record.getAlternateFormula());
        assertEquals(true, record.getAutobreaks());
        assertEquals(true, record.getDialog());
        assertEquals(true, record.getDisplayGuts());
        assertEquals(false, record.getFitToPage());
        assertEquals(true, record.getRowSumsBelow());
        assertEquals(true, record.getRowSumsRight());
        assertEquals(false, s.getAlternateExpression());
        assertEquals(false, s.getAlternateFormula());
        assertEquals(true, s.getAutobreaks());
        assertEquals(true, s.getDialog());
        assertEquals(true, s.getDisplayGuts());
        assertEquals(false, s.getFitToPage());
        assertEquals(true, s.getRowSumsBelow());
        assertEquals(true, s.getRowSumsRight());
    }

    public void testReadBooleans() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test boolean");
        HSSFRow row = sheet.createRow(2);
        HSSFCell cell = row.createCell(9);
        cell.setCellValue(true);
        cell = row.createCell(11);
        cell.setCellValue(true);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        sheet = workbook.getSheetAt(0);
        row = sheet.getRow(2);
        assertNotNull(row);
        assertEquals(2, row.getPhysicalNumberOfCells());
    }

    public void testRemoveRow() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test boolean");
        HSSFRow row = sheet.createRow(2);
        sheet.removeRow(row);
    }

    public void testRemoveZeroRow() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);
        try {
            sheet.removeRow(row);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Invalid row number (-1) outside allowable range (0..65535)")) {
                throw new AssertionFailedError("Identified bug 45367");
            }
            throw e;
        }
    }

    public void testCloneSheet() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Clone");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        HSSFCell cell2 = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("clone_test"));
        cell2.setCellFormula("sin(1)");

        HSSFSheet clonedSheet = workbook.cloneSheet(0);
        HSSFRow clonedRow = clonedSheet.getRow(0);

        //Check for a good clone
        assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");

        //Check that the cells are not somehow linked
        cell.setCellValue(new HSSFRichTextString("Difference Check"));
        cell2.setCellFormula("cos(2)");
        if ("Difference Check".equals(clonedRow.getCell(0).getRichStringCellValue().getString())) {
            fail("string cell not properly cloned");
        }
        if ("COS(2)".equals(clonedRow.getCell(1).getCellFormula())) {
            fail("formula cell not properly cloned");
        }
        assertEquals(clonedRow.getCell(0).getRichStringCellValue().getString(), "clone_test");
        assertEquals(clonedRow.getCell(1).getCellFormula(), "SIN(1)");
    }

    /** tests that the sheet name for multiple clones of the same sheet is unique
     * BUG 37416
     */
    public void testCloneSheetMultipleTimes() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Test Clone");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString("clone_test"));
        //Clone the sheet multiple times
        workbook.cloneSheet(0);
        workbook.cloneSheet(0);

        assertNotNull(workbook.getSheet("Test Clone"));
        assertNotNull(workbook.getSheet("Test Clone (2)"));
        assertEquals("Test Clone (3)", workbook.getSheetName(2));
        assertNotNull(workbook.getSheet("Test Clone (3)"));

        workbook.removeSheetAt(0);
        workbook.removeSheetAt(0);
        workbook.removeSheetAt(0);
        workbook.createSheet("abc ( 123)");
        workbook.cloneSheet(0);
        assertEquals("abc (124)", workbook.getSheetName(1));
    }

    /**
     * Setting landscape and portrait stuff on new sheets
     */
    public void testPrintSetupLandscapeNew() throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheetL = workbook.createSheet("LandscapeS");
        HSSFSheet sheetP = workbook.createSheet("LandscapeP");

        // Check two aspects of the print setup
        assertFalse(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(0, sheetL.getPrintSetup().getCopies());
        assertEquals(0, sheetP.getPrintSetup().getCopies());

        // Change one on each
        sheetL.getPrintSetup().setLandscape(true);
        sheetP.getPrintSetup().setCopies((short)3);

        // Check taken
        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(0, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetP.getPrintSetup().getCopies());

        // Save and re-load, and check still there
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook = new HSSFWorkbook(new ByteArrayInputStream(baos.toByteArray()));

        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetP.getPrintSetup().getLandscape());
        assertEquals(0, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetP.getPrintSetup().getCopies());
    }

    /**
     * Setting landscape and portrait stuff on existing sheets
     */
    public void testPrintSetupLandscapeExisting() {
        HSSFWorkbook workbook = openSample("SimpleWithPageBreaks.xls");

        assertEquals(3, workbook.getNumberOfSheets());

        HSSFSheet sheetL = workbook.getSheetAt(0);
        HSSFSheet sheetPM = workbook.getSheetAt(1);
        HSSFSheet sheetLS = workbook.getSheetAt(2);

        // Check two aspects of the print setup
        assertFalse(sheetL.getPrintSetup().getLandscape());
        assertTrue(sheetPM.getPrintSetup().getLandscape());
        assertTrue(sheetLS.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(1, sheetPM.getPrintSetup().getCopies());
        assertEquals(1, sheetLS.getPrintSetup().getCopies());

        // Change one on each
        sheetL.getPrintSetup().setLandscape(true);
        sheetPM.getPrintSetup().setLandscape(false);
        sheetPM.getPrintSetup().setCopies((short)3);

        // Check taken
        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetPM.getPrintSetup().getLandscape());
        assertTrue(sheetLS.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetPM.getPrintSetup().getCopies());
        assertEquals(1, sheetLS.getPrintSetup().getCopies());

        // Save and re-load, and check still there
        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        assertTrue(sheetL.getPrintSetup().getLandscape());
        assertFalse(sheetPM.getPrintSetup().getLandscape());
        assertTrue(sheetLS.getPrintSetup().getLandscape());
        assertEquals(1, sheetL.getPrintSetup().getCopies());
        assertEquals(3, sheetPM.getPrintSetup().getCopies());
        assertEquals(1, sheetLS.getPrintSetup().getCopies());
    }

    public void testGroupRows() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet s = workbook.createSheet();
        HSSFRow r1 = s.createRow(0);
        HSSFRow r2 = s.createRow(1);
        HSSFRow r3 = s.createRow(2);
        HSSFRow r4 = s.createRow(3);
        HSSFRow r5 = s.createRow(4);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(0, r3.getOutlineLevel());
        assertEquals(0, r4.getOutlineLevel());
        assertEquals(0, r5.getOutlineLevel());

        s.groupRow(2,3);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(1, r3.getOutlineLevel());
        assertEquals(1, r4.getOutlineLevel());
        assertEquals(0, r5.getOutlineLevel());

        // Save and re-open
        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        s = workbook.getSheetAt(0);
        r1 = s.getRow(0);
        r2 = s.getRow(1);
        r3 = s.getRow(2);
        r4 = s.getRow(3);
        r5 = s.getRow(4);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(1, r3.getOutlineLevel());
        assertEquals(1, r4.getOutlineLevel());
        assertEquals(0, r5.getOutlineLevel());
    }

    public void testGroupRowsExisting() {
        HSSFWorkbook workbook = openSample("NoGutsRecords.xls");

        HSSFSheet s = workbook.getSheetAt(0);
        HSSFRow r1 = s.getRow(0);
        HSSFRow r2 = s.getRow(1);
        HSSFRow r3 = s.getRow(2);
        HSSFRow r4 = s.getRow(3);
        HSSFRow r5 = s.getRow(4);
        HSSFRow r6 = s.getRow(5);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(0, r3.getOutlineLevel());
        assertEquals(0, r4.getOutlineLevel());
        assertEquals(0, r5.getOutlineLevel());
        assertEquals(0, r6.getOutlineLevel());

        // This used to complain about lacking guts records
        s.groupRow(2, 4);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(1, r3.getOutlineLevel());
        assertEquals(1, r4.getOutlineLevel());
        assertEquals(1, r5.getOutlineLevel());
        assertEquals(0, r6.getOutlineLevel());

        // Save and re-open
        try {
            workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
        } catch (OutOfMemoryError e) {
            throw new AssertionFailedError("Identified bug 39903");
        }

        s = workbook.getSheetAt(0);
        r1 = s.getRow(0);
        r2 = s.getRow(1);
        r3 = s.getRow(2);
        r4 = s.getRow(3);
        r5 = s.getRow(4);
        r6 = s.getRow(5);

        assertEquals(0, r1.getOutlineLevel());
        assertEquals(0, r2.getOutlineLevel());
        assertEquals(1, r3.getOutlineLevel());
        assertEquals(1, r4.getOutlineLevel());
        assertEquals(1, r5.getOutlineLevel());
        assertEquals(0, r6.getOutlineLevel());
    }

    public void testGetDrawings() {
        HSSFWorkbook wb1c = openSample("WithChart.xls");
        HSSFWorkbook wb2c = openSample("WithTwoCharts.xls");

        // 1 chart sheet -> data on 1st, chart on 2nd
        assertNotNull(wb1c.getSheetAt(0).getDrawingPatriarch());
        assertNotNull(wb1c.getSheetAt(1).getDrawingPatriarch());
        assertFalse(wb1c.getSheetAt(0).getDrawingPatriarch().containsChart());
        assertTrue(wb1c.getSheetAt(1).getDrawingPatriarch().containsChart());

        // 2 chart sheet -> data on 1st, chart on 2nd+3rd
        assertNotNull(wb2c.getSheetAt(0).getDrawingPatriarch());
        assertNotNull(wb2c.getSheetAt(1).getDrawingPatriarch());
        assertNotNull(wb2c.getSheetAt(2).getDrawingPatriarch());
        assertFalse(wb2c.getSheetAt(0).getDrawingPatriarch().containsChart());
        assertTrue(wb2c.getSheetAt(1).getDrawingPatriarch().containsChart());
        assertTrue(wb2c.getSheetAt(2).getDrawingPatriarch().containsChart());
    }

    /**
     * Test that the ProtectRecord is included when creating or cloning a sheet
     */
    public void testProtect() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet hssfSheet = workbook.createSheet();
        Sheet sheet = hssfSheet.getSheet();
        ProtectRecord protect = sheet.getProtect();

        assertFalse(protect.getProtect());

        // This will tell us that cloneSheet, and by extension,
        // the list forms of createSheet leave us with an accessible
        // ProtectRecord.
        hssfSheet.setProtect(true);
        Sheet cloned = sheet.cloneSheet();
        assertNotNull(cloned.getProtect());
        assertTrue(hssfSheet.getProtect());
    }

    public void testProtectSheet() {
        short expected = (short)0xfef1;
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        s.protectSheet("abcdefghij");
        Sheet sheet = s.getSheet();
        ProtectRecord protect = sheet.getProtect();
        PasswordRecord pass = sheet.getPassword();
        assertTrue("protection should be on",protect.getProtect());
        assertTrue("object protection should be on",sheet.isProtected()[1]);
        assertTrue("scenario protection should be on",sheet.isProtected()[2]);
        assertEquals("well known value for top secret hash should be "+Integer.toHexString(expected).substring(4),expected,pass.getPassword());
    }


    public void testZoom() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        assertEquals(-1, sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid));
        sheet.setZoom(3,4);
        assertTrue(sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid) > 0);
        SCLRecord sclRecord = (SCLRecord) sheet.getSheet().findFirstRecordBySid(SCLRecord.sid);
        assertEquals(3, sclRecord.getNumerator());
        assertEquals(4, sclRecord.getDenominator());

        int sclLoc = sheet.getSheet().findFirstRecordLocBySid(SCLRecord.sid);
        int window2Loc = sheet.getSheet().findFirstRecordLocBySid(WindowTwoRecord.sid);
        assertTrue(sclLoc == window2Loc + 1);
    }


    /**
     * When removing one merged region, it would break
     *
     */
    public void testRemoveMerged() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        CellRangeAddress region = new CellRangeAddress(0, 1, 0, 1);
        sheet.addMergedRegion(region);
        region = new CellRangeAddress(1, 2, 0, 1);
        sheet.addMergedRegion(region);

        sheet.removeMergedRegion(0);

        region = sheet.getMergedRegion(0);
        assertEquals("Left over region should be starting at row 1", 1, region.getFirstRow());

        sheet.removeMergedRegion(0);

        assertEquals("there should be no merged regions left!", 0, sheet.getNumMergedRegions());

        //an, add, remove, get(0) would null pointer
        sheet.addMergedRegion(region);
        assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());
        sheet.removeMergedRegion(0);
        assertEquals("there should now be zero merged regions!", 0, sheet.getNumMergedRegions());
        //add it again!
        region.setLastRow(4);

        sheet.addMergedRegion(region);
        assertEquals("there should now be one merged region!", 1, sheet.getNumMergedRegions());

        //should exist now!
        assertTrue("there isn't more than one merged region in there", 1 <= sheet.getNumMergedRegions());
        region = sheet.getMergedRegion(0);
        assertEquals("the merged row to doesnt match the one we put in ", 4, region.getLastRow());
    }

    public void testShiftMerged() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString("first row, first cell"));

        row = sheet.createRow(1);
        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("second row, second cell"));

        CellRangeAddress region = new CellRangeAddress(1, 1, 0, 1);
        sheet.addMergedRegion(region);

        sheet.shiftRows(1, 1, 1);

        region = sheet.getMergedRegion(0);
        assertEquals("Merged region not moved over to row 2", 2, region.getFirstRow());
    }

    /**
     * Tests the display of gridlines, formulas, and rowcolheadings.
     * @author Shawn Laubach (slaubach at apache dot org)
     */
    public void testDisplayOptions() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        assertEquals(sheet.isDisplayGridlines(), true);
        assertEquals(sheet.isDisplayRowColHeadings(), true);
        assertEquals(sheet.isDisplayFormulas(), false);

        sheet.setDisplayGridlines(false);
        sheet.setDisplayRowColHeadings(false);
        sheet.setDisplayFormulas(true);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        assertEquals(sheet.isDisplayGridlines(), false);
        assertEquals(sheet.isDisplayRowColHeadings(), false);
        assertEquals(sheet.isDisplayFormulas(), true);
    }


    /**
     * Make sure the excel file loads work
     *
     */
    public void testPageBreakFiles() {
        HSSFWorkbook wb = openSample("SimpleWithPageBreaks.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        assertNotNull(sheet);

        assertEquals("1 row page break", 1, sheet.getRowBreaks().length);
        assertEquals("1 column page break", 1, sheet.getColumnBreaks().length);

        assertTrue("No row page break", sheet.isRowBroken(22));
        assertTrue("No column page break", sheet.isColumnBroken((short)4));

        sheet.setRowBreak(10);
        sheet.setColumnBreak((short)13);

        assertEquals("row breaks number", 2, sheet.getRowBreaks().length);
        assertEquals("column breaks number", 2, sheet.getColumnBreaks().length);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);

        assertTrue("No row page break", sheet.isRowBroken(22));
        assertTrue("No column page break", sheet.isColumnBroken((short)4));

        assertEquals("row breaks number", 2, sheet.getRowBreaks().length);
        assertEquals("column breaks number", 2, sheet.getColumnBreaks().length);
    }

    public void testDBCSName () {
        HSSFWorkbook wb = openSample("DBCSSheetName.xls");
        wb.getSheetAt(1);
        assertEquals ("DBCS Sheet Name 2", wb.getSheetName(1),"\u090f\u0915" );
        assertEquals("DBCS Sheet Name 1", wb.getSheetName(0),"\u091c\u093e");
    }

    /**
     * Testing newly added method that exposes the WINDOW2.toprow
     * parameter to allow setting the toprow in the visible view
     * of the sheet when it is first opened.
     */
    public void testTopRow() {
        HSSFWorkbook wb = openSample("SimpleWithPageBreaks.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        assertNotNull(sheet);

        short toprow = (short) 100;
        short leftcol = (short) 50;
        sheet.showInPane(toprow,leftcol);
        assertEquals("HSSFSheet.getTopRow()", toprow, sheet.getTopRow());
        assertEquals("HSSFSheet.getLeftCol()", leftcol, sheet.getLeftCol());
    }

    /** cell with formula becomes null on cloning a sheet*/
     public void test35084() {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet("Sheet1");
        HSSFRow r = s.createRow(0);
        r.createCell(0).setCellValue(1);
        r.createCell(1).setCellFormula("A1*2");
        HSSFSheet s1 = wb.cloneSheet(0);
        r = s1.getRow(0);
        assertEquals("double", r.getCell(0).getNumericCellValue(), 1, 0); // sanity check
        assertNotNull(r.getCell(1));
        assertEquals("formula", r.getCell(1).getCellFormula(), "A1*2");
    }

    /** test that new default column styles get applied */
    public void testDefaultColumnStyle() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCellStyle style = wb.createCellStyle();
        HSSFSheet s = wb.createSheet();
        s.setDefaultColumnStyle((short) 0, style);
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(0);
        assertEquals("style should match", style.getIndex(), c.getCellStyle().getIndex());
    }


    /**
     *
     */
    public void testAddEmptyRow() {
        //try to add 5 empty rows to a new sheet
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        for (int i = 0; i < 5; i++) {
            sheet.createRow(i);
        }

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);

        //try adding empty rows in an existing worksheet
        workbook = openSample("Simple.xls");

        sheet = workbook.getSheetAt(0);
        for (int i = 3; i < 10; i++) sheet.createRow(i);

        workbook = HSSFTestDataSamples.writeOutAndReadBack(workbook);
    }

    public void testAutoSizeColumn() {
        HSSFWorkbook wb = openSample("43902.xls");
        String sheetName = "my sheet";
        HSSFSheet sheet = wb.getSheet(sheetName);

        // Can't use literal numbers for column sizes, as
        //  will come out with different values on different
        //  machines based on the fonts available.
        // So, we use ranges, which are pretty large, but
        //  thankfully don't overlap!
        int minWithRow1And2 = 6400;
        int maxWithRow1And2 = 7800;
        int minWithRow1Only = 2750;
        int maxWithRow1Only = 3300;

        // autoSize the first column and check its size before the merged region (1,0,1,1) is set:
        // it has to be based on the 2nd row width
        sheet.autoSizeColumn((short)0);
        assertTrue("Column autosized with only one row: wrong width", sheet.getColumnWidth((short)0) >= minWithRow1And2);
        assertTrue("Column autosized with only one row: wrong width", sheet.getColumnWidth((short)0) <= maxWithRow1And2);

        //create a region over the 2nd row and auto size the first column
        sheet.addMergedRegion(new CellRangeAddress(1,1,0,1));
        sheet.autoSizeColumn((short)0);
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);

        // check that the autoSized column width has ignored the 2nd row
        // because it is included in a merged region (Excel like behavior)
        HSSFSheet sheet2 = wb2.getSheet(sheetName);
        assertTrue(sheet2.getColumnWidth((short)0) >= minWithRow1Only);
        assertTrue(sheet2.getColumnWidth((short)0) <= maxWithRow1Only);

        // remove the 2nd row merged region and check that the 2nd row value is used to the autoSizeColumn width
        sheet2.removeMergedRegion(1);
        sheet2.autoSizeColumn((short)0);
        HSSFWorkbook wb3 = HSSFTestDataSamples.writeOutAndReadBack(wb2);
        HSSFSheet sheet3 = wb3.getSheet(sheetName);
        assertTrue(sheet3.getColumnWidth((short)0) >= minWithRow1And2);
        assertTrue(sheet3.getColumnWidth((short)0) <= maxWithRow1And2);
    }

    /**
     * Setting ForceFormulaRecalculation on sheets
     */
    public void testForceRecalculation() throws Exception {
        HSSFWorkbook workbook = openSample("UncalcedRecord.xls");

        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFSheet sheet2 = workbook.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        row.createCell(0).setCellValue(5);
        row.createCell(1).setCellValue(8);
        assertFalse(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());

        // Save and manually verify that on column C we have 0, value in template
        File tempFile = new File(System.getProperty("java.io.tmpdir")+"/uncalced_err.xls" );
        tempFile.delete();
        FileOutputStream fout = new FileOutputStream( tempFile );
        workbook.write( fout );
        fout.close();
        sheet.setForceFormulaRecalculation(true);
        assertTrue(sheet.getForceFormulaRecalculation());

        // Save and manually verify that on column C we have now 13, calculated value
        tempFile = new File(System.getProperty("java.io.tmpdir")+"/uncalced_succ.xls" );
        tempFile.delete();
        fout = new FileOutputStream( tempFile );
        workbook.write( fout );
        fout.close();

        // Try it can be opened
        HSSFWorkbook wb2 = new HSSFWorkbook(new FileInputStream(tempFile));

        // And check correct sheet settings found
        sheet = wb2.getSheetAt(0);
        sheet2 = wb2.getSheetAt(1);
        assertTrue(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());

        // Now turn if back off again
        sheet.setForceFormulaRecalculation(false);

        fout = new FileOutputStream( tempFile );
        wb2.write( fout );
        fout.close();
        wb2 = new HSSFWorkbook(new FileInputStream(tempFile));

        assertFalse(wb2.getSheetAt(0).getForceFormulaRecalculation());
        assertFalse(wb2.getSheetAt(1).getForceFormulaRecalculation());
        assertFalse(wb2.getSheetAt(2).getForceFormulaRecalculation());

        // Now add a new sheet, and check things work
        //  with old ones unset, new one set
        HSSFSheet s4 = wb2.createSheet();
        s4.setForceFormulaRecalculation(true);

        assertFalse(sheet.getForceFormulaRecalculation());
        assertFalse(sheet2.getForceFormulaRecalculation());
        assertTrue(s4.getForceFormulaRecalculation());

        fout = new FileOutputStream( tempFile );
        wb2.write( fout );
        fout.close();

        HSSFWorkbook wb3 = new HSSFWorkbook(new FileInputStream(tempFile));
        assertFalse(wb3.getSheetAt(0).getForceFormulaRecalculation());
        assertFalse(wb3.getSheetAt(1).getForceFormulaRecalculation());
        assertFalse(wb3.getSheetAt(2).getForceFormulaRecalculation());
        assertTrue(wb3.getSheetAt(3).getForceFormulaRecalculation());
    }

    public void testColumnWidth() {
        //check we can correctly read column widths from a reference workbook
        HSSFWorkbook wb = openSample("colwidth.xls");

        //reference values
        int[] ref = {365, 548, 731, 914, 1097, 1280, 1462, 1645, 1828, 2011, 2194, 2377, 2560, 2742, 2925, 3108, 3291, 3474, 3657};

        HSSFSheet sh = wb.getSheetAt(0);
        for (char i = 'A'; i <= 'S'; i++) {
            int idx = i - 'A';
            int w = sh.getColumnWidth((short)idx);
            assertEquals(ref[idx], w);
        }

        //the second sheet doesn't have overridden column widths
        sh = wb.getSheetAt(1);
        int def_width = sh.getDefaultColumnWidth();
        for (char i = 'A'; i <= 'S'; i++) {
            int idx = i - 'A';
            int w = sh.getColumnWidth((short)idx);
            //getDefaultColumnWidth returns width measued in characters
            //getColumnWidth returns width measued in 1/256th units
            assertEquals(def_width*256, w);
        }

        //test new workbook
        wb = new HSSFWorkbook();
        sh = wb.createSheet();
        sh.setDefaultColumnWidth((short)10);
        assertEquals(10, sh.getDefaultColumnWidth());
        assertEquals(256*10, sh.getColumnWidth((short)0));
        assertEquals(256*10, sh.getColumnWidth((short)1));
        assertEquals(256*10, sh.getColumnWidth((short)2));
        for (char i = 'D'; i <= 'F'; i++) {
            short w = (short)(256*12);
            sh.setColumnWidth((short)i, w);
            assertEquals(w, sh.getColumnWidth((short)i));
        }

        //serialize and read again
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);

        sh = wb.getSheetAt(0);
        assertEquals(10, sh.getDefaultColumnWidth());
        //columns A-C have default width
        assertEquals(256*10, sh.getColumnWidth((short)0));
        assertEquals(256*10, sh.getColumnWidth((short)1));
        assertEquals(256*10, sh.getColumnWidth((short)2));
        //columns D-F have custom width
        for (char i = 'D'; i <= 'F'; i++) {
            short w = (short)(256*12);
            assertEquals(w, sh.getColumnWidth((short)i));
        }
    }

    /**
     * Some utilities write Excel files without the ROW records.
     * Excel, ooo, and google docs are OK with this.
     * Now POI is too.
     */
    public void testMissingRowRecords_bug41187() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex41187-19267.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);
        if(row == null) {
            throw new AssertionFailedError("Identified bug 41187 a");
        }
        if (row.getHeight() == 0) {
            throw new AssertionFailedError("Identified bug 41187 b");
        }
        assertEquals("Hi Excel!", row.getCell(0).getRichStringCellValue().getString());
        // check row height for 'default' flag
        assertEquals((short)0xFF, row.getHeight());

        HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    /**
     * If we clone a sheet containing drawings,
     * we must allocate a new ID of the drawing group and re-create shape IDs
     *
     * See bug #45720.
     */
    public void testCloneSheetWithDrawings() {
        HSSFWorkbook wb1 = HSSFTestDataSamples.openSampleWorkbook("45720.xls");

        HSSFSheet sheet1 = wb1.getSheetAt(0);

        wb1.getWorkbook().findDrawingGroup();
        DrawingManager2 dm1 = wb1.getWorkbook().getDrawingManager();

        wb1.cloneSheet(0);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb1);
        wb2.getWorkbook().findDrawingGroup();
        DrawingManager2 dm2 = wb2.getWorkbook().getDrawingManager();

        //check EscherDggRecord - a workbook-level registry of drawing objects
        assertEquals(dm1.getDgg().getMaxDrawingGroupId() + 1, dm2.getDgg().getMaxDrawingGroupId());

        HSSFSheet sheet2 = wb2.getSheetAt(1);

        //check that id of the drawing group was updated
        EscherDgRecord dg1 = (EscherDgRecord)sheet1.getDrawingEscherAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
        EscherDgRecord dg2 = (EscherDgRecord)sheet2.getDrawingEscherAggregate().findFirstWithId(EscherDgRecord.RECORD_ID);
        int dg_id_1 = dg1.getOptions() >> 4;
        int dg_id_2 = dg2.getOptions() >> 4;
        assertEquals(dg_id_1 + 1, dg_id_2);

        //TODO: check shapeId in the cloned sheet
    }
}
