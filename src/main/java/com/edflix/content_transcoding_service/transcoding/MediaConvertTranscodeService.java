package com.edflix.content_transcoding_service.transcoding;

import java.util.Collections;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient;
import software.amazon.awssdk.services.mediaconvert.model.*;

@Service
public class MediaConvertTranscodeService {

    private final MediaConvertClient mediaConvertClient;

    public MediaConvertTranscodeService(MediaConvertClient mediaConvertClient) {
        this.mediaConvertClient = mediaConvertClient;
    }

    public void createTranscodeJob(String url, String contentProviderId) {
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
                            .audioSourceName("Audio Selector 1") // Link to the audio selector
                            .codecSettings(AudioCodecSettings.builder()
                                    .codec(AudioCodec.AAC) // Changed to AAC for compatibility
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
                                    .destination("s3://edflix/" + contentProviderId + "/")
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

            // Create the job request
            CreateJobRequest createJobRequest = CreateJobRequest.builder()
                    .role("arn:aws:iam::125309500155:role/service-role/MediaConvert_Default_Role")
                    .settings(jobSettings)
                    .build();

            // Submit the job
            mediaConvertClient.createJob(createJobRequest);
            System.out.println("Transcode job created successfully for URL: " + url);
        } catch (Exception e) {
            System.err.println("Error occurred while creating transcode job for URL: " + url);
            e.printStackTrace();
            throw new RuntimeException("Failed to create transcode job for URL: " + url + ". Error: " + e.getMessage(), e);
        }
    }
}
