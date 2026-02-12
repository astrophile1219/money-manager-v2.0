package in.dipak.money_manager.service;

import in.dipak.money_manager.dto.ExpenseDTO;
import in.dipak.money_manager.dto.IncomeDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class ExcelService {

    public void writeExpenseExcel(OutputStream os, List<ExpenseDTO> expenses) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            createExpenseSheet(workbook, expenses, headerStyle, dateStyle, amountStyle);

            workbook.write(os);
        }
    }

    public void writeIncomeExcel(OutputStream os, List<IncomeDTO> incomes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            createIncomeSheet(workbook, incomes, headerStyle, dateStyle, amountStyle);
            workbook.write(os);
        }
    }

    /* ================== INCOME SHEET ================== */

    private void createIncomeSheet(Workbook workbook, List<IncomeDTO> incomes, CellStyle headerStyle, CellStyle dateStyle, CellStyle amountStyle) {
        Sheet sheet = workbook.createSheet("Incomes");
        createHeader(sheet, headerStyle);

        double total = 0;

        for (int i = 0; i < incomes.size(); i++) {
            IncomeDTO income = incomes.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(value(income.getName()));
            row.createCell(2).setCellValue(value(income.getCategoryName()));

            Cell amountCell = row.createCell(3);
            amountCell.setCellValue(income.getAmount().doubleValue());
            amountCell.setCellStyle(amountStyle);

            Cell dateCell = row.createCell(4);
            dateCell.setCellValue(income.getDate()); // ✅ already LocalDate
            dateCell.setCellStyle(dateStyle);


            total += income.getAmount().doubleValue();
        }

        createTotalRow(sheet, incomes.size() + 1, total, amountStyle);
        autoSizeColumns(sheet, 5);
    }

    /* ================== EXPENSE SHEET ================== */

    private void createExpenseSheet(Workbook workbook, List<ExpenseDTO> expenses, CellStyle headerStyle, CellStyle dateStyle, CellStyle amountStyle) {
        Sheet sheet = workbook.createSheet("Expenses");
        createHeader(sheet, headerStyle);

        double total = 0;

        for (int i = 0; i < expenses.size(); i++) {
            ExpenseDTO expense = expenses.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(value(expense.getName()));
            row.createCell(2).setCellValue(value(expense.getCategoryName()));

            Cell amountCell = row.createCell(3);
            amountCell.setCellValue(expense.getAmount().doubleValue());
            amountCell.setCellStyle(amountStyle);

            Cell dateCell = row.createCell(4);
            dateCell.setCellValue(expense.getDate());
            dateCell.setCellStyle(dateStyle);

            total += expense.getAmount().doubleValue();
        }

        createTotalRow(sheet, expenses.size() + 1, total, amountStyle);
        autoSizeColumns(sheet, 5);
    }

    /* ================== COMMON HELPERS ================== */

    private void createHeader(Sheet sheet, CellStyle headerStyle) {
        Row header = sheet.createRow(0);
        String[] columns = {"S.No", "Name", "Category", "Amount", "Date"};

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createTotalRow(Sheet sheet, int rowIndex, double total, CellStyle amountStyle) {
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.createCell(2).setCellValue("TOTAL");

        Cell totalCell = totalRow.createCell(3);
        totalCell.setCellValue(total);
        totalCell.setCellStyle(amountStyle);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String value(String val) {
        return val != null ? val : "N/A";
    }

    /* ================== STYLES ================== */

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("dd MMM yyyy"));
        return style;
    }

    private CellStyle createAmountStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }
}


//package in.dipak.money_manager.service;
//
//import in.dipak.money_manager.dto.ExpenseDTO;
//import in.dipak.money_manager.dto.IncomeDTO;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.List;
//import java.util.stream.IntStream;
//
//@Service
//public class ExcelService {
//    public void writeIncomesToExcel(OutputStream os, List<IncomeDTO> incomes) throws IOException {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Incomes");
//            Row header = sheet.createRow(0);
//            header.createCell(0).setCellValue("S.No");
//            header.createCell(1).setCellValue("Name");
//            header.createCell(2).setCellValue("Category");
//            header.createCell(3).setCellValue("Amount");
//            header.createCell(4).setCellValue("Date");
//            IntStream.range(0, incomes.size())
//                    .forEach(i -> {
//                        IncomeDTO income = incomes.get(i);
//                        Row row = sheet.createRow(i + 1);
//                        row.createCell(0).setCellValue(i + 1);
//                        row.createCell(1).setCellValue(income.getName() != null ? income.getName() : "N/A");
//                        row.createCell(2).setCellValue(income.getCategoryId() != null ? income.getCategoryName() : "N/A");
//                        row.createCell(3).setCellValue(income.getAmount() != null ? income.getAmount().doubleValue() : 0);
//                        row.createCell(4).setCellValue(income.getDate() != null ? income.getDate().toString() : "N/A");
//                    });
//            workbook.write(os);
//        }
//    }
//
//    public void writeExpensesToExcel(OutputStream os, List<ExpenseDTO> expenses) throws IOException {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("Incomes");
//            Row header = sheet.createRow(0);
//            header.createCell(0).setCellValue("S.No");
//            header.createCell(1).setCellValue("Name");
//            header.createCell(2).setCellValue("Category");
//            header.createCell(3).setCellValue("Amount");
//            header.createCell(4).setCellValue("Date");
//            IntStream.range(0, expenses.size())
//                    .forEach(i -> {
//                        ExpenseDTO expense = expenses.get(i);
//                        Row row = sheet.createRow(i + 1);
//                        row.createCell(0).setCellValue(i + 1);
//                        row.createCell(1).setCellValue(expense.getName() != null ? expense.getName() : "N/A");
//                        row.createCell(2).setCellValue(expense.getCategoryId() != null ? expense.getCategoryName() : "N/A");
//                        row.createCell(3).setCellValue(expense.getAmount() != null ? expense.getAmount().doubleValue() : 0);
//                        row.createCell(4).setCellValue(expense.getDate() != null ? expense.getDate().toString() : "N/A");
//                    });
//            workbook.write(os);
//        }
//    }
//}
