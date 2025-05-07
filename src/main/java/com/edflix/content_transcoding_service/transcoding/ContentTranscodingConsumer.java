package com.edflix.content_transcoding_service.transcoding;

import org.springframework.stereotype.Service;
import io.awspring.cloud.sqs.annotation.SqsListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.edflix.content_transcoding_service.transcoding.TranscodeRequest;

@Service
public class ContentTranscodingConsumer {

    private final MediaConvertTranscodeService mediaConvertTranscodeService;
    private final ObjectMapper objectMapper;

    public ContentTranscodingConsumer(MediaConvertTranscodeService mediaConvertTranscodeService, ObjectMapper objectMapper) {
        this.mediaConvertTranscodeService = mediaConvertTranscodeService;
        this.objectMapper = objectMapper;
    }

    @SqsListener("${aws.sqs.content-transcoding-queue-url}")
    public void receiveMessage(String message) {
        try {
            // Validate if the message is in JSON format
            TranscodeRequest transcodeRequest = objectMapper.readValue(message, TranscodeRequest.class);

            String url = transcodeRequest.getUrl();
            String contentProviderId = transcodeRequest.getContentProviderId();

            if (url == null || contentProviderId == null) {
                throw new IllegalArgumentException("TranscodeRequest must contain valid 'url' and 'contentProviderId' fields");
            }

            // Delegate transcode job creation to the service
            mediaConvertTranscodeService.createTranscodeJob(url, contentProviderId);
            System.out.println("Transcode job created successfully for URL: " + url);

        } catch (JsonProcessingException e) {
            System.err.println("Invalid JSON message: " + message);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Failed to process message: " + message);
            e.printStackTrace();
        }
    }
}
