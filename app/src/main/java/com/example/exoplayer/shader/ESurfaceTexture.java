package com.example.exoplayer.shader;

import android.graphics.SurfaceTexture;

import com.example.exoplayer.shader.filter.GlPreviewFilter;


/**
 * 
 */

class ESurfaceTexture implements SurfaceTexture.OnFrameAvailableListener {

    private final SurfaceTexture surfaceTexture;
    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener;

    ESurfaceTexture(final int texName) {
        surfaceTexture = new SurfaceTexture(texName);
        surfaceTexture.setOnFrameAvailableListener(this);
    }

    void setOnFrameAvailableListener(final SurfaceTexture.OnFrameAvailableListener l) {
        onFrameAvailableListener = l;
    }


    int getTextureTarget() {
        return GlPreviewFilter.GL_TEXTURE_EXTERNAL_OES;
    }

    void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    void getTransformMatrix(final float[] mtx) {
        surfaceTexture.getTransformMatrix(mtx);
    }

    SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
        if (onFrameAvailableListener != null) {
            onFrameAvailableListener.onFrameAvailable(this.surfaceTexture);
        }
    }

    public void release() {
        surfaceTexture.release();
    }
}