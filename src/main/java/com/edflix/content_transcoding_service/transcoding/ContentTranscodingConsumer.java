package com.edflix.content_transcoding_service.transcoding;

import org.springframework.stereotype.Service;

import io.awspring.cloud.sqs.annotation.SqsListener;


@Service
public class ContentTranscodingConsumer {

    @SqsListener("${aws.sqs.content-transcoding-queue-url}")
    public void receiveMessage(String message) {
        // Process the message
        System.out.println("Received message: " + message);
        // Add your message processing logic here
    }
}
