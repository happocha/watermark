package com.test.watermark.encoder;

import static com.test.watermark.ConstantsKt.PULSE_RATE;

import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.test.watermark.encoder.gles.EglCore;
import com.test.watermark.encoder.gles.FullFrameRect;
import com.test.watermark.encoder.gles.Texture2dProgram;
import com.test.watermark.encoder.gles.WindowSurface;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Google video encoder implementation
 * @see <a href="https://github.com/google/grafika/blob/master/app/src/main/java/com/android/grafika/TextureMovieEncoder.java"</a>
 */
public class TextureMovieEncoder implements Runnable {

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_STOP_RECORDING = 1;
    private static final int MSG_FRAME_AVAILABLE = 2;
    private static final int MSG_SET_TEXTURE_ID = 3;
    private static final int MSG_QUIT = 4;

    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private FullFrameRect mFullScreen;
    private int mTextureId;
    private int mFrameCount;
    private VideoEncoderCore mVideoEncoder;

    private volatile EncoderHandler mHandler;

    private final Object mReadyFence = new Object();
    private boolean mReady;
    private boolean mRunning;

    public static class EncoderConfig {
        final File mOutputFile;
        final int mWidth;
        final int mHeight;
        final int mBitRate;
        final EGLContext mEglContext;

        public EncoderConfig(File outputFile, int width, int height, int bitRate, EGLContext sharedEglContext) {
            mOutputFile = outputFile;
            mWidth = width;
            mHeight = height;
            mBitRate = bitRate;
            mEglContext = sharedEglContext;
        }
    }

    public void startRecording(EncoderConfig config) {
        synchronized (mReadyFence) {
            if (mRunning) {
                return;
            }
            mRunning = true;
            new Thread(this, "TextureMovieEncoder").start();
            while (!mReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING, config));
    }

    public void stopRecording() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
        mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
    }

    public void frameAvailable(SurfaceTexture st) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        float[] transform = new float[16];
        st.getTransformMatrix(transform);
        long timestamp = st.getTimestamp();
        if (timestamp == 0) {
            return;
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE,
                (int) (timestamp >> 32), (int) timestamp, transform));
    }

    public void setTextureId(int id) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SET_TEXTURE_ID, id, 0, null));
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new EncoderHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();
        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mHandler = null;
        }
    }


    private static class EncoderHandler extends Handler {
        private final WeakReference<TextureMovieEncoder> mWeakEncoder;

        public EncoderHandler(TextureMovieEncoder encoder) {
            mWeakEncoder = new WeakReference<>(encoder);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            TextureMovieEncoder encoder = mWeakEncoder.get();
            if (encoder == null) {
                return;
            }

            switch (what) {
                case MSG_START_RECORDING:
                    encoder.handleStartRecording((EncoderConfig) obj);
                    break;
                case MSG_STOP_RECORDING:
                    encoder.handleStopRecording();
                    break;
                case MSG_FRAME_AVAILABLE:
                    long timestamp = (((long) inputMessage.arg1) << 32) |
                            (((long) inputMessage.arg2) & 0xffffffffL);
                    encoder.handleFrameAvailable((float[]) obj, timestamp);
                    break;
                case MSG_SET_TEXTURE_ID:
                    encoder.handleSetTexture(inputMessage.arg1);
                    break;
                case MSG_QUIT:
                    Looper.myLooper().quit();
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }

    private void handleStartRecording(EncoderConfig config) {
        prepareEncoder(config.mEglContext, config.mWidth, config.mHeight, config.mBitRate, config.mOutputFile);
    }

    private void handleFrameAvailable(float[] transform, long timestampNanos) {
        mVideoEncoder.drainEncoder(false);
        mFullScreen.drawFrame(mTextureId, transform);
        drawWatermark();
        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();
    }

    private void handleStopRecording() {
        mVideoEncoder.drainEncoder(true);
        releaseEncoder();
    }

    private void handleSetTexture(int id) {
        mTextureId = id;
    }

    private void prepareEncoder(EGLContext sharedContext, int width, int height, int bitRate, File outputFile) {
        try {
            mVideoEncoder = new VideoEncoderCore(width, height, bitRate, outputFile);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mEglCore = new EglCore(sharedContext, EglCore.FLAG_RECORDABLE);
        mInputWindowSurface = new WindowSurface(mEglCore, mVideoEncoder.getInputSurface(), true);
        mInputWindowSurface.makeCurrent();
        mFullScreen = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));
    }

    private void releaseEncoder() {
        mVideoEncoder.release();
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);
            mFullScreen = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    private void drawWatermark() {
        int size = 100;
        int offset = 0;
        if ((++mFrameCount & PULSE_RATE) == 0) {
            size = 120;
            offset = 5;
        }
        final int width = mInputWindowSurface.getWidth();
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(width - 150 - offset, 50 - offset, size, size);
        GLES20.glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }
}
