package com.japac.pac.PDF;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.Pfm2afm;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class TemplatePDF {
    private Context context;
    private File pdfArchivo;
    private Document documento;
    private PdfWriter pdfWriter;
    private Paragraph parrafo;

    public TemplatePDF(Context context) {
        this.context = context;
    }

    public void abrirDocumento() {
        crearArchivo();
        try {
            documento = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(documento, new FileOutputStream(pdfArchivo));
            documento.open();
        } catch (Exception e) {
            Toast.makeText(context, "No se a podido abrir el pdf", Toast.LENGTH_SHORT).show();
        }
    }

    private void crearArchivo() {

        File carpeta = new File(Environment.getExternalStorageState().toString(), "PDF");

        if (!carpeta.exists())
            carpeta.mkdir();
        pdfArchivo = new File(carpeta, "TemplatePDF.pdf");

    }

    public void cerrarDocumento() {
        documento.close();
    }

    public void crearTabla(String[] header, ArrayList<int[]> dia) {
        try {
            documento.addTitle("Registro diario de jornada a tiempo completo");
            parrafo = new Paragraph();
            PdfPTable pdfPTable = new PdfPTable(header.length);
            pdfPTable.setWidthPercentage(100);
            PdfPCell pdfPCell;
            int indexC = 0;

            while (indexC < header.length) {
                pdfPCell = new PdfPCell(new Phrase(header[indexC++]));
                pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                pdfPCell.setBackgroundColor(BaseColor.WHITE);
                pdfPTable.addCell(pdfPCell);
            }

            for (int indexR = 0; indexR < dia.size(); indexR++) {
                int[] row = dia.get(indexR);
                for (indexC = 0; indexC < dia.size(); indexC++) {
                    pdfPCell = new PdfPCell(new Phrase(row[indexC]));
                    pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pdfPCell.setFixedHeight(40);
                    pdfPTable.addCell(pdfPCell);
                }
            }

            parrafo.add(pdfPTable);
            documento.add(parrafo);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void añadirMetaData(String empleado, String empresa, String nif, String ccc, String mes, ArrayList<String[]> horaEntrada, ArrayList<String[]> horaSalida, ArrayList<String[]> totalHorasOrdinarias, ArrayList<String[]> totalHorasComplementarias, String urlFirmaEmpleado, String urlFirmaJefe) {

    }
}
