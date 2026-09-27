package org.vced.convertmicroservice.converter.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
@Slf4j
public class XlsxContentExtractor extends DocumentContentExtractor{
    // дока: https://poi.apache.org/components/spreadsheet/
    /*
            HSSFWorkbook
            XSSFWorkbook
            SXSSFWorkbook
     */
    @Override
    public Map.Entry<File, List<String>> extractTables(String documentPath, String outputPdfFile) {
        try {
            List<String> mdTables = new ArrayList<>();
            XSSFWorkbook workbook = new XSSFWorkbook(OPCPackage.open(new File(documentPath)));

            String pdfPath = getDocumentPathInsideNoTablesDir(documentPath, ".pdf");

            Files.deleteIfExists(Path.of(pdfPath));

            new File(pdfPath).getParentFile().mkdirs();

            PdfContentInserter pdfContentInserter = new PdfContentInserter(pdfPath);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                mdTables.add(convertSheetToMarkdown(sheet));
                pdfContentInserter.addSection(sheet.getSheetName(), getIdxTableMarker(i));
            }
            pdfContentInserter.save();
            return new AbstractMap.SimpleEntry<>(new File(pdfPath), mdTables);
        } catch (IOException e) {
            log.error("file {} does not exist: {}", documentPath, e.getMessage());
            return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
        } catch (InvalidFormatException e) {
            return new AbstractMap.SimpleEntry<>(new File(outputPdfFile), List.of());
        }
    }

    private static String convertSheetToMarkdown(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();
        boolean headerPrinted = false;
        StringBuilder sb = new StringBuilder();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            sb.append("|");
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                sb.append(getCellText(cell)).append("|");
            }
            sb.append("\n");

            if (!headerPrinted) {
                sb.append("|");
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    sb.append("---|");
                }
                sb.append("\n");
                headerPrinted = true;
            }
        }

        return sb.toString();
    }

    private static String getCellText(Cell cell) {
        String text;
        switch (cell.getCellType()) {
            case STRING:
                text = cell.getRichStringCellValue().getString();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    text = cell.getDateCellValue().toString();
                } else {
                    text = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                text = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                text = cell.getCellFormula();
                break;
            case BLANK:
                return "";
            default:
                return "?";
        }

        // Удаляем переносы строк и лишние пробелы
        return text.replaceAll("[\\r\\n]+", " ").trim();
    }

    @Override
    protected DocumentType getDocumentType() {
        return DocumentType.XLSX;
    }
    public static boolean isXSSFWorkbook(String documentPath) {
        try {
            XSSFWorkbook sxssfWorkbook = new XSSFWorkbook(OPCPackage.open(new File(documentPath)));
            sxssfWorkbook.close();
            return true;
        } catch (OLE2NotOfficeXmlFileException | POIXMLException e) {
            return false;
        }
        catch (InvalidFormatException | IOException e) {
            log.error("Incorrect document path: {}"
                    , documentPath, e);
        }
        return false;
    }
}
