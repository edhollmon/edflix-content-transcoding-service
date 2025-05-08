# Content Transcoding Service
This repository contains the backend service responsible for transcoding uploaded video files into multiple resolutions and generating streaming-ready manifests (e.g., HLS .m3u8) for a video streaming platform.

## Design Doc
https://docs.google.com/document/d/1KfwyUJmzNDB5U-2Jv50Y-q1mU-QKxSSAuNztCxjIhGI/edit?usp=sharing

## High Level Component Designs
https://drive.google.com/file/d/198p-V0xXJtjnu9v8F-k1tdAsk6kXG99P/view?usp=sharing

## Tech Stack
- **Language**: Java
- **Storage**: AWS MediaConvert, AWS S3, AWS SQS
- **Manifest Format**: HLS (`.m3u8`)
- **Database**: DynamoDB
- **Queue**: AWS SQS


