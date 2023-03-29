package com.example.exoplayer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.PlayerView
import com.example.exoplayer.shader.EPlayerView
import com.example.exoplayer.shader.filter.GlBoxBlurFilter
import com.example.exoplayer.shader.filter.GlBrightnessFilter
import com.example.exoplayer.shader.filter.GlFilter
import com.example.exoplayer.shader.filter.GlFilterGroup
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class VideoFragment : Fragment(R.layout.fragment_video) {
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null

    private var videoId = ""
    private var useToggle = false
    private var currentMediaItem = 0
    private var playbackPosition = 0L
    private var systemBarBehavior = 0

    private val timelineObservable: Observable<Long> = Observable.interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .map { player?.currentPosition ?: -1L }
    private var compositeDisposable = CompositeDisposable()

    private val actionsCached = mutableListOf<DataEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        videoId = requireArguments().getString(ARG_VIDEO_ID, "")
        useToggle = requireArguments().getBoolean(ARG_USE_TOGGLE, false)
        return super.onCreateView(inflater, container, savedInstanceState)?.let { view ->
            playerView = view.findViewById(R.id.player_view)

            view.isFocusableInTouchMode = true
            view.requestFocus()
            view.setOnKeyListener { v, keyCode, event ->
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    activity?.onBackPressed()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            view
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        currentMediaItem = savedInstanceState?.getInt(VIEW_ARG_MEDIA) ?: 0
        playbackPosition = savedInstanceState?.getLong(VIEW_ARG_POSITION) ?: 0L
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable = CompositeDisposable()
        initializePlayer()
        hideSystemUi()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(VIEW_ARG_MEDIA, currentMediaItem)
        outState.putLong(VIEW_ARG_POSITION, playbackPosition)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
        showSystemUi()
        compositeDisposable.dispose()
        compositeDisposable.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(requireActivity()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(requireActivity())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                val mediaItem = MediaItem.Builder()
                    .setUri("https://c6e8-178-70-176-177.eu.ngrok.io/static/${videoId}.mpd")
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.seekTo(currentMediaItem, playbackPosition)
                exoPlayer.addAnalyticsListener(EventLogger("[MTS_EXO_PLAYER_ANALYTICS]"))
                exoPlayer.prepare()
            }

        val shaderView = EPlayerView(requireActivity())
        shaderView.setExoPlayer(player)
        shaderView.glFilter = GlFilterGroup(hashSetOf(GlFilter()))
        val contentFrame: FrameLayout = playerView.findViewById(androidx.media3.ui.R.id.exo_content_frame)
        contentFrame.addView(shaderView)
        playerView.player = player

        loadConfig()

        // observe time - lower level
        timelineObservable.subscribeBy { currentPositionMs ->
            val actionsDto: Set<VideoActionsDTO> = actionsForCurrentTime(currentPositionMs, actionsCached)
            // 2. map to internal actions
            val actionsUI: List<UIAction> = actionsDto
                .map {
                    when (it) {
                        VideoActionsDTO.SCRIMER -> UIAction.Alert(String())
                        VideoActionsDTO.BLUR -> UIAction.Shader(UIShader.BLUR)
                        VideoActionsDTO.LOWER_CONTRAST -> UIAction.Shader(UIShader.BRIGHTNESS)
                        VideoActionsDTO.LOWER_SATURATION -> UIAction.Shader(UIShader.BRIGHTNESS)
                    }
                }
            val shadersInternal: List<GlFilter> = actionsUI.filterIsInstance(UIAction.Shader::class.java)
                .map {
                    when (it.shader) {
                        UIShader.BLUR -> GlBoxBlurFilter()
                        UIShader.BRIGHTNESS -> GlBrightnessFilter().apply { setBrightness(-0.40f) }
                    }
                }.toMutableList().apply { add(GlFilter()) }

            val currentInternalShaders = (shaderView.glFilter as GlFilterGroup)
            if (!currentInternalShaders.filters.containsAll(shadersInternal) || !shadersInternal.containsAll(currentInternalShaders.filters)) {
                if (shadersInternal.isEmpty()) {
                    shaderView.glFilter = GlFilterGroup(hashSetOf(GlFilter()))
                } else {
                    currentInternalShaders.clear()
                    currentInternalShaders.addFilters(shadersInternal)
                    shaderView.glFilter = currentInternalShaders
                }
            }
        }.addTo(compositeDisposable)
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            currentMediaItem = exoPlayer.currentMediaItemIndex
            playbackPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
        player = null
    }

    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, playerView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(requireActivity().window, playerView).let { controller ->
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = systemBarBehavior
        }
    }

    enum class UIShader {
        BLUR,
        BRIGHTNESS
    }
    sealed class UIAction {
        data class Alert(val message: String) : UIAction()
        data class Shader(val shader: UIShader) : UIAction()
    }

    private fun actionsForCurrentTime(currentTime: Long, actions: List<DataEntity>): Set<VideoActionsDTO> {
        if (actions.isEmpty()) {
            return emptySet()
        }

        val result = mutableSetOf<VideoActionsDTO>()
        actions.forEach { action ->
            val startTimeMs = TimeUnit.SECONDS.toMillis(action.startTime)
            val endTimeMs = TimeUnit.SECONDS.toMillis(action.endTime)
            if (currentTime in (startTimeMs..endTimeMs)) {
                result.addAll(action.actions)
            }
        }
        return result
    }

    private fun loadConfig() {
        if (!useToggle || actionsCached.isNotEmpty()) {
            player?.playWhenReady = true
            return
        }

        retrofit.getConfig(videoId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                player?.playWhenReady = true
                this.actionsCached.clear()
                this.actionsCached.addAll(it.data)
            }.addTo(compositeDisposable)
    }

    companion object {
        private const val VIEW_ARG_MEDIA = "view_arg_media"
        private const val VIEW_ARG_POSITION = "view_arg_position"
        private const val ARG_VIDEO_ID = "arg_video"
        private const val ARG_USE_TOGGLE = "arg_toggle"

        fun getInstance(videoId: String, useToggle: Boolean): VideoFragment {
            return VideoFragment().apply {
                arguments = Bundle(2).apply {
                    putString(ARG_VIDEO_ID, videoId)
                    putBoolean(ARG_USE_TOGGLE, useToggle)
                }
            }
        }
    }
}