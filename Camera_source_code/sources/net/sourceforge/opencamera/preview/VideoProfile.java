package net.sourceforge.opencamera.preview;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;

public class VideoProfile {
    private static final String TAG = "VideoProfile";
    public int audioBitRate;
    public int audioChannels;
    public int audioCodec;
    public int audioSampleRate;
    public int audioSource;
    public String fileExtension;
    public int fileFormat;
    public boolean no_audio_permission;
    public boolean record_audio;
    public int videoBitRate;
    public double videoCaptureRate;
    public int videoCodec;
    public int videoFrameHeight;
    public int videoFrameRate;
    public int videoFrameWidth;
    public int videoSource;

    VideoProfile() {
        this.fileExtension = "mp4";
    }

    VideoProfile(CamcorderProfile camcorderProfile) {
        this.fileExtension = "mp4";
        this.record_audio = true;
        this.no_audio_permission = false;
        this.audioSource = 5;
        this.audioCodec = camcorderProfile.audioCodec;
        this.audioChannels = camcorderProfile.audioChannels;
        this.audioBitRate = camcorderProfile.audioBitRate;
        this.audioSampleRate = camcorderProfile.audioSampleRate;
        this.fileFormat = camcorderProfile.fileFormat;
        this.videoSource = 1;
        this.videoCodec = camcorderProfile.videoCodec;
        this.videoFrameRate = camcorderProfile.videoFrameRate;
        this.videoCaptureRate = (double) camcorderProfile.videoFrameRate;
        this.videoBitRate = camcorderProfile.videoBitRate;
        this.videoFrameHeight = camcorderProfile.videoFrameHeight;
        this.videoFrameWidth = camcorderProfile.videoFrameWidth;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nAudioSource:        ");
        sb.append(this.audioSource);
        sb.append("\nVideoSource:        ");
        sb.append(this.videoSource);
        sb.append("\nFileFormat:         ");
        sb.append(this.fileFormat);
        sb.append("\nFileExtension:         ");
        sb.append(this.fileExtension);
        sb.append("\nAudioCodec:         ");
        sb.append(this.audioCodec);
        sb.append("\nAudioChannels:      ");
        sb.append(this.audioChannels);
        sb.append("\nAudioBitrate:       ");
        sb.append(this.audioBitRate);
        sb.append("\nAudioSampleRate:    ");
        sb.append(this.audioSampleRate);
        sb.append("\nVideoCodec:         ");
        sb.append(this.videoCodec);
        sb.append("\nVideoFrameRate:     ");
        sb.append(this.videoFrameRate);
        sb.append("\nVideoCaptureRate:   ");
        sb.append(this.videoCaptureRate);
        sb.append("\nVideoBitRate:       ");
        sb.append(this.videoBitRate);
        sb.append("\nVideoWidth:         ");
        sb.append(this.videoFrameWidth);
        sb.append("\nVideoHeight:        ");
        sb.append(this.videoFrameHeight);
        return sb.toString();
    }

    public void copyToMediaRecorder(MediaRecorder mediaRecorder) {
        if (this.record_audio) {
            mediaRecorder.setAudioSource(this.audioSource);
        }
        mediaRecorder.setVideoSource(this.videoSource);
        mediaRecorder.setOutputFormat(this.fileFormat);
        mediaRecorder.setVideoFrameRate(this.videoFrameRate);
        double d = this.videoCaptureRate;
        if (d != ((double) this.videoFrameRate)) {
            mediaRecorder.setCaptureRate(d);
        }
        mediaRecorder.setVideoSize(this.videoFrameWidth, this.videoFrameHeight);
        mediaRecorder.setVideoEncodingBitRate(this.videoBitRate);
        mediaRecorder.setVideoEncoder(this.videoCodec);
        if (this.record_audio) {
            mediaRecorder.setAudioEncodingBitRate(this.audioBitRate);
            mediaRecorder.setAudioChannels(this.audioChannels);
            mediaRecorder.setAudioSamplingRate(this.audioSampleRate);
            mediaRecorder.setAudioEncoder(this.audioCodec);
        }
    }
}
