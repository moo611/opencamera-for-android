package com.atech.glcamera.grafika.my;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";

    private static final int DEFAULT_SAMPLE_RATE = 48000;
    private static final int DEFAULT_PCM_DATA_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int DEFAULT_CHANNELS = 1;

    private ExecutorService mExecutor = Executors.newCachedThreadPool();
    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private int mSampleRate = DEFAULT_SAMPLE_RATE;
    private int mPcmFormat = DEFAULT_PCM_DATA_FORMAT;
    private int mChannels = DEFAULT_CHANNELS;

    private AudioRecordCallback mRecordCallback;

    private boolean mIsRecording = false;

    public void setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public void setPcmFormat(int pcmFormat) {
        mPcmFormat = pcmFormat;
    }

    public void setRecordCallback(AudioRecordCallback recordCallback) {
        mRecordCallback = recordCallback;
    }

    public void setChannels(int channels) {
        mChannels = channels;
    }

    public int getChannels() {
        return mChannels;
    }

    public boolean start() {
        try {
            int channelConfig = mChannels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_OUT_STEREO;
//            mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, channelConfig, mPcmFormat);
            mBufferSize = getAudioBufferSize(channelConfig, mPcmFormat);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate,
                    channelConfig, mPcmFormat, mBufferSize);
        } catch (Exception e) {
            Log.e(TAG, "init AudioRecord exception: " + e.getLocalizedMessage());
            return false;
        }

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "cannot init AudioRecord");
            return false;
        }
        mIsRecording = true;

        mExecutor.execute(this::record);

        return true;
    }

    // 16BIT 格式兼容性更好
    // 单声道效率更高
    private int getAudioBufferSize(int channelLayout, int pcmFormat) {
        int bufferSize = 1024;

        switch (channelLayout) {
            case AudioFormat.CHANNEL_IN_MONO:
                bufferSize *= 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                bufferSize *= 2;
                break;
        }

        switch (pcmFormat) {
            case AudioFormat.ENCODING_PCM_8BIT:
                bufferSize *= 1;
                break;
            case AudioFormat.ENCODING_PCM_16BIT:
                bufferSize *= 2;
                break;
        }

        return bufferSize;
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        if (mAudioRecord == null || mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }

        ByteBuffer audioBuffer = ByteBuffer.allocate(mBufferSize);
        mAudioRecord.startRecording();
        Log.d(TAG, "AudioRecorder started");

        int readResult;
        while (mIsRecording) {
            readResult = mAudioRecord.read(audioBuffer.array(), 0, mBufferSize);
            if (readResult > 0 && mRecordCallback != null) {
                byte[] data = new byte[readResult];
                audioBuffer.position(0);
                audioBuffer.limit(readResult);
                audioBuffer.get(data, 0, readResult);


                mRecordCallback.onRecordSample(data);
            }
        }

        release();
        Log.d(TAG, "AudioRecorder finished");
    }

    public void stop() {
        mIsRecording = false;
    }

    private void release() {
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public interface AudioRecordCallback {
        // start 在哪个线程调用，就运行在哪个线程

        void onRecordSample(byte[] data);
    }

}