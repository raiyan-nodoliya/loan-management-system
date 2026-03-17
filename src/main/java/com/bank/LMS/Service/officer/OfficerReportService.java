package com.bank.LMS.Service.officer;

import com.bank.LMS.Entity.LoanApplication;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OfficerReportService {

    public void exportApplicationsToPdf(
            List<LoanApplication> applications,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String customerName,
            OutputStream outputStream
    ) throws Exception {

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        com.lowagie.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        Paragraph title = new Paragraph("Loan Applications Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Generated On: " + LocalDateTime.now(), normalFont));
        document.add(new Paragraph("From Date: " + valueOrAll(fromDate), normalFont));
        document.add(new Paragraph("To Date: " + valueOrAll(toDate), normalFont));
        document.add(new Paragraph("Status: " + valueOrAll(status), normalFont));
        document.add(new Paragraph("Customer Name: " + valueOrAll(customerName), normalFont));
        document.add(new Paragraph("Total Records: " + applications.size(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 3f, 2.5f, 2f, 2f, 2f});

        addPdfHeader(table, "App No", headerFont);
        addPdfHeader(table, "Customer", headerFont);
        addPdfHeader(table, "Loan Type", headerFont);
        addPdfHeader(table, "Amount", headerFont);
        addPdfHeader(table, "Status", headerFont);
        addPdfHeader(table, "Created Date", headerFont);

        for (LoanApplication a : applications) {
            table.addCell(getSafe(a.getApplicationNo()));
            table.addCell(a.getCustomer() != null ? getSafe(a.getCustomer().getName()) : "N/A");
            table.addCell(a.getLoanType() != null ? getSafe(a.getLoanType().getLoanTypeName()) : "N/A");
            table.addCell(formatAmount(a.getAmountRequested()));
            table.addCell(a.getStatus() != null ? getSafe(a.getStatus().getLabel()) : "N/A");
            table.addCell(a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "N/A");
        }

        document.add(table);
        document.close();
    }

    public void exportApplicationsToExcel(
            List<LoanApplication> applications,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String customerName,
            OutputStream outputStream
    ) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Applications Report");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Loan Applications Report");

        Row info1 = sheet.createRow(rowNum++);
        info1.createCell(0).setCellValue("Generated On");
        info1.createCell(1).setCellValue(LocalDateTime.now().toString());

        Row info2 = sheet.createRow(rowNum++);
        info2.createCell(0).setCellValue("From Date");
        info2.createCell(1).setCellValue(valueOrAll(fromDate));

        Row info3 = sheet.createRow(rowNum++);
        info3.createCell(0).setCellValue("To Date");
        info3.createCell(1).setCellValue(valueOrAll(toDate));

        Row info4 = sheet.createRow(rowNum++);
        info4.createCell(0).setCellValue("Status");
        info4.createCell(1).setCellValue(valueOrAll(status));

        Row info5 = sheet.createRow(rowNum++);
        info5.createCell(0).setCellValue("Customer Name");
        info5.createCell(1).setCellValue(valueOrAll(customerName));

        Row info6 = sheet.createRow(rowNum++);
        info6.createCell(0).setCellValue("Total Records");
        info6.createCell(1).setCellValue(applications.size());

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"App No", "Customer", "Loan Type", "Amount", "Status", "Created Date"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (LoanApplication a : applications) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(getSafe(a.getApplicationNo()));
            row.createCell(1).setCellValue(a.getCustomer() != null ? getSafe(a.getCustomer().getName()) : "N/A");
            row.createCell(2).setCellValue(a.getLoanType() != null ? getSafe(a.getLoanType().getLoanTypeName()) : "N/A");
            row.createCell(3).setCellValue(a.getAmountRequested() != null ? a.getAmountRequested().doubleValue() : 0.0);
            row.createCell(4).setCellValue(a.getStatus() != null ? getSafe(a.getStatus().getLabel()) : "N/A");
            row.createCell(5).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "N/A");
        }

        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    private void addPdfHeader(PdfPTable table, String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private String getSafe(String value) {
        return value != null ? value : "";
    }

    private String valueOrAll(Object value) {
        return value != null && !value.toString().isBlank() ? value.toString() : "All";
    }

    private String formatAmount(BigDecimal amount) {
        return amount != null ? "Rs. " + amount : "Rs. 0";
    }
}