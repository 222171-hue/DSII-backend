package com.medicalcenter.apirsfinalproject.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.UnitValue;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;

    public byte[] generateAppointmentsReport(String specialty, LocalDate startDate, LocalDate endDate) {
        List<Appointment> appointments = appointmentRepository.findAll();
        
        if (specialty != null && !specialty.trim().isEmpty()) {
            appointments = appointments.stream()
                .filter(a -> a.getSpecialist().getEspecialidad().getName().equalsIgnoreCase(specialty))
                .toList();
        }

        if (startDate != null) {
            appointments = appointments.stream()
                .filter(a -> !a.getAppointmentDate().isBefore(startDate))
                .toList();
        }

        if (endDate != null) {
            appointments = appointments.stream()
                .filter(a -> !a.getAppointmentDate().isAfter(endDate))
                .toList();
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        String title = specialty != null && !specialty.trim().isEmpty() 
            ? "Reporte de Citas Médicas - " + specialty 
            : "Reporte de Citas Médicas - UNAMBA";
            
        DeviceRgb titleColor = new DeviceRgb(31, 41, 55); // Gray 800
        document.add(new Paragraph(title)
            .setFontSize(20)
            .setBold()
            .setFontColor(titleColor)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));
        
        document.add(createReportTable(appointments));
        
        // Add footer
        document.add(new Paragraph("Generado el: " + LocalDate.now().toString())
            .setFontSize(10)
            .setFontColor(new DeviceRgb(156, 163, 175)) // Gray 400
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginTop(10));
            
        document.close();
        return baos.toByteArray();
    }
    
    private Table createReportTable(List<Appointment> appointments) {
        Table table = new Table(new float[]{2, 2, 3, 2, 2});
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        DeviceRgb headerBg = new DeviceRgb(22, 163, 74);
        String[] headers = {"Especialidad", "Fecha y Hora", "Estudiante", "Carrera", "Estado"};
        
        for (String h : headers) {
            Cell cell = new Cell()
                .add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerBg)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(Border.NO_BORDER);
            table.addHeaderCell(cell);
        }

        Color rowEvenBg = ColorConstants.WHITE;
        Color rowOddBg = new DeviceRgb(240, 253, 244);

        int rowIndex = 0;
        for (Appointment a : appointments) {
            boolean isEven = rowIndex++ % 2 == 0;
            populateTableRow(table, a, isEven ? rowEvenBg : rowOddBg);
        }
        return table;
    }

    private void populateTableRow(Table table, Appointment a, Color rowBg) {
        String spec = a.getSpecialist() != null && a.getSpecialist().getEspecialidad() != null 
            ? a.getSpecialist().getEspecialidad().getName() : "N/A";
        table.addCell(createCell(spec, rowBg, TextAlignment.LEFT));
        
        table.addCell(createCell(a.getAppointmentDate().toString() + " " + a.getStartTime().toString(), rowBg, TextAlignment.CENTER));
        
        if (a.getStudent() != null) {
            table.addCell(createCell(a.getStudent().getNombre() + " " + a.getStudent().getApellidos(), rowBg, TextAlignment.LEFT));
            table.addCell(createCell(a.getStudent().getCarrera() != null ? a.getStudent().getCarrera() : "N/A", rowBg, TextAlignment.CENTER));
        } else {
            table.addCell(createCell("N/A", rowBg, TextAlignment.CENTER));
            table.addCell(createCell("N/A", rowBg, TextAlignment.CENTER));
        }
        
        table.addCell(createCell(a.getStatus().name(), rowBg, TextAlignment.CENTER));
    }
    
    private Cell createCell(String content, Color bgColor, TextAlignment alignment) {
        return new Cell()
            .add(new Paragraph(content).setFontSize(10).setFontColor(new DeviceRgb(55, 65, 81))) // Gray 700
            .setBackgroundColor(bgColor)
            .setTextAlignment(alignment)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .setPadding(6)
            .setBorder(new SolidBorder(new DeviceRgb(229, 231, 235), 1)); // Gray 200
    }
}
