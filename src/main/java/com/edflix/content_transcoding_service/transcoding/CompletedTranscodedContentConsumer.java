package com.edflix.content_transcoding_service.transcoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.annotation.SqsListener;

import java.util.List;

@Service
public class CompletedTranscodedContentConsumer {

    private final ObjectMapper objectMapper;

    public CompletedTranscodedContentConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @SqsListener("${aws.sqs.completed-transcoded-content-queue-url}")
    public void listenToCompletedTranscodedContentQueue(String message) {
        try {
            // Parse the message
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode detailNode = rootNode.path("detail");
            JsonNode outputGroupDetails = detailNode.path("outputGroupDetails");

            // Extract userMetadata and map to TranscodeRequest
            JsonNode userMetadataNode = detailNode.path("userMetadata");
            TranscodeRequest transcodeRequest = objectMapper.treeToValue(userMetadataNode, TranscodeRequest.class);
            System.out.println("Extracted TranscodeRequest: " + transcodeRequest);

            // Extract output file paths
            for (JsonNode outputGroup : outputGroupDetails) {
                JsonNode outputDetails = outputGroup.path("outputDetails");
                for (JsonNode outputDetail : outputDetails) {
                    List<String> outputFilePaths = objectMapper.convertValue(
                            outputDetail.path("outputFilePaths"), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});

                    // Print each output file path to the console
                    for (String filePath : outputFilePaths) {
                        System.out.println("Output file path: " + filePath);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
