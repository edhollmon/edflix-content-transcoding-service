package com.edflix.content_transcoding_service.transcoding;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient;
import software.amazon.awssdk.services.mediaconvert.model.*;

@Service
public class MediaConvertTranscodeService {

        @Value("${aws.mediaconvert.role.arn}")
        private String mediaConvertRoleArn;

        @Value("${aws.s3.destination.bucket.name}")
        private String s3DestinationBucketName;

        private final MediaConvertClient mediaConvertClient;
    private final ObjectMapper objectMapper;

    public MediaConvertTranscodeService(MediaConvertClient mediaConvertClient, ObjectMapper objectMapper) {
        this.mediaConvertClient = mediaConvertClient;
        this.objectMapper = objectMapper;
    }

    public void createTranscodeJob(String url, String contentProviderId, TranscodeRequest transcodeRequest) {
        try {
            // Define output settings
            Output output = Output.builder()
                    .containerSettings(ContainerSettings.builder()
                            .container(ContainerType.MP4) // Changed to MP4 for compatibility
                            .build())
                    .videoDescription(VideoDescription.builder()
                            .codecSettings(VideoCodecSettings.builder()
                                    .codec(VideoCodec.H_264)
                                    .h264Settings(H264Settings.builder()
                                            .bitrate(5000000)
                                            .rateControlMode(H264RateControlMode.CBR)
                                            .build())
                                    .build())
                            .build())
                    .audioDescriptions(AudioDescription.builder()
                            .audioSourceName("Audio Selector 1")
                            .codecSettings(AudioCodecSettings.builder()
                                    .codec(AudioCodec.AAC)
                                    .aacSettings(AacSettings.builder()
                                            .bitrate(96000)
                                            .codingMode(AacCodingMode.CODING_MODE_2_0)
                                            .sampleRate(48000)
                                            .build())
                                    .build())
                            .build())
                    .build();

            // Define output group settings
            OutputGroup outputGroup = OutputGroup.builder()
                    .name("File Group")
                    .outputGroupSettings(OutputGroupSettings.builder()
                            .type(OutputGroupType.FILE_GROUP_SETTINGS)
                            .fileGroupSettings(FileGroupSettings.builder()
                                    .destination("s3://"+s3DestinationBucketName+"/" + contentProviderId + "/")
                                    .build())
                            .build())
                    .outputs(output)
                    .build();

            // Define job settings
            JobSettings jobSettings = JobSettings.builder()
                    .inputs(Collections.singletonList(
                            Input.builder()
                                    .fileInput(url)
                                    .audioSelectors(Collections.singletonMap(
                                            "Audio Selector 1", // Define the audio selector
                                            AudioSelector.builder()
                                                    .defaultSelection(AudioDefaultSelection.DEFAULT)
                                                    .build()))
                                    .build()))
                    .outputGroups(outputGroup)
                    .build();

            // Convert TranscodeRequest to userMetadata
            Map<String, String> userMetadata = objectMapper.convertValue(transcodeRequest, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});

            // Create the job request with userMetadata
            CreateJobRequest createJobRequest = CreateJobRequest.builder()
                    .role(mediaConvertRoleArn)
                    .settings(jobSettings)
                    .userMetadata(userMetadata)
                    .build();

            // Submit the job
            mediaConvertClient.createJob(createJobRequest);
            System.out.println("Service - Transcode job created successfully for URL: " + url);
        } catch (Exception e) {
            System.err.println("Error occurred while creating transcode job for URL: " + url);
            e.printStackTrace();
            throw new RuntimeException("Failed to create transcode job for URL: " + url + ". Error: " + e.getMessage(), e);
        }
    }
}
