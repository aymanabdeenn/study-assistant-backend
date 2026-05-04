package com.a3m.studyassistant.backend.features.integration.tika;

import dev.langchain4j.data.document.Document;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.ContentHandler;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

@Component
public class CustomTikaParser {
    public Document parse(InputStream inputStream, ParseContext context) {
        ContentHandler handler = new BodyContentHandler(-1);
        org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        try {
            // Perform the actual Tika parse
            parser.parse(inputStream, handler, tikaMetadata, context);

            // 🛠️ Bridge: Convert Tika Metadata to LangChain4j Metadata
            Map<String, String> metadataMap = new HashMap<>();
            for (String name : tikaMetadata.names()) {
                 // We take the first value if multiple exist for a single key
                 metadataMap.put(name, tikaMetadata.get(name));
            }

        // Return a LangChain4j Document
        return Document.from(
            handler.toString(),
            dev.langchain4j.data.document.Metadata.from(metadataMap)
        );

    } catch (Exception e) {
        throw new RuntimeException("Error parsing multimodal PDF", e);
    }
    }
}
