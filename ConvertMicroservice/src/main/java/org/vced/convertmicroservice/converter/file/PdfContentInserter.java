package org.vced.convertmicroservice.converter.file;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PdfContentInserter {

    private final PDDocument document;
    private final PDType0Font font;
    private final PDDocumentOutline outline;
    private final String pdfPath;

    public PdfContentInserter(String pdfPath) throws IOException {
        this.pdfPath = pdfPath;
        File file = new File(pdfPath);
        this.document = file.exists() ? Loader.loadPDF(file) : new PDDocument();
        this.font = PDType0Font.load(document, new FileInputStream("./arial.ttf"));

        PDDocumentOutline existingOutline = document.getDocumentCatalog().getDocumentOutline();
        if (existingOutline == null) {
            outline = new PDDocumentOutline();
            document.getDocumentCatalog().setDocumentOutline(outline);
        } else {
            outline = existingOutline;
        }
        addSection("Title", "text");
    }

    public void addSection(String heading, String text) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Заголовок
            contentStream.setFont(font, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(heading);
            contentStream.endText();

            // Основной текст
            contentStream.setFont(font, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 730);

            for (String line : text.split("\n")) {
                contentStream.showText(line.trim());
                contentStream.newLineAtOffset(0, -15);
            }

            contentStream.endText();
        }

        // Добавляем заголовок в оглавление
        PDOutlineItem bookmark = new PDOutlineItem();
        bookmark.setTitle(heading);

        PDPageFitDestination destination = new PDPageFitDestination();
        destination.setPage(page);
        bookmark.setDestination(destination);

        outline.addLast(bookmark);
        outline.openNode();
        bookmark.openNode();
    }

    public void save() throws IOException {
        document.save(pdfPath);
        document.close();
    }
}
