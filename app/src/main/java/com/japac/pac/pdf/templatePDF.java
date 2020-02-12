package com.japac.pac.pdf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.japac.pac.menu.menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class templatePDF {

    private File pdfFile;
    private Document document;
    private PdfWriter pdfWriter;

    public templatePDF() {
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


    }

    private void closeDocument() {
        document.close();
    }

    public void addMetaData(String empresa, String empleado, String mes, String ano) {
        document.addTitle("Registro del empleado " + empleado + " de " + mes + " de " + ano);
        document.addAuthor(empresa);
    }

    public void crearHeader(String empresa, String empleado, String CIF, String NIF, String NAF, String mes, String ano) {
        try {
            DateFormat dayFormat = new SimpleDateFormat("dd 'del' MM 'de' yyyy", Locale.getDefault());
            DateFormat hourFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String fecha = dayFormat.format(Calendar.getInstance().getTime());
            String hora = hourFormat.format(Calendar.getInstance().getTime());
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

        } catch (DocumentException e) {
            Log.e("crearHeader", e.toString());
        }


    }

    public void tablaMain(String dia, String horaEntrada, String horaSalida, String numeroHorasOrdinarias, String numeroHorasComplementarias, String numeroHorasTotal, String id, String ruta, boolean end, String ano, String empre, String emple, String mesn, FirebaseStorage almacen) {
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
            if (end) {
                tablaEnd(id, ruta, ano, empre, emple, mesn, almacen);
            }
        } catch (DocumentException e) {
            Log.e("tablaMain", e.toString());
        }

    }

    private void tablaEnd(String idEmpleado, String rutaFirma, String ano, String empre, String emple, String mesn, FirebaseStorage almacen) {
        try {

            PdfPTable pdfPTableTablaEnd = new PdfPTable(1);
            pdfPTableTablaEnd.setTotalWidth(PageSize.A4.getWidth());
            pdfPTableTablaEnd.setLockedWidth(true);
            PdfPCell pdfPCellEnd;
            pdfPCellEnd = new PdfPCell(new Phrase("ID del empleado: " + idEmpleado));
            pdfPCellEnd.setHorizontalAlignment(Element.ALIGN_CENTER);
            pdfPTableTablaEnd.addCell(pdfPCellEnd);
            document.add(pdfPTableTablaEnd);
            firma(rutaFirma, ano, empre, emple, mesn, almacen);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }

    private void firma(String rutaFirma, String ano, String empre, String emple, String mesn, FirebaseStorage almacen) {
        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            Bitmap b = BitmapFactory.decodeFile(rutaFirma);
            b = removeMargins2(b);
            int wi = 150;
            if(Math.round(pdfWriter.getVerticalPosition(true) - document.bottomMargin()) < 150){
                wi = Math.round(pdfWriter.getVerticalPosition(true) - document.bottomMargin());
            }
            int le = wi;
            Bitmap out = Bitmap.createScaledBitmap(b, wi, le, false);
            File file = new File(dir, "firma2.jpg");
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                b.recycle();
                out.recycle();
            } catch (Exception ignored) {

            }
            Image image = Image.getInstance(file.getAbsolutePath());
            document.add(image);
            closeDocument();
            subida(almacen, ano, empre, emple, mesn);
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap removeMargins2(Bitmap bmp) {
        long dtMili = System.currentTimeMillis();
        int MTop = 0, MBot = 0, MLeft = 0, MRight = 0;
        boolean found2 = false;

        int[] bmpIn = new int[bmp.getWidth() * bmp.getHeight()];
        int[][] bmpInt = new int[bmp.getWidth()][bmp.getHeight()];

        bmp.getPixels(bmpIn, 0, bmp.getWidth(), 0, 0, bmp.getWidth(),
                bmp.getHeight());

        for (int ii = 0, contX = 0, contY = 0; ii < bmpIn.length; ii++) {
            bmpInt[contX][contY] = bmpIn[ii];
            contX++;
            if (contX >= bmp.getWidth()) {
                contX = 0;
                contY++;
                if (contY >= bmp.getHeight()) {
                    break;
                }
            }
        }

        for (int hP = 0; hP < bmpInt[0].length && !found2; hP++) {
            // looking for MTop
            for (int wP = 0; wP < bmpInt.length; wP++) {
                if (bmpInt[wP][hP] != Color.WHITE) {
                    Log.e("MTop 2", "Pixel found @" + hP);
                    MTop = hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int hP = bmpInt[0].length - 1; hP >= 0 && !found2; hP--) {
            // looking for MBot
            for (int[] ints : bmpInt) {
                if (ints[hP] != Color.WHITE) {
                    Log.e("MBot 2", "Pixel found @" + hP);
                    MBot = bmp.getHeight() - hP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = 0; wP < bmpInt.length && !found2; wP++) {
            // looking for MLeft
            for (int hP = 0; hP < bmpInt[0].length; hP++) {
                if (bmpInt[wP][hP] != Color.WHITE) {
                    Log.e("MLeft 2", "Pixel found @" + wP);
                    MLeft = wP;
                    found2 = true;
                    break;
                }
            }
        }
        found2 = false;

        for (int wP = bmpInt.length - 1; wP >= 0 && !found2; wP--) {
            // looking for MRight
            for (int hP = 0; hP < bmpInt[0].length; hP++) {
                if (bmpInt[wP][hP] != Color.WHITE) {
                    Log.e("MRight 2", "Pixel found @" + wP);
                    MRight = bmp.getWidth() - wP;
                    found2 = true;
                    break;
                }
            }

        }
        found2 = false;

        int sizeY = bmp.getHeight() - MBot - MTop, sizeX = bmp.getWidth()
                - MRight - MLeft;

        Bitmap bmp2 = Bitmap.createBitmap(bmp, MLeft, MTop, sizeX, sizeY);
        dtMili = (System.currentTimeMillis() - dtMili);
        Log.e("Margin   2",
                "Time needed " + dtMili + "mSec\nh:" + bmp.getWidth() + "w:"
                        + bmp.getHeight() + "\narray x:" + bmpInt.length + "y:"
                        + bmpInt[0].length);
        return bmp2;
    }

    private void subida(FirebaseStorage almacen, String ao1, String empresa, String empleado, String mesnu) {
        final StorageReference pdfRef = almacen.getReference();
        Uri file = Uri.fromFile(pdfFile);
        StorageReference riversRef = pdfRef.child(empresa + "/Registros/" + empleado + "/" + ao1 + "/" + mesnu + "/" + file.getLastPathSegment());

        Uri path = Uri.fromFile(pdfFile);

        UploadTask uploadTask = riversRef.putFile(path);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                pdfFile.delete();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                menu.getInstance().menuShareA();
                pdfFile.delete();
            }
        });
    }
}
