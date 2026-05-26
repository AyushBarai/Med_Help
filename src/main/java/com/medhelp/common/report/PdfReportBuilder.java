package com.medhelp.common.report;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.medhelp.common.lab.Lab;
import com.medhelp.common.order.TestOrder;
import com.medhelp.common.patient.Patient;
import com.medhelp.common.result.TestResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds a branded PDF lab report using iText 8.
 *
 * STRUCTURE OF THE PDF:
 *  1. Header        — lab name, address, phone, email, GSTIN
 *  2. Title bar     — "DIAGNOSTIC REPORT" banner
 *  3. Info grid     — patient details (left) + order details (right)
 *  4. Results       — one section per test, with a parameters table
 *  5. Footer        — "computer generated" disclaimer + verification code
 *
 * HOW iText 8 WORKS:
 *  PdfWriter  → writes bytes to the output stream
 *  PdfDocument → the raw PDF container
 *  Document    → the layout engine (adds paragraphs, tables, etc.)
 *  Everything you add to Document flows like HTML — top to bottom.
 */
@Component
public class PdfReportBuilder {

    // =====================================================================
    //  COLORS — all using RGB (0-255 scale)
    // =====================================================================
    private static final DeviceRgb C_NAVY       = new DeviceRgb(26,  58,  92);   // lab name, headers
    private static final DeviceRgb C_BLUE_MID   = new DeviceRgb(44,  82,  130);  // table header bg
    private static final DeviceRgb C_BLUE_LIGHT = new DeviceRgb(235, 244, 255);  // test section bg
    private static final DeviceRgb C_WHITE_TEXT = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb C_MUTED      = new DeviceRgb(100, 116, 139);  // labels, hints
    private static final DeviceRgb C_BORDER     = new DeviceRgb(226, 232, 240);  // cell borders
    private static final DeviceRgb C_ROW_ALT    = new DeviceRgb(247, 250, 252);  // alternate rows
    private static final DeviceRgb C_INFO_BG    = new DeviceRgb(250, 252, 255);  // patient info box
    private static final DeviceRgb C_RED        = new DeviceRgb(197, 48,  48);   // HIGH / flag
    private static final DeviceRgb C_BLUE_FLAG  = new DeviceRgb(43,  108, 176);  // LOW flag
    private static final DeviceRgb C_GREEN      = new DeviceRgb(39,  103, 73);   // NORMAL flag
    private static final DeviceRgb C_CRITICAL_BG = new DeviceRgb(255, 235, 235); // CRITICAL row bg
    private static final DeviceRgb C_CRITICAL   = new DeviceRgb(116, 42,  42);   // CRITICAL text

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    // =====================================================================
    //  PUBLIC ENTRY POINT
    // =====================================================================

    /**
     * Generates the PDF and returns it as a byte array.
     * The caller saves these bytes to disk or S3.
     *
     * @param lab       the lab (for letterhead)
     * @param patient   the patient (printed on report)
     * @param order     the order (order number, date, etc.)
     * @param items     list of tests with their result parameters
     */
    public byte[] build(Lab lab, Patient patient, TestOrder order,
                        List<TestItemData> items) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // iText 8 setup — three objects that work together
        PdfWriter   writer = new PdfWriter(baos);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);

        // A4 margins: top, right, bottom, left (in points — 1pt ≈ 0.35mm)
        doc.setMargins(36, 40, 50, 40);

        // Fonts — using built-in Helvetica (no external font file needed)
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont bold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Build each section in order
        addHeader(doc, lab, bold, regular);
        addPatientOrderInfo(doc, patient, order, bold, regular);
        addResultsSections(doc, items, bold, regular);
        addFooter(doc, order, bold, regular);

        doc.close(); // IMPORTANT — flushes everything to the output stream
        return baos.toByteArray();
    }

    // =====================================================================
    //  SECTION 1: HEADER — Lab branding
    // =====================================================================

    private void addHeader(Document doc, Lab lab,
                           PdfFont bold, PdfFont regular) throws IOException {

        // Lab name — largest text on the page
        doc.add(new Paragraph(lab.getName())
                .setFont(bold)
                .setFontSize(22)
                .setFontColor(C_NAVY)
                .setMarginBottom(3));

        // Address on one line
        String address = buildAddressLine(lab);
        if (!address.isBlank()) {
            doc.add(new Paragraph(address)
                    .setFont(regular).setFontSize(9)
                    .setFontColor(C_MUTED).setMarginBottom(2));
        }

        // Phone + email on one line
        StringBuilder contact = new StringBuilder();
        if (lab.getPhone() != null) contact.append("Ph: ").append(lab.getPhone());
        if (lab.getEmail() != null) {
            if (!contact.isEmpty()) contact.append("   |   ");
            contact.append("Email: ").append(lab.getEmail());
        }
        if (!contact.isEmpty()) {
            doc.add(new Paragraph(contact.toString())
                    .setFont(regular).setFontSize(9)
                    .setFontColor(C_MUTED).setMarginBottom(2));
        }

        // GSTIN (required for invoicing in India)
        if (lab.getGstin() != null && !lab.getGstin().isBlank()) {
            doc.add(new Paragraph("GSTIN: " + lab.getGstin())
                    .setFont(regular).setFontSize(8)
                    .setFontColor(C_MUTED).setMarginBottom(5));
        }

        // Thick blue divider line under the lab header
        doc.add(new Paragraph()
                .setBorderBottom(new SolidBorder(C_NAVY, 2f))
                .setMarginBottom(8));

        // "DIAGNOSTIC REPORT" banner — navy background, white text
        doc.add(new Paragraph("DIAGNOSTIC REPORT")
                .setFont(bold).setFontSize(11)
                .setFontColor(C_WHITE_TEXT)
                .setBackgroundColor(C_NAVY)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingTop(7).setPaddingBottom(7)
                .setMarginBottom(14));
    }

    // =====================================================================
    //  SECTION 2: PATIENT + ORDER INFO — 2-column grid
    // =====================================================================

    private void addPatientOrderInfo(Document doc, Patient patient,
                                     TestOrder order,
                                     PdfFont bold, PdfFont regular) throws IOException {

        // A 2-column table — left = patient, right = order details
        Table grid = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(16);

        // LEFT: patient details
        Cell left = new Cell()
                .setBorder(new SolidBorder(C_BORDER, 0.5f))
                .setBackgroundColor(C_INFO_BG)
                .setPadding(10);

        left.add(new Paragraph("PATIENT INFORMATION")
                .setFont(bold).setFontSize(8).setFontColor(C_MUTED)
                .setMarginBottom(7));

        addInfoRow(left, "Name",       patient.getName(), bold, regular);
        addInfoRow(left, "Age/Gender", buildAgeGender(patient), bold, regular);
        if (patient.getPhone() != null)
            addInfoRow(left, "Phone", patient.getPhone(), bold, regular);
        if (patient.getDob() != null)
            addInfoRow(left, "DOB",   patient.getDob().format(DATE_FMT), bold, regular);

        // RIGHT: order details
        Cell right = new Cell()
                .setBorder(new SolidBorder(C_BORDER, 0.5f))
                .setBackgroundColor(C_INFO_BG)
                .setPadding(10);

        right.add(new Paragraph("ORDER INFORMATION")
                .setFont(bold).setFontSize(8).setFontColor(C_MUTED)
                .setMarginBottom(7));

        addInfoRow(right, "Order No.", order.getOrderNumber(), bold, regular);
        addInfoRow(right, "Date",
                order.getCreatedDate() != null
                        ? order.getCreatedDate().format(DATETIME_FMT) : "-",
                bold, regular);
        if (order.getReferredBy() != null)
            addInfoRow(right, "Referred by", "Dr. " + order.getReferredBy(), bold, regular);
        addInfoRow(right, "Collection",
                order.getCollectionType().name().replace("_", " "), bold, regular);
        if (order.getExpectedAt() != null)
            addInfoRow(right, "Expected by", order.getExpectedAt().format(DATETIME_FMT), bold, regular);

        grid.addCell(left);
        grid.addCell(right);
        doc.add(grid);
    }

    // =====================================================================
    //  SECTION 3: TEST RESULTS — one block per test
    // =====================================================================

    private void addResultsSections(Document doc, List<TestItemData> items,
                                    PdfFont bold, PdfFont regular) throws IOException {
        for (TestItemData item : items) {
            // Test name banner (blue background)
            doc.add(new Paragraph(item.getTestName().toUpperCase())
                    .setFont(bold).setFontSize(10)
                    .setFontColor(C_NAVY)
                    .setBackgroundColor(C_BLUE_LIGHT)
                    .setPaddingTop(7).setPaddingBottom(7).setPaddingLeft(8)
                    .setMarginBottom(1));

            // Category subtitle
            if (item.getCategory() != null) {
                doc.add(new Paragraph(item.getCategory() +
                        (item.getSampleType() != null ? "  |  Sample: " + item.getSampleType() : ""))
                        .setFont(regular).setFontSize(8).setFontColor(C_MUTED)
                        .setMarginBottom(4));
            }

            if (item.getResults().isEmpty()) {
                doc.add(new Paragraph("Results not yet available.")
                        .setFont(regular).setFontSize(9).setFontColor(C_MUTED)
                        .setMarginBottom(12));
                continue;
            }

            // Results table — 5 columns
            Table table = new Table(UnitValue.createPercentArray(new float[]{3.5f, 1.5f, 1.5f, 2.5f, 1f}))
                    .useAllAvailableWidth()
                    .setMarginBottom(16);

            // Table header row
            for (String h : new String[]{"Parameter", "Value", "Unit", "Reference Range", "Flag"}) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h)
                                .setFont(bold).setFontSize(8)
                                .setFontColor(C_WHITE_TEXT))
                        .setBackgroundColor(C_BLUE_MID)
                        .setBorder(Border.NO_BORDER)
                        .setPaddingTop(6).setPaddingBottom(6)
                        .setPaddingLeft(5).setPaddingRight(5));
            }

            // Data rows — alternate background color for readability
            for (int i = 0; i < item.getResults().size(); i++) {
                addResultRow(table, item.getResults().get(i), i % 2 == 1, bold, regular);
            }

            doc.add(table);
        }
    }

    /**
     * Adds one row to the results table for a single test parameter.
     * Abnormal values are bolded and colored. CRITICAL rows get a red background.
     */
    private void addResultRow(Table table, TestResult r, boolean altRow,
                               PdfFont bold, PdfFont regular) throws IOException {

        // Choose row background based on flag severity
        Color rowBg = switch (r.getFlag()) {
            case CRITICAL -> C_CRITICAL_BG;       // light red
            default       -> altRow ? C_ROW_ALT : ColorConstants.WHITE;
        };

        // Choose text color and font based on flag
        boolean isAbnormal = r.getFlag() != TestResult.ResultFlag.NORMAL;
        Color valueColor = switch (r.getFlag()) {
            case HIGH, CRITICAL -> C_RED;
            case LOW            -> C_BLUE_FLAG;
            default             -> ColorConstants.BLACK;
        };
        PdfFont valueFont = isAbnormal ? bold : regular;

        // Flag display string (H, L, C, or blank for normal)
        String flagStr = switch (r.getFlag()) {
            case HIGH     -> "H";
            case LOW      -> "L";
            case CRITICAL -> "C!";
            default       -> "";
        };
        Color flagColor = switch (r.getFlag()) {
            case HIGH, CRITICAL -> C_RED;
            case LOW            -> C_BLUE_FLAG;
            default             -> C_GREEN;
        };

        table.addCell(makeCell(r.getParameterName(),                              regular,   9,  ColorConstants.BLACK, rowBg, false));
        table.addCell(makeCell(nvl(r.getValue()),                                 valueFont, 9,  valueColor,           rowBg, true));
        table.addCell(makeCell(nvl(r.getUnit()),                                  regular,   8,  C_MUTED,              rowBg, true));
        table.addCell(makeCell(nvl(r.getReferenceRange()),                        regular,   8,  C_MUTED,              rowBg, true));
        table.addCell(makeCell(flagStr,                                            bold,      9,  flagColor,            rowBg, true));
    }

    // =====================================================================
    //  SECTION 4: FOOTER
    // =====================================================================

    private void addFooter(Document doc, TestOrder order,
                           PdfFont bold, PdfFont regular) throws IOException {

        // Thin divider line above footer
        doc.add(new Paragraph()
                .setBorderTop(new SolidBorder(C_BORDER, 0.5f))
                .setMarginTop(20).setMarginBottom(8));

        doc.add(new Paragraph(
                "This is a computer-generated report and does not require a physical signature.")
                .setFont(regular).setFontSize(8).setFontColor(C_MUTED)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));

        doc.add(new Paragraph(
                "Generated on: " + LocalDateTime.now().format(DATETIME_FMT) +
                "   |   Order: " + order.getOrderNumber())
                .setFont(regular).setFontSize(8).setFontColor(C_MUTED)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));

        doc.add(new Paragraph(
                "For queries, contact the lab directly.")
                .setFont(regular).setFontSize(8).setFontColor(C_MUTED)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // =====================================================================
    //  HELPERS
    // =====================================================================

    /** Adds a "Label: Value" row inside a cell */
    private void addInfoRow(Cell cell, String label, String value,
                            PdfFont bold, PdfFont regular) {
        cell.add(new Paragraph()
                .add(new Text(label + ": ").setFont(bold).setFontSize(9).setFontColor(C_MUTED))
                .add(new Text(value).setFont(regular).setFontSize(9).setFontColor(ColorConstants.BLACK))
                .setMarginBottom(3));
    }

    /** Creates a table cell with consistent padding and border */
    private Cell makeCell(String text, PdfFont font, float size,
                          Color color, Color bg, boolean center) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(size).setFontColor(color))
                .setBackgroundColor(bg)
                .setBorder(new SolidBorder(C_BORDER, 0.3f))
                .setPadding(5)
                .setTextAlignment(center ? TextAlignment.CENTER : TextAlignment.LEFT);
    }

    private String buildAddressLine(Lab lab) {
        StringBuilder sb = new StringBuilder();
        if (lab.getAddress() != null) sb.append(lab.getAddress());
        if (lab.getCity()    != null) { if (!sb.isEmpty()) sb.append(", "); sb.append(lab.getCity()); }
        if (lab.getState()   != null) { if (!sb.isEmpty()) sb.append(", "); sb.append(lab.getState()); }
        return sb.toString();
    }

    private String buildAgeGender(Patient patient) {
        StringBuilder sb = new StringBuilder();
        if (patient.getDob() != null) {
            int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
            sb.append(age).append(" years");
        }
        if (patient.getGender() != null) {
            if (!sb.isEmpty()) sb.append(" / ");
            sb.append(patient.getGender().name());
        }
        return sb.isEmpty() ? "Not specified" : sb.toString();
    }

    /** Returns "-" for null strings — prevents NPE in PDF cells */
    private String nvl(String s) { return s != null && !s.isBlank() ? s : "-"; }

    // =====================================================================
    //  DATA CARRIER — passed from ReportService to PdfReportBuilder
    // =====================================================================

    @Data
    @AllArgsConstructor
    public static class TestItemData {
        private String testName;
        private String category;
        private String sampleType;
        private List<TestResult> results;
    }
}