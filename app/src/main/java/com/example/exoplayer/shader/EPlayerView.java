package com.example.exoplayer.shader;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import androidx.media3.common.VideoSize;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.video.VideoRendererEventListener;
import androidx.media3.ui.PlayerView;

import com.example.exoplayer.shader.chooser.EConfigChooser;
import com.example.exoplayer.shader.contextfactory.EContextFactory;
import com.example.exoplayer.shader.filter.GlFilter;

@UnstableApi
public class EPlayerView extends GLSurfaceView implements VideoRendererEventListener {

    private final static String TAG = EPlayerView.class.getSimpleName();

    private final EPlayerRenderer renderer;
    private ExoPlayer player;

    private float videoAspect = 1f;
    private PlayerScaleType playerScaleType = PlayerScaleType.RESIZE_FIT_WIDTH;

    public EPlayerView(Context context) {
        this(context, null);
    }

    public EPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextFactory(new EContextFactory());
        setEGLConfigChooser(new EConfigChooser());

        renderer = new EPlayerRenderer(this);
        setRenderer(renderer);
    }

    public EPlayerView setExoPlayer(ExoPlayer player) {
        if (this.player != null) {
            this.player.release();
            this.player = null;
        }
        this.player = player;
        this.renderer.setSimpleExoPlayer(player);

        PlayerView playerView;

        return this;
    }

    public void setGlFilter(GlFilter glFilter) {
        renderer.setGlFilter(glFilter);
    }

    public GlFilter getGlFilter() {
        return renderer.getGlFilter();
    }

    public void setPlayerScaleType(PlayerScaleType playerScaleType) {
        this.playerScaleType = playerScaleType;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        int viewWidth = measuredWidth;
        int viewHeight = measuredHeight;

        switch (playerScaleType) {
            case RESIZE_FIT_WIDTH:
                viewHeight = (int) (measuredWidth / videoAspect);
                break;
            case RESIZE_FIT_HEIGHT:
                viewWidth = (int) (measuredHeight * videoAspect);
                break;
        }

        // Log.d(TAG, "onMeasure viewWidth = " + viewWidth + " viewHeight = " + viewHeight);

        setMeasuredDimension(viewWidth, viewHeight);

    }

    @Override
    public void onPause() {
        super.onPause();
        renderer.release();
    }

    @Override
    public void onVideoSizeChanged(VideoSize videoSize) {
        VideoRendererEventListener.super.onVideoSizeChanged(videoSize);
        videoAspect = ((float) videoSize.width / videoSize.height) * videoSize.pixelWidthHeightRatio;
        requestLayout();
    }
}
