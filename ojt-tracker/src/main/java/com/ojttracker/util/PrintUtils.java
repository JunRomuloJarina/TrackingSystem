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

/** Prints a paginated OJT attendance report with morning and afternoon punches. */
public final class PrintUtils {
    private PrintUtils() { }

    public static void printReport(Student student, DashboardService.Snapshot snapshot, List<OJTRecord> records) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new ReportPrintable(student, snapshot, records));
        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(null, "Report sent to printer.", "Print", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Printing failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static final class ReportPrintable implements Printable {
        private static final int ROW_HEIGHT = 18;
        private static final int HEADER_HEIGHT = 220;
        private static final int FOOTER_HEIGHT = 110;
        private static final int HORIZONTAL_PADDING = 28;

        private final Student student;
        private final DashboardService.Snapshot snapshot;
        private final List<OJTRecord> records;

        private ReportPrintable(Student student, DashboardService.Snapshot snapshot, List<OJTRecord> records) {
            this.student = student;
            this.snapshot = snapshot;
            this.records = records == null ? List.of() : records;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.translate(pageFormat.getImageableX() + HORIZONTAL_PADDING, pageFormat.getImageableY());
                int pageWidth = Math.max(1, (int) pageFormat.getImageableWidth() - HORIZONTAL_PADDING * 2);
                int pageHeight = (int) pageFormat.getImageableHeight();
                int rowsPerPage = Math.max(1, (pageHeight - HEADER_HEIGHT - FOOTER_HEIGHT) / ROW_HEIGHT);
                int totalPages = Math.max(1, (int) Math.ceil(records.size() / (double) rowsPerPage));
                if (pageIndex >= totalPages) return NO_SUCH_PAGE;

                int y = pageIndex == 0 ? drawHeader(g2, pageWidth) : 20;
                y = drawTableHeader(g2, y, pageWidth);
                int start = pageIndex * rowsPerPage;
                int end = Math.min(start + rowsPerPage, records.size());
                for (int i = start; i < end; i++) y = drawRow(g2, y, records.get(i), pageWidth);
                if (pageIndex == totalPages - 1) drawFooter(g2, y + 20, pageWidth);
                return PAGE_EXISTS;
            } finally {
                g2.dispose();
            }
        }

        private int drawHeader(Graphics2D g2, int pageWidth) {
            int y = 20;
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            drawCentered(g2, "OJT ATTENDANCE REPORT", pageWidth, y);
            y += 30;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            drawCentered(g2, nullSafe(student.getSchool()), pageWidth, y);
            y += 25;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            y = drawKeyValueRow(g2, y, "Student:", nullSafe(student.getFullName()), "Course:", nullSafe(student.getCourse()));
            y = drawKeyValueRow(g2, y, "Student ID:", nullSafe(student.getStudentId()), "Year Level:", nullSafe(student.getYearLevel()));
            y = drawKeyValueRow(g2, y, "OJT Company:", nullSafe(student.getOjtCompany()), "Supervisor:", nullSafe(student.getSupervisor()));
            y = drawKeyValueRow(g2, y, "OJT Period:", DateUtils.formatDate(student.getOjtStartDate()) + " to " + DateUtils.formatDate(student.getExpectedEndDate()), "", "");
            y += 8;
            y = drawKeyValueRow(g2, y, "Required Hours:", snapshot.requiredHours() + " hours", "Completed Hours:", snapshot.completedHours() + " hours");
            y = drawKeyValueRow(g2, y, "Remaining Hours:", snapshot.remainingHours() + " hours", "Progress:", snapshot.progressPercent() + "%");
            return y + 15;
        }

        private int drawTableHeader(Graphics2D g2, int y, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            int[] columns = columnPositions(pageWidth);
            String[] headers = {"Date", "AM In", "AM Out", "PM In", "PM Out", "Break", "Hours", "Remarks"};
            for (int i = 0; i < headers.length; i++) g2.drawString(headers[i], columns[i], y);
            y += 6;
            g2.drawLine(0, y, pageWidth, y);
            return y + 14;
        }

        private int drawRow(Graphics2D g2, int y, OJTRecord record, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
            int[] columns = columnPositions(pageWidth);
            g2.drawString(DateUtils.formatDate(record.getWorkDate()), columns[0], y);
            g2.drawString(DateUtils.formatTime(record.getMorningTimeIn()), columns[1], y);
            g2.drawString(DateUtils.formatTime(record.getMorningTimeOut()), columns[2], y);
            g2.drawString(DateUtils.formatTime(record.getAfternoonTimeIn()), columns[3], y);
            g2.drawString(DateUtils.formatTime(record.getAfternoonTimeOut()), columns[4], y);
            g2.drawString(String.valueOf(record.getBreakHours()), columns[5], y);
            g2.drawString(String.valueOf(record.getTotalHours()), columns[6], y);
            String remarks = record.getRemarks() == null ? "" : record.getRemarks();
            if (remarks.length() > 28) remarks = remarks.substring(0, 25) + "...";
            g2.drawString(remarks, columns[7], y);
            return y + ROW_HEIGHT;
        }

        private void drawFooter(Graphics2D g2, int y, int pageWidth) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawLine(0, y, pageWidth, y);
            y += 16;
            g2.drawString("TOTAL OJT HOURS: " + snapshot.completedHours() + " HOURS", 0, y);
            y += 50;
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int signatureWidth = Math.min(220, pageWidth / 3);
            g2.drawLine(0, y, signatureWidth, y);
            g2.drawLine(pageWidth - signatureWidth, y, pageWidth, y);
            y += 14;
            g2.drawString("Student Signature", 0, y);
            g2.drawString("OJT Supervisor Signature", pageWidth - signatureWidth, y);
        }

        private int[] columnPositions(int pageWidth) {
            int[] widths = {62, 55, 55, 55, 55, 42, 42};
            int[] positions = new int[8];
            positions[0] = 0;
            for (int i = 1; i < positions.length; i++) positions[i] = positions[i - 1] + widths[i - 1];
            positions[7] = Math.min(positions[7], Math.max(0, pageWidth - 100));
            return positions;
        }

        private int drawKeyValueRow(Graphics2D g2, int y, String label1, String value1, String label2, String value2) {
            g2.drawString(label1 + " " + value1, 0, y);
            if (!label2.isEmpty()) g2.drawString(label2 + " " + value2, 300, y);
            return y + 16;
        }

        private void drawCentered(Graphics2D g2, String text, int pageWidth, int y) {
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, Math.max(0, (pageWidth - textWidth) / 2), y);
        }

        private String nullSafe(String value) { return value == null ? "" : value; }
    }
}
