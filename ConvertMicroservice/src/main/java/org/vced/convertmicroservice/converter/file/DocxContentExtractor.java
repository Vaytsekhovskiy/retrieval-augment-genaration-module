package org.vced.convertmicroservice.converter.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlCursor;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.local.JodConverter;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DocxContentExtractor extends DocumentContentExtractor {
    public static boolean isXWPFDocument(String documentPath) {
        try {
            XWPFDocument xwpfDocument = new XWPFDocument(OPCPackage.open(new File(documentPath)));
            xwpfDocument.close();
            return true;
        } catch (POIXMLException | NotOfficeXmlFileException e) {
            return false;
        }
        catch (InvalidFormatException | IOException e) {
            log.error("Incorrect document path: {}"
                    , documentPath, e);
        }
        return false;
    }
    @Override
    public Map.Entry<File, List<String>> extractTables(String documentPath, String outputPdfFile) {
        Path path = Path.of(documentPath);
        Path documentCopyPath = Path.of(getDocumentPathInsideNoTablesDir(documentPath, ".docx"));
        try {
            new File(documentCopyPath.toUri()).getParentFile().mkdirs();
            Files.copy(
                    path,
                    documentCopyPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
            var result = cutTablesFromDocx(documentCopyPath);
            Files.delete(documentCopyPath);
            if (result == null) return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
            return result;
        } catch (IOException e) {
            log.error("directory {} does not exist: {}", DOC_DIRECTORY_NO_TABLES, e.getMessage());
            return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
        }
    }
    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.DOCX;
    }
    // РАБОТАЕТ ТОЛЬКО С DOCX, С DOC НЕ РАБОТАЕТ!
    protected static Map.Entry<File, List<String>> cutTablesFromDocx(Path docxCopy) {
        XWPFDocument xwpfDocument;
        try {
            xwpfDocument = new XWPFDocument(OPCPackage.open(new File(docxCopy.toUri())));
        } catch (IOException | InvalidFormatException e) {
            log.error("Incorrect document path: {}. The system won't process document tables"
                    , docxCopy, e);
            return null;
        } catch (Throwable e) {
            log.error("Unexpected error occurred while extracting tables from document {}", docxCopy, e);
            return null;
        }
        // Таблицы в md формате
        List<String> tablesText = DocxContentExtractor.getDocxTablesText(xwpfDocument);
        // Удаляй таблицы из документа
        try {
            DocxContentExtractor.deleteTablesFromDocx(xwpfDocument, docxCopy);
        } catch (IOException e) {
            log.error("Incorrect document path: {}. The system won't process document tables"
                    , docxCopy, e);
            return null;
        }
        // переведи в пдф
        File pdfWithoutTables = new File(FilenameUtils.removeExtension(docxCopy.toString()) + ".pdf");
        try {
            JodConverter
                    .convert(new File(docxCopy.toUri()))
                    .to(pdfWithoutTables)
                    .execute();
        } catch (OfficeException e) {
            log.error("Failed to convert docx file {}", docxCopy, e);
            return null;
        }

        return new AbstractMap.SimpleEntry<>(pdfWithoutTables, tablesText);
    }

    private static List<XWPFTable> deleteTablesFromDocx(XWPFDocument doc, Path docxPath) throws IOException {
        if (doc == null) {
            return List.of();
        }
        List<XWPFTable> tables = doc.getTables();
        List<XWPFTable> deletedTables = new ArrayList<>(tables);
        // Удаляем таблицы с конца и вставляем маркер. С конца,
        // т.к.  это классическая техника защиты от ошибок при модификации коллекции во время итерации.
        for (int i = tables.size() - 1; i >= 0; i--) {
            XWPFTable table = tables.get(i);

            // Вставляем пометку об удалённой таблице
            XmlCursor cursor = table.getCTTbl().newCursor();
            XWPFParagraph newParagraph = doc.insertNewParagraph(cursor);
            XWPFRun run = newParagraph.createRun();
            run.setText(TABLE_REMOVED_MARKER.replace(TABLE_INDEX, String.valueOf(i)));

            // Удаляем оригинальную таблицу
            doc.removeBodyElement(doc.getPosOfTable(table));
        }
        // перед сохранением в пдф нужно с документа убирать:
        // Ошибка: источник перекрёстной ссылки не найден
        // Эта херобора находится либо в странных таблицах, либо в колонтитулах, всё удаляем
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            List<CTSimpleField> simpleFields = paragraph.getCTP().getFldSimpleList();
            // удаляет все мёртвые ссылки
            for (int i = simpleFields.size() - 1; i >= 0; i--) { // Обратный порядок — безопасно удалять
                CTSimpleField field = simpleFields.get(i);
                String instr = field.getInstr();
                if (instr != null && instr.contains("REF")) {
                    paragraph.getCTP().removeFldSimple(i);
                }
            }
        }
        // Удаление содержимого всех header и footer
        for (XWPFHeader header : doc.getHeaderList()) {
            header.clearHeaderFooter();
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            footer.clearHeaderFooter();
        }

        // Удаление ссылок на колонтитулы в секциях
        CTSectPr bodySectPr = doc.getDocument().getBody().getSectPr();
        if (bodySectPr != null) {
            if (bodySectPr.getHeaderReferenceList() != null) {
                bodySectPr.getHeaderReferenceList().clear();
            }
            if (bodySectPr.getFooterReferenceList() != null) {
                bodySectPr.getFooterReferenceList().clear();
            }
        }

        // Также удаляем ссылки на колонтитулы в параграфах (в разделах)
        for (XWPFParagraph para : doc.getParagraphs()) {
            CTSectPr sectPr = para.getCTP().getPPr() != null ? para.getCTP().getPPr().getSectPr() : null;
            if (sectPr != null) {
                if (sectPr.getHeaderReferenceList() != null) {
                    sectPr.getHeaderReferenceList().clear();
                }
                if (sectPr.getFooterReferenceList() != null) {
                    sectPr.getFooterReferenceList().clear();
                }
            }
        }
        String docxCopy = DOC_DIRECTORY_NO_TABLES + "/" + FilenameUtils.getBaseName(String.valueOf(docxPath)) + "_copy" + ".docx";
        try (FileOutputStream out = new FileOutputStream(docxCopy)) {
            doc.write(out);
        } catch (IOException e) {
            log.error("document: {} already busy with another process. " +
                    "Close other processes. " +
                    "The system won't delete tables from docx", docxPath);
        }
        doc.close();
        Files.delete(Path.of(docxCopy));
        return deletedTables;
    }

    private static List<String> getDocxTablesText(XWPFDocument doc) {
        if (doc == null) {
            return List.of();
        }
        List<XWPFTable> tables = doc.getTables();
        return tables.stream()
                .map(DocxContentExtractor::convertTableToMarkdownWithFirstRowMerge)
                .filter(table -> !table.isBlank())
                .toList();
    }

    private static String convertTableToMarkdownWithFirstRowMerge(XWPFTable table) {
        StringBuilder md = new StringBuilder();
        List<XWPFTableRow> rows = table.getRows();
        if (rows.isEmpty()) return "";

        // Первая строка — заголовок
        XWPFTableRow headerRow = rows.getFirst();
        List<XWPFTableCell> headerCells = headerRow.getTableCells();

        List<String> expandedHeader = new ArrayList<>();

        // Обработка первой строки (заголовка) с учётом объединения
        for (XWPFTableCell cell : headerCells) {
            //String text = cell.getText().trim();
            StringBuilder text = new StringBuilder();
            cell.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append(" "));
            int gridSpan = 1;
            if (cell.getCTTc().getTcPr() != null && cell.getCTTc().getTcPr().isSetGridSpan()) {
                gridSpan = cell.getCTTc().getTcPr().getGridSpan().getVal().intValue();
            }

            for (int i = 0; i < gridSpan; i++) {
                expandedHeader.add(text.toString().trim());
            }
        }

        // Записываем заголовок
        md.append("| ");
        for (String col : expandedHeader) {
            md.append(col).append(" | ");
        }
        md.append("\n");

        // Разделитель
        md.append("|");
        md.append(" --- |".repeat(expandedHeader.size()));
        md.append("\n");

        // Остальные строки — без анализа объединений
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();
            md.append("| ");
            for (XWPFTableCell cell : cells) {
                StringBuilder text = new StringBuilder();
                cell.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append(" "));
                md.append(text.toString().trim()).append(" | ");
            }
            md.append("\n");
        }
        return md.toString();
    }
    /*
        Тот же метод, но не учитывает объединение первой ячейки
        public static String convertTableToMarkdown(XWPFTable table) {
            StringBuilder md = new StringBuilder();

            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) return "";

            // Первая строка — заголовок
            XWPFTableRow headerRow = rows.get(0);
            List<XWPFTableCell> headerCells = headerRow.getTableCells();

            // Заголовок
            for (XWPFTableCell cell : headerCells) {
                md.append("| ").append(cell.getText().trim()).append(" ");
            }
            md.append("|\n");

            // Разделитель
            for (int i = 0; i < headerCells.size(); i++) {
                md.append("|---");
            }
            md.append("|\n");

            // Остальные строки
            for (int i = 1; i < rows.size(); i++) {
                XWPFTableRow row = rows.get(i);
                for (XWPFTableCell cell : row.getTableCells()) {
                    md.append("| ").append(cell.getText().trim()).append(" ");
                }
                md.append("|\n");
            }
            System.out.println(md);
            return md.toString();
        }
     */
}
