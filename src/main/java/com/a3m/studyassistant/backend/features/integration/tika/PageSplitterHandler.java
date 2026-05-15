package com.a3m.studyassistant.backend.features.integration.tika;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class PageSplitterHandler extends DefaultHandler {
    private final Map<Integer, String> pageTexts = new HashMap<>();
    private final StringBuilder fallbackContent = new StringBuilder(); // For non-paged files
    private StringBuilder currentContent = new StringBuilder();
    private int pageCounter = 0;
    private boolean isInsidePage = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) {
        String className = atts.getValue("class");

        // Check for "page" (PDF) OR "slide" (PowerPoint)
        boolean isNewSection = "div".equals(qName) && className != null &&
                (className.contains("page") || className.contains("slide"));

        if (isNewSection) {
            // Save whatever we were working on before moving to the next page
            saveCurrentPage();

            pageCounter++;
            currentContent = new StringBuilder();
            isInsidePage = true;
        }
    }

    public void saveCurrentPage() {
        if (currentContent.length() > 0) {
            pageTexts.put(pageCounter, currentContent.toString().trim());
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if ("div".equals(qName) && isInsidePage) {
            pageTexts.put(pageCounter, currentContent.toString().trim());
            isInsidePage = false;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        // Capture for paged logic
        if (isInsidePage) {
            currentContent.append(ch, start, length);
        }
        // Always capture for fallback (Word/TXT)
        fallbackContent.append(ch, start, length);
    }

    public String getFallbackText() { return fallbackContent.toString().trim(); }
    public Map<Integer, String> getPageTexts() { return pageTexts; }
}
