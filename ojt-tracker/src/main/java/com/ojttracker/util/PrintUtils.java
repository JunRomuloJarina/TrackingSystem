package com.ojttracker.util;

import com.ojttracker.model.OJTRecord;
import com.ojttracker.model.Student;
import com.ojttracker.service.DashboardService;

import javax.swing.JOptionPane;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;

/**
 * Builds and prints a professional, school/OJT-documentation-style
 * attendance report using {@link PrinterJob}, including header
 * information, the full attendance table (paginated automatically),
 * totals, and signature lines.
 */
public final class PrintUtils {

    private PrintUtils() {
    }

    public static void printReport(Student student, DashboardService.Snapshot snapshot, List<OJTRecord> records) {
        PrinterJob job = PrinterJob.getPrinterJob();
        ReportPrintable printable = new ReportPrintable(student, snapshot, records);
        job.setPrintable(printable);
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null, "Report sent to printer.", "Print",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Printing failed: " + e.getMessage(), "Print Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** Renders each attendance row plus header/footer content across as many pages as needed. */
    private static class ReportPrintable implements Printable {

        private final Student student;
        private final DashboardService.Snapshot snapshot;
        private final List<OJTRecord> records;

        private static final int ROW_HEIGHT = 18;
        private static final int HEADER_HEIGHT = 220;
        private static final int FOOTER_HEIGHT = 110;
        private static final int HORIZONTAL_PADDING = 36;

        ReportPrintable(Student student, DashboardService.Snapshot snapshot, List<OJTRecord> records) {
            this.student = student;
            this.snapshot = snapshot;
            this.records = records;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX() + HORIZONTAL_PADDING, pageFormat.getImageableY());
            int pageWidth = Math.max(1, (int) pageFormat.getImageableWidth() - (HORIZONTAL_PADDING * 2));
            int pageHeight = (int) pageFormat.getImageableHeight();

            int rowsPerPage = Math.max(1, (pageHeight - HEADER_HEIGHT - FOOTER_HEIGHT) / ROW_HEIGHT);
            int totalPages = (int) Math.ceil(records.size() / (double) rowsPerPage);
            totalPages = Math.max(totalPages, 1);
            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }

            int y = 0;
            if (pageIndex == 0) {
                y = drawHeader(g2, pageWidth);
            } else {
                y = 20;
            }

            y = drawTableHeader(g2, y, pageWidth);

            int start = pageIndex * rowsPerPage;
            int end = Math.min(start + rowsPerPage, records.size());
            for (int i = start; i < end; i++) {
                y = drawRow(g2, y, records.get(i), pageWidth);
            }

            if (pageIndex == totalPages - 1) {
                drawFooter(g2, y + 20, pageWidth);
            }

            return PAGE_EXISTS;
        }

        private int drawHeader(Graphics2D g2, int pageWidth) {
            int y = 20;
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            drawCentered(g2, "OJT ATTENDANCE REPORT", pageWidth, y);
            y += 30;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String school = student.getSchool() == null ? "" : student.getSchool();
            drawCentered(g2, school, pageWidth, y);
            y += 25;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            y = drawKeyValueRow(g2, y, "Student:", nullSafe(student.getFullName()),
                    "Course:", nullSafe(student.getCourse()));
            y = drawKeyValueRow(g2, y, "Student ID:", nullSafe(student.getStudentId()),
                    "Year Level:", nullSafe(student.getYearLevel()));
            y = drawKeyValueRow(g2, y, "OJT Company:", nullSafe(student.getOjtCompany()),
                    "Supervisor:", nullSafe(student.getSupervisor()));
            y = drawKeyValueRow(g2, y, "OJT Period:",
                    nullSafe(DateUtils.formatDate(student.getOjtStartDate())) + " to "
                            + nullSafe(DateUtils.formatDate(student.getExpectedEndDate())),
                    "", "");

            y += 8;
            y = drawKeyValueRow(g2, y, "Required Hours:", snapshot.requiredHours() + " hours",
                    "Completed Hours:", snapshot.completedHours() + " hours");
            y = drawKeyValueRow(g2, y, "Remaining Hours:", snapshot.remainingHours() + " hours",
                    "Progress:", snapshot.progressPercent() + "%");
            y += 15;
            return y;
        }

        private int drawTableHeader(Graphics2D g2, int y, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            int[] cols = columnPositions(pageWidth);
            String[] headers = {"Date", "Time In", "Time Out", "Break", "Hours", "Remarks"};
            for (int i = 0; i < headers.length; i++) {
                g2.drawString(headers[i], cols[i], y);
            }
            y += 6;
            g2.drawLine(0, y, pageWidth, y);
            y += 14;
            return y;
        }

        private int drawRow(Graphics2D g2, int y, OJTRecord record, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int[] cols = columnPositions(pageWidth);
            g2.drawString(DateUtils.formatDate(record.getWorkDate()), cols[0], y);
            g2.drawString(DateUtils.formatTime(record.getTimeIn()), cols[1], y);
            g2.drawString(DateUtils.formatTime(record.getTimeOut()), cols[2], y);
            g2.drawString(String.valueOf(record.getBreakHours()), cols[3], y);
            g2.drawString(String.valueOf(record.getTotalHours()), cols[4], y);
            String remarks = record.getRemarks() == null ? "" : record.getRemarks();
            if (remarks.length() > 40) {
                remarks = remarks.substring(0, 37) + "...";
            }
            g2.drawString(remarks, cols[5], y);
            return y + ROW_HEIGHT;
        }

        private void drawFooter(Graphics2D g2, int y, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawLine(0, y, pageWidth, y);
            y += 16;
            g2.drawString("TOTAL OJT HOURS: " + snapshot.completedHours() + " HOURS", 0, y);
            y += 50;

            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int signatureWidth = 220;
            g2.drawLine(0, y, signatureWidth, y);
            g2.drawLine(pageWidth - signatureWidth, y, pageWidth, y);
            y += 14;
            g2.drawString("Student Signature", 0, y);
            g2.drawString("OJT Supervisor Signature", pageWidth - signatureWidth, y);
        }

        private int[] columnPositions(int pageWidth) {
            return new int[]{0, 90, 170, 250, 310, 370};
        }

        private int drawKeyValueRow(Graphics2D g2, int y, String label1, String value1, String label2, String value2) {
            g2.drawString(label1 + " " + value1, 0, y);
            if (!label2.isEmpty()) {
                g2.drawString(label2 + " " + value2, 300, y);
            }
            return y + 16;
        }

        private void drawCentered(Graphics2D g2, String text, int pageWidth, int y) {
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, Math.max(0, (pageWidth - textWidth) / 2), y);
        }

        private String nullSafe(String s) {
            return s == null ? "" : s;
        }
    }
}