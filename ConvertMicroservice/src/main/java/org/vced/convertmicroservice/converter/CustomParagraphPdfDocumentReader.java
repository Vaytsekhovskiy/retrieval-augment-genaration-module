package org.vced.convertmicroservice.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.ParagraphManager;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.core.io.Resource;

/**
 * Дополнение ParagraphPdfDocumentReader для передачи Y-координат в метаданные
 */
@Slf4j
public class CustomParagraphPdfDocumentReader extends ParagraphPdfDocumentReader {

    private static final String METADATA_START_POSITION = "position";
    private static final String METADATA_END_POSITION = "end_position";

    /**
     * Constructs a ParagraphPdfDocumentReader using a resource URL.
     *
     * @param resourceUrl The URL of the PDF resource.
     */
    public CustomParagraphPdfDocumentReader(String resourceUrl) {
        super(resourceUrl);
    }

    /**
     * Constructs a ParagraphPdfDocumentReader using a resource.
     *
     * @param pdfResource The PDF resource.
     */
    public CustomParagraphPdfDocumentReader(Resource pdfResource) {
        super(pdfResource);
    }

    /**
     * Constructs a ParagraphPdfDocumentReader using a resource URL and a configuration.
     *
     * @param resourceUrl The URL of the PDF resource.
     * @param config      The configuration for PDF document processing.
     */
    public CustomParagraphPdfDocumentReader(String resourceUrl, PdfDocumentReaderConfig config) {
        super(resourceUrl, config);
    }

    /**
     * Constructs a ParagraphPdfDocumentReader using a resource and a configuration.
     *
     * @param pdfResource The PDF resource.
     * @param config      The configuration for PDF document processing.
     */
    public CustomParagraphPdfDocumentReader(Resource pdfResource, PdfDocumentReaderConfig config) {
        super(pdfResource, config);
    }

    @Override
    protected void addMetadata(ParagraphManager.Paragraph from, ParagraphManager.Paragraph to, Document document) {
        super.addMetadata(from, to, document);

        float startHeight = this.document.getPage(from.startPageNumber()-1).getMediaBox().getHeight();
        float endHeight = this.document.getPage(to.startPageNumber()-1).getMediaBox().getHeight();
        float fromPosition = 0;
        float toPosition = 0;
        try {
            fromPosition = from.position() / startHeight;
            toPosition = to.position() / endHeight;
        } catch (ArithmeticException e) {
            log.error("Error: Paragraph height is 0; {}", e.getMessage());
        }
        document.getMetadata().put(METADATA_START_POSITION, fromPosition);
        document.getMetadata().put(METADATA_END_POSITION, toPosition);
        // в процентах (0 - самый низ, 1 - самый верх страницы), чтобы не мешала разная высота разных страниц
    }
}