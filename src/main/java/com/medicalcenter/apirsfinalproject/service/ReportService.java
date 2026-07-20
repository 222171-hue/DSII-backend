package com.medicalcenter.apirsfinalproject.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.medicalcenter.apirsfinalproject.entity.Appointment;
import com.medicalcenter.apirsfinalproject.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AppointmentRepository appointmentRepository;

    public byte[] generateAppointmentsReport(String specialty) {
        List<Appointment> appointments = appointmentRepository.findAll();
        
        if (specialty != null && !specialty.trim().isEmpty()) {
            appointments = appointments.stream()
                .filter(a -> a.getSpecialist().getEspecialidad().getName().equalsIgnoreCase(specialty))
                .collect(Collectors.toList());
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        String title = specialty != null && !specialty.trim().isEmpty() 
            ? "Reporte de Citas Médicas - " + specialty 
            : "Reporte de Citas Médicas - UNAMBA";
        document.add(new Paragraph(title).setFontSize(18).setBold());
        
        Table table = new Table(new float[]{3, 3, 3, 2, 2});
        table.addHeaderCell("Especialidad");
        table.addHeaderCell("Fecha y Hora");
        table.addHeaderCell("Estudiante");
        table.addHeaderCell("Carrera");
        table.addHeaderCell("Estado");

        for (Appointment a : appointments) {
            table.addCell(a.getSpecialist().getEspecialidad().getName());
            table.addCell(a.getAppointmentDate().toString() + " " + a.getStartTime().toString());
            table.addCell(a.getStudent().getNombre() + " " + a.getStudent().getApellidos());
            table.addCell(a.getStudent().getCarrera());
            table.addCell(a.getStatus().name());
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }
}
