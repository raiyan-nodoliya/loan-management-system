package com.bank.LMS.Service.admin;

import com.bank.LMS.Entity.LoanApplication;
import com.bank.LMS.Repository.LoanApplicationRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdminReportService {

    private final LoanApplicationRepository loanApplicationRepository;

    public AdminReportService(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    public List<LoanApplication> getFilteredApplications(
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String customerName,
            String customerCity,
            String ageRange,
            String salaryRange,
            String amountRange,
            String loanTypeName,
            String gender
    ) {
        LocalDateTime fromDateTime = (fromDate != null) ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateTime = (toDate != null) ? toDate.atTime(LocalTime.MAX) : null;

        IntegerRange age = parseIntegerRange(ageRange);
        BigDecimalRange salary = parseBigDecimalRange(salaryRange);
        BigDecimalRange amount = parseBigDecimalRange(amountRange);

        LocalDate minDob = null;
        LocalDate maxDob = null;

        LocalDate today = LocalDate.now();

        if (age != null) {
            if (age.max != null) {
                minDob = today.minusYears(age.max);
            }
            if (age.min != null) {
                maxDob = today.minusYears(age.min);
            }
        }

        return loanApplicationRepository.findFilteredApplications(
                fromDateTime,
                toDateTime,
                status,
                customerName,
                customerCity,
                minDob,
                maxDob,
                salary != null ? salary.min : null,
                salary != null ? salary.max : null,
                amount != null ? amount.min : null,
                amount != null ? amount.max : null,
                loanTypeName,
                gender
        );
    }

    public void exportApplicationsToPdf(
            List<LoanApplication> applications,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            String customerName,
            String customerCity,
            String ageRange,
            String salaryRange,
            String amountRange,
            String loanTypeName,
            String gender,
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
        document.add(new Paragraph("Customer City: " + valueOrAll(customerCity), normalFont));
        document.add(new Paragraph("Age Range: " + valueOrAll(ageRange), normalFont));
        document.add(new Paragraph("Salary Range: " + valueOrAll(salaryRange), normalFont));
        document.add(new Paragraph("Amount Range: " + valueOrAll(amountRange), normalFont));
        document.add(new Paragraph("Loan Type: " + valueOrAll(loanTypeName), normalFont));
        document.add(new Paragraph("Gender: " + valueOrAll(gender), normalFont));
        document.add(new Paragraph("Total Records: " + applications.size(), normalFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 3f, 2.5f, 2f, 2.5f, 2f, 2.5f, 2.5f});

        addPdfHeader(table, "App No", headerFont);
        addPdfHeader(table, "Customer", headerFont);
        addPdfHeader(table, "City", headerFont);
        addPdfHeader(table, "Gender", headerFont);
        addPdfHeader(table, "Loan Type", headerFont);
        addPdfHeader(table, "Amount", headerFont);
        addPdfHeader(table, "Status", headerFont);
        addPdfHeader(table, "Created Date", headerFont);

        for (LoanApplication a : applications) {
            table.addCell(getSafe(a.getApplicationNo()));
            table.addCell(a.getCustomer() != null ? getSafe(a.getCustomer().getName()) : "N/A");
            table.addCell(a.getCustomer() != null ? getSafe(a.getCustomer().getCity()) : "N/A");
            table.addCell(a.getCustomer() != null ? getSafe(a.getCustomer().getGender()) : "N/A");
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
            String customerCity,
            String ageRange,
            String salaryRange,
            String amountRange,
            String loanTypeName,
            String gender,
            OutputStream outputStream
    ) throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Applications Report");

        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font excelHeaderFont = workbook.createFont();
        excelHeaderFont.setBold(true);
        headerStyle.setFont(excelHeaderFont);

        int rowNum = 0;

        Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Loan Applications Report");

        createInfoRow(sheet, rowNum++, "Generated On", LocalDateTime.now().toString());
        createInfoRow(sheet, rowNum++, "From Date", valueOrAll(fromDate));
        createInfoRow(sheet, rowNum++, "To Date", valueOrAll(toDate));
        createInfoRow(sheet, rowNum++, "Status", valueOrAll(status));
        createInfoRow(sheet, rowNum++, "Customer Name", valueOrAll(customerName));
        createInfoRow(sheet, rowNum++, "Customer City", valueOrAll(customerCity));
        createInfoRow(sheet, rowNum++, "Age Range", valueOrAll(ageRange));
        createInfoRow(sheet, rowNum++, "Salary Range", valueOrAll(salaryRange));
        createInfoRow(sheet, rowNum++, "Amount Range", valueOrAll(amountRange));
        createInfoRow(sheet, rowNum++, "Loan Type", valueOrAll(loanTypeName));
        createInfoRow(sheet, rowNum++, "Gender", valueOrAll(gender));
        createInfoRow(sheet, rowNum++, "Total Records", String.valueOf(applications.size()));

        rowNum++;

        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"App No", "Customer", "City", "Gender", "Loan Type", "Amount", "Status", "Created Date"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (LoanApplication a : applications) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(getSafe(a.getApplicationNo()));
            row.createCell(1).setCellValue(a.getCustomer() != null ? getSafe(a.getCustomer().getName()) : "N/A");
            row.createCell(2).setCellValue(a.getCustomer() != null ? getSafe(a.getCustomer().getCity()) : "N/A");
            row.createCell(3).setCellValue(a.getCustomer() != null ? getSafe(a.getCustomer().getGender()) : "N/A");
            row.createCell(4).setCellValue(a.getLoanType() != null ? getSafe(a.getLoanType().getLoanTypeName()) : "N/A");
            row.createCell(5).setCellValue(a.getAmountRequested() != null ? a.getAmountRequested().doubleValue() : 0.0);
            row.createCell(6).setCellValue(a.getStatus() != null ? getSafe(a.getStatus().getLabel()) : "N/A");
            row.createCell(7).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().toLocalDate().toString() : "N/A");
        }

        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }

    private void createInfoRow(Sheet sheet, int rowNum, String key, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
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

    private IntegerRange parseIntegerRange(String range) {
        if (range == null || range.isBlank()) {
            return null;
        }

        String cleaned = range.replaceAll("\\s+", "");
        String[] parts = cleaned.split("-");

        Integer min = null;
        Integer max = null;

        try {
            if (parts.length == 2) {
                min = !parts[0].isBlank() ? Integer.parseInt(parts[0]) : null;
                max = !parts[1].isBlank() ? Integer.parseInt(parts[1]) : null;
            } else if (parts.length == 1) {
                min = Integer.parseInt(parts[0]);
            }
        } catch (Exception e) {
            return null;
        }

        return new IntegerRange(min, max);
    }

    private BigDecimalRange parseBigDecimalRange(String range) {
        if (range == null || range.isBlank()) {
            return null;
        }

        String cleaned = range.replaceAll("\\s+", "").replace(",", "");
        String[] parts = cleaned.split("-");

        BigDecimal min = null;
        BigDecimal max = null;

        try {
            if (parts.length == 2) {
                min = !parts[0].isBlank() ? new BigDecimal(parts[0]) : null;
                max = !parts[1].isBlank() ? new BigDecimal(parts[1]) : null;
            } else if (parts.length == 1) {
                min = new BigDecimal(parts[0]);
            }
        } catch (Exception e) {
            return null;
        }

        return new BigDecimalRange(min, max);
    }

    private static class IntegerRange {
        private final Integer min;
        private final Integer max;

        private IntegerRange(Integer min, Integer max) {
            this.min = min;
            this.max = max;
        }
    }

    private static class BigDecimalRange {
        private final BigDecimal min;
        private final BigDecimal max;

        private BigDecimalRange(BigDecimal min, BigDecimal max) {
            this.min = min;
            this.max = max;
        }
    }
}