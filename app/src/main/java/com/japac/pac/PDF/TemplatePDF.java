package com.japac.pac.PDF;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class TemplatePDF {

    private Context context;
    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;
    private String fecha, hora;

    public TemplatePDF(Context context) {
        this.context = context;
    }

    public void openDocument(String empleado, String mes, String ano) {
        createFile(empleado, mes, ano);
        try {
            document = new Document(PageSize.A4);
            pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

        } catch (Exception e) {
            Log.e("openDocument", e.toString());
        }
    }

    private void createFile(String empleado, String mes, String ano) {
        File folder = new File(Environment.getExternalStorageDirectory().toString(), "PDF");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        pdfFile = new File(folder, empleado + "_" + mes + "_" + ano + ".pdf");
        Log.d("FILE PATH", pdfFile.getAbsolutePath());
    }

    public void closeDocument() {
        document.close();
    }

    public void addMetaData(String empresa, String empleado, String mes, String ano) {
        document.addTitle("Registro del empleado " + empleado + " de " + mes + " de " + ano);
        document.addAuthor(empresa);
    }

    public void crearHeader(String empresa, String empleado, String CIF, String NIF, String NAF, String mes, String ano) {

        try {
            DateFormat dayFormat = new SimpleDateFormat("dd 'del' MM 'de' yyyy");
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
            fecha = dayFormat.format(Calendar.getInstance().getTime());
            hora = hourFormat.format(Calendar.getInstance().getTime());
            PdfPTable pdfPTableTablaTitular = new PdfPTable(1);
            pdfPTableTablaTitular.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableTablaTitular.setLockedWidth(true);
            PdfPCell pdfPCellTitulo;
            pdfPCellTitulo = new PdfPCell(new Phrase("REGISTRO DIARIO DE JORNADA EN TRABAJADORES A TIEMPO COMPLETO"));
            pdfPCellTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaTitular.addCell(pdfPCellTitulo);
            document.add(pdfPTableTablaTitular);
            PdfPTable pdfPTableInfo = new PdfPTable(2);
            pdfPTableInfo.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableInfo.setLockedWidth(true);
            PdfPCell pdfPCellInfo;
            pdfPCellInfo = new PdfPCell(new Phrase("EMPRESA"));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPCellInfo = new PdfPCell(new Phrase("EMPLEADO"));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("Nombre o razón social: " + empresa));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("Nombre de Empleado: " + empleado));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("CIF: " + CIF));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("NIF: " + NIF));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("Generado el " + fecha + " a las " + hora));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            pdfPCellInfo = new PdfPCell(new Phrase("NAF: " + NAF));
            pdfPCellInfo.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableInfo.addCell(pdfPCellInfo);
            document.add(pdfPTableInfo);
            PdfPTable pdfPTableTablaColumnas = new PdfPTable(6);
            pdfPTableTablaColumnas.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableTablaColumnas.setLockedWidth(true);
            PdfPCell pdfPCellColumnas;
            pdfPCellColumnas = new PdfPCell(new Phrase(mes + " de " + ano));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            pdfPCellColumnas = new PdfPCell(new Phrase("Hora de entrada"));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            pdfPCellColumnas = new PdfPCell(new Phrase("Hora de la salida"));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            pdfPCellColumnas = new PdfPCell(new Phrase("Horas ordinarias"));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            pdfPCellColumnas = new PdfPCell(new Phrase("Horas complementarias"));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            pdfPCellColumnas = new PdfPCell(new Phrase("Total horas"));
            pdfPCellColumnas.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaColumnas.addCell(pdfPCellColumnas);
            document.add(pdfPTableTablaColumnas);
            Log.d("añadida header", "bien");
        } catch (DocumentException e) {
            Log.e("crearHeader", e.toString());
        }


    }

    public void tablaMain(String dia, String horaEntrada, String horaSalida, String numeroHorasOrdinarias, String numeroHorasComplementarias, String numeroHorasTotal) {

        try {
            PdfPTable pdfPTableTablaMain = new PdfPTable(6);
            pdfPTableTablaMain.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableTablaMain.setLockedWidth(true);
            PdfPCell pdfPCellMain;
            pdfPCellMain = new PdfPCell(new Phrase(dia));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            pdfPCellMain = new PdfPCell(new Phrase(horaEntrada));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            pdfPCellMain = new PdfPCell(new Phrase(horaSalida));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            pdfPCellMain = new PdfPCell(new Phrase(numeroHorasOrdinarias));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            pdfPCellMain = new PdfPCell(new Phrase(numeroHorasComplementarias));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            pdfPCellMain = new PdfPCell(new Phrase(numeroHorasTotal));
            pdfPCellMain.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaMain.addCell(pdfPCellMain);
            document.add(pdfPTableTablaMain);
            Log.d("añadida", "bien");
        } catch (DocumentException e) {
            Log.e("tablaMain", e.toString());
        }

    }

    public void tablaEnd(String idEmpleado) {

        try {

            PdfPTable pdfPTableTablaEnd = new PdfPTable(2);
            pdfPTableTablaEnd.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableTablaEnd.setLockedWidth(true);
            PdfPCell pdfPCellEnd;
            pdfPCellEnd = new PdfPCell(new Phrase(idEmpleado));
            pdfPCellEnd.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaEnd.addCell(pdfPCellEnd);
            document.add(pdfPTableTablaEnd);
            Log.d("tablaEnd", "BIEN");
        } catch (DocumentException e) {
            e.printStackTrace();
        }


    }

}
