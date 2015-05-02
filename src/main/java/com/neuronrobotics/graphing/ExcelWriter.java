package com.neuronrobotics.graphing;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.jfree.data.xy.XYDataItem;

public class ExcelWriter implements DataWriter {

	private WorkbookSettings wbSettings = new WorkbookSettings();
	private WritableWorkbook workbook;
	private WritableSheet excelSheet;
	private int lineOffset = 0;
	public ExcelWriter() {
		wbSettings.setLocale(new Locale("en", "EN"));
	}
	
	private void addNumber(int column, int row, double d) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, d);
		excelSheet.addCell(number);
	}
	
	private void addLabel(int column, int row, String s) throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s);
		excelSheet.addCell(label);
	}
	
	
	public void setFile(File f) {
		try {
			workbook = Workbook.createWorkbook(f, wbSettings);
			workbook.createSheet("Data", 0);
			excelSheet = workbook.getSheet(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addData(DataChannel c) {
		try {
			int col = 1;
			addLabel(lineOffset, 0, c.toString() + " Time (ms)");
			addLabel(lineOffset+1, 0, c.toString() + " Value");
			
			for(Object o : c.getSeries().getItems()) {
				XYDataItem i = (XYDataItem) o;	
				addNumber(lineOffset, col, i.getXValue());
				addNumber(lineOffset+1, col, i.getYValue());
				col++;
			}
		} catch (RowsExceededException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (WriteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lineOffset+=2;
	}

	
	public void cleanup() {
		try {
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

