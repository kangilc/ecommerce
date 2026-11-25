package com.ecommerce.product.ai;

import com.ecommerce.common.exception.CustomException;
import com.ecommerce.common.exception.ErrorCode;
import com.google.genai.Client;
import com.google.genai.gaos.models.errors.CreateInteractionClientError;
import com.google.genai.gaos.models.errors.CreateInteractionServerError;
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
import java.util.function.Function;
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

    private static final String FALLBACK_MODEL = "gemini-3.1-flash-lite";

    private final GeminiProperties properties;
    private volatile Client cachedClient;

    @Override
    public GeminiTextResult generateText(
            String model,
            String systemInstruction,
            String prompt,
            String previousInteractionId) {
        return executeWithFallback(model, targetModel -> {
            CreateModelInteraction.Builder builder = CreateModelInteraction.builder()
                    .model(Model.of(targetModel))
                    .input(InteractionsInput.of(prompt));

            if (systemInstruction != null && !systemInstruction.isBlank()) {
                builder.systemInstruction(systemInstruction);
            }
            if (previousInteractionId != null && !previousInteractionId.isBlank()) {
                builder.previousInteractionId(previousInteractionId);
            }
            return builder.build();
        });
    }

    @Override
    public String generateStructuredJson(
            String model,
            String systemInstruction,
            String prompt,
            Map<String, Object> jsonSchema) {
        return executeWithFallback(model, targetModel -> {
            CreateModelInteraction.Builder builder = CreateModelInteraction.builder()
                    .model(Model.of(targetModel))
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
            return builder.build();
        }).text();
    }

    private GeminiTextResult executeWithFallback(
            String primaryModel,
            Function<String, CreateModelInteraction> requestFactory) {
        try {
            return executeInteraction(requestFactory.apply(primaryModel));
        } catch (TransientGeminiException e) {
            if (!FALLBACK_MODEL.equals(primaryModel)) {
                log.warn("Primary model {} unavailable ({}); retrying with {}",
                        primaryModel, e.getMessage(), FALLBACK_MODEL);
                try {
                    return executeInteraction(requestFactory.apply(FALLBACK_MODEL));
                } catch (TransientGeminiException retryEx) {
                    throw new CustomException(
                            ErrorCode.INTERNAL_SERVER_ERROR,
                            "Gemini API request failed: " + retryEx.getMessage());
                }
            }
            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Gemini API request failed: " + e.getMessage());
        }
    }

    private GeminiTextResult executeInteraction(CreateModelInteraction params) throws TransientGeminiException {
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
        } catch (CreateInteractionServerError e) {
            log.error("Gemini API server error: {}", e.getMessage(), e);
            throw new TransientGeminiException(e.getMessage());
        } catch (CreateInteractionClientError e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                log.error("Gemini API rate limit error: {}", e.getMessage(), e);
                throw new TransientGeminiException(e.getMessage());
            }
            log.error("Gemini API client error: {}", e.getMessage(), e);
            throw new CustomException(
                    ErrorCode.INVALID_INPUT_VALUE, "Gemini API request rejected: " + e.getMessage());
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

    private static final class TransientGeminiException extends Exception {
        private TransientGeminiException(String message) {
            super(message);
        }
    }
}
