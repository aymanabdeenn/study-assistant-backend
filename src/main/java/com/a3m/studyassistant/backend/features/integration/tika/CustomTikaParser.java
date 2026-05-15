package com.a3m.studyassistant.backend.features.integration.tika;

import dev.langchain4j.data.document.Document;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class CustomTikaParser {

    public List<Document> parse(InputStream inputStream, ParseContext context) {
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(false);
        pdfConfig.setSortByPosition(true);
        context.set(PDFParserConfig.class, pdfConfig);

        PageSplitterHandler pageHandler = new PageSplitterHandler();
        org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        // Use TikaInputStream to handle the ZIP structure of PPTX/DOCX properly
        try (org.apache.tika.io.TikaInputStream tikaStream = org.apache.tika.io.TikaInputStream.get(inputStream)) {

            parser.parse(tikaStream, pageHandler, tikaMetadata, context);

            // CRITICAL: Save the last page/slide after parsing finishes
            pageHandler.saveCurrentPage();

            Map<String, String> baseMetadata = new HashMap<>();
            for (String name : tikaMetadata.names()) {
                baseMetadata.put(name, tikaMetadata.get(name));
            }

            Map<Integer, String> pages = pageHandler.getPageTexts();

            // If the map is empty, it means no 'page' or 'slide' divs were found (Word/TXT)
            if (pages.isEmpty()) {
                String fullText = pageHandler.getFallbackText();
                return List.of(Document.from(fullText, createMetadata(baseMetadata, 1)));
            }

            return pages.entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty()) // Don't ingest empty slides
                    .map(entry -> Document.from(entry.getValue(), createMetadata(baseMetadata, entry.getKey())))
                    .toList();

        } catch (Exception e) {
            e.printStackTrace(); // Always print the stack trace for your graduation project debugging
            throw new RuntimeException("Error parsing multimodal file: " + e.getMessage(), e);
        }
    }

    private dev.langchain4j.data.document.Metadata createMetadata(Map<String, String> base, int pageNum) {
        Map<String, String> meta = new HashMap<>(base);
        meta.put("page_number", String.valueOf(pageNum));
        return dev.langchain4j.data.document.Metadata.from(meta);
    }
}
