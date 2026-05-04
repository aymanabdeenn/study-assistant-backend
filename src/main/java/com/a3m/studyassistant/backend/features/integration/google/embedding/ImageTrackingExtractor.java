package com.a3m.studyassistant.backend.features.integration.google.embedding;

import com.a3m.studyassistant.backend.features.ai.orchestration.PromptProviderService;
import com.a3m.studyassistant.backend.features.integration.google.chat.GeminiChatService;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.ContentHandler;
import java.io.InputStream;

public class ImageTrackingExtractor implements EmbeddedDocumentExtractor {
    private final GeminiChatService chatService;
    private final PromptProviderService promptProviderService;

    public ImageTrackingExtractor(GeminiChatService chatService, PromptProviderService promptProviderService) {
        this.chatService = chatService;
        this.promptProviderService = promptProviderService;
    }

    @Override
    public boolean shouldParseEmbedded(Metadata metadata) {
        // Only process if it's an image
        String contentType = metadata.get(Metadata.CONTENT_TYPE);
        if (contentType == null) return false;

        // Gemini only supports these specific types
        boolean isSupportedFormat = contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp") ||
                contentType.equals("image/heic") ||
                contentType.equals("image/heif")
        );

        if (!isSupportedFormat) return false;

        String sizeStr = metadata.get(Metadata.CONTENT_LENGTH);
        if (sizeStr != null) {
            long size = Long.parseLong(sizeStr);
            if (size < 10240) return false; // Skip if smaller than 10KB
        }

        return true;
    }

    @Override
    public void parseEmbedded(InputStream stream, ContentHandler handler, Metadata metadata, boolean outputHtml) {
        try {
            byte[] imageBytes = stream.readAllBytes();
            String description = chatService.describeImage(imageBytes, metadata.get(Metadata.CONTENT_TYPE), promptProviderService.getPrompt("parse_image_system"));

            String injection = "\n[IMAGE_DESCRIPTION_START]\n" + description + "\n[IMAGE_DESCRIPTION_END]\n";
            char[] chars = injection.toCharArray();
            handler.characters(chars, 0, chars.length);

        } catch (Exception e) {
            System.err.println("Failed to process embedded image: " + e.getMessage());
        }
    }
}
