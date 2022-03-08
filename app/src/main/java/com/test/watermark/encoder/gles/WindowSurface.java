package com.test.watermark.encoder.gles;

import android.view.Surface;

public class WindowSurface extends EglSurfaceBase {
    private Surface mSurface;
    private final boolean mReleaseSurface;

    public WindowSurface(EglCore eglCore, Surface surface, boolean releaseSurface) {
        super(eglCore);
        createWindowSurface(surface);
        mSurface = surface;
        mReleaseSurface = releaseSurface;
    }
    public void release() {
        releaseEglSurface();
        if (mSurface != null) {
            if (mReleaseSurface) {
                mSurface.release();
            }
            mSurface = null;
        }
    }
}
