package com.ecommerce.product.ai;

import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.exception.ErrorCode;
import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.Content;
import com.google.genai.gaos.models.interactions.CreateModelInteraction;
import com.google.genai.gaos.models.interactions.CreateModelInteractionResponseFormat;
import com.google.genai.gaos.models.interactions.Interaction;
import com.google.genai.gaos.models.interactions.InteractionsInput;
import com.google.genai.gaos.models.interactions.Model;
import com.google.genai.gaos.models.interactions.ModelOutputStep;
import com.google.genai.gaos.models.interactions.ResponseFormat;
import com.google.genai.gaos.models.interactions.Step;
import com.google.genai.gaos.models.interactions.TextContent;
import com.google.genai.gaos.models.interactions.TextResponseFormat;
import com.google.genai.gaos.models.interactions.TextResponseFormatMimeType;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import com.google.genai.gaos.models.operations.CreateInteractionResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gemini Interactions API gateway backed by the official Google Gen AI Java SDK.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleGenAiGateway implements GeminiInteractionGateway {

    private final GeminiProperties properties;
    private volatile Client cachedClient;

    @Override
    public GeminiTextResult generateText(
            String model,
            String systemInstruction,
            String prompt,
            String previousInteractionId) {
        CreateModelInteraction.Builder builder = CreateModelInteraction.builder()
                .model(Model.of(model))
                .input(InteractionsInput.of(prompt));

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            builder.systemInstruction(systemInstruction);
        }
        if (previousInteractionId != null && !previousInteractionId.isBlank()) {
            builder.previousInteractionId(previousInteractionId);
        }

        return executeInteraction(builder.build());
    }

    @Override
    public String generateStructuredJson(
            String model,
            String systemInstruction,
            String prompt,
            Map<String, Object> jsonSchema) {
        CreateModelInteraction.Builder builder = CreateModelInteraction.builder()
                .model(Model.of(model))
                .input(InteractionsInput.of(prompt))
                .responseFormat(
                        CreateModelInteractionResponseFormat.of(
                                ResponseFormat.of(
                                        TextResponseFormat.builder()
                                                .mimeType(TextResponseFormatMimeType.APPLICATION_JSON)
                                                .schema(jsonSchema)
                                                .build())));

        if (systemInstruction != null && !systemInstruction.isBlank()) {
            builder.systemInstruction(systemInstruction);
        }

        return executeInteraction(builder.build()).text();
    }

    private GeminiTextResult executeInteraction(CreateModelInteraction params) {
        Client client = getOrCreateClient();
        try {
            CreateInteractionResponse response =
                    client.interactions.create(CreateInteractionRequestBody.of(params));
            Interaction interaction = response.interaction()
                    .orElseThrow(() -> new CustomException(
                            ErrorCode.INTERNAL_SERVER_ERROR, "No interaction returned from Gemini API"));

            String text = extractOutputText(interaction);
            String interactionId = interaction.id().orElse(null);
            return new GeminiTextResult(text, interactionId);
        } catch (CustomException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR, "Gemini API request failed: " + e.getMessage());
        }
    }

    private String extractOutputText(Interaction interaction) {
        StringBuilder output = new StringBuilder();
        if (interaction.steps().isPresent()) {
            for (Step step : interaction.steps().get()) {
                if (step instanceof ModelOutputStep outputStep && outputStep.content().isPresent()) {
                    for (Content content : outputStep.content().get()) {
                        if (content instanceof TextContent textContent) {
                            textContent.text().ifPresent(output::append);
                        }
                    }
                }
            }
        }
        return output.toString().trim();
    }

    private Client getOrCreateClient() {
        if (cachedClient != null) {
            return cachedClient;
        }
        synchronized (this) {
            if (cachedClient != null) {
                return cachedClient;
            }
            String resolvedKey = resolveApiKey();
            if (resolvedKey == null) {
                throw new CustomException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "Gemini API key is not configured. Set GOOGLE_API_KEY or gemini.api-key.");
            }
            cachedClient = Client.builder().apiKey(resolvedKey).build();
            return cachedClient;
        }
    }

    private String resolveApiKey() {
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            return properties.getApiKey().trim();
        }
        String googleKey = System.getenv("GOOGLE_API_KEY");
        if (googleKey != null && !googleKey.isBlank()) {
            return googleKey.trim();
        }
        String legacyGeminiKey = System.getenv("GEMINI_API_KEY");
        if (legacyGeminiKey != null && !legacyGeminiKey.isBlank()) {
            return legacyGeminiKey.trim();
        }
        return null;
    }
}
