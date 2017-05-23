package com.guaiguai.wrl.mylibrary.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.guaiguai.wrl.mylibrary.R;
import com.guaiguai.wrl.mylibrary.adutil.Utils;
import com.guaiguai.wrl.mylibrary.constant.SDKConstant;
import com.guaiguai.wrl.mylibrary.core.AdParameters;

/**
 * Created by wei on 2017/5/18.
 */

public class CustomVideoView extends RelativeLayout implements View.OnClickListener,
        TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener {

    /**
     * 常量
     */
    private static final int TIME_MSG = 0x01;
    private static final int TIME_INVAL = 1000;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int LOAD_TOTAL_TIME = 3;  //加载的最多的次数

    /**
     * UI
     */
    private ViewGroup mParentContainer;
    private RelativeLayout mPlayerView;
    private TextureView mVideoView;
    private Button mMiniPlayBtn;
    private ImageView mFullBtn;
    private ImageView mLoadingBar;
    private ImageView mFrameView;
    private AudioManager audioManager;   //音量控制器
    private Surface videoSurface;

    /**w
     * Data
     */
    private String mUrl;     //加载视频地址
    private String mFrameURI;
    private boolean isMute;   //是否静音
    private int mScreenWidth, mDestationHeight;

    /**
     * Status状态保护
     */
    private boolean canPlay = true;
    private boolean mIsRealPause;    //是不是手动进行暂停的
    private boolean mIsComplete;     //是不是自己播放完成的吗
    private int mCurrentCount;
    private int playerState = STATE_IDLE;

    private MediaPlayer mediaPlayer;
    private ADVideoPlayerListener listener;   //通知外界 需要做什么事，就是一个监听器
    private ScreenEventReceiver mScreenReceiver;     //解锁的监听

    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TIME_MSG:
                    if (isPlaying()) {
                        //还可以在这里更新progressbar
                        listener.onBufferUpdate(getCurrentPosition());
                        sendEmptyMessageDelayed(TIME_MSG, TIME_INVAL);
                    }
                    break;
            }
        }
    };

    public CustomVideoView(Context context,ViewGroup parentContainer) {
        super(context);
        this.mContext = context;
        mParentContainer = parentContainer;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        initData();
        initView();
        registerBroadcastReceiver();
    }

    private void initData() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mDestationHeight = ((int) (mScreenWidth * SDKConstant.VIDEO_HEIGHT_PERCENT));

    }

    private void initView() {
        mPlayerView  = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.xadsdk_video_player,this);
        mVideoView = (TextureView) mPlayerView.findViewById(R.id.xadsdk_player_video_textureView);
        mVideoView.setOnClickListener(this);
        mVideoView.setSurfaceTextureListener(this);
        mVideoView.setKeepScreenOn(true);      //暂时理解为设置屏幕常量
        initSmallLayoutMode();
    }

    /**
     * 初始化一些小控件的布局
     */
    private void initSmallLayoutMode() {

        LayoutParams params = new LayoutParams(mScreenWidth,mDestationHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPlayerView.setLayoutParams(params);

        mMiniPlayBtn = (Button) mPlayerView.findViewById(R.id.xadsdk_small_play_btn);
        mFullBtn = (ImageView) mPlayerView.findViewById(R.id.xadsdk_to_full_view);
        mLoadingBar = (ImageView) mPlayerView.findViewById(R.id.loading_bar);
        mFrameView = (ImageView) mPlayerView.findViewById(R.id.framing_view);

        mMiniPlayBtn.setOnClickListener(this);
        mFullBtn.setOnClickListener(this);

    }

    /**
     * 进行加载视频
     */
    private void load () {
        if (this.playerState != STATE_IDLE) {   //如果现在的播放状态不是空闲状态的话就return
            return;
        }
        //加载的过程中动画
        showLoadingView();
        try{
            //加载的过程中 你需要将你的播放器的状态设置为空闲状态，进行还原的处理
            //重新设置播放状态
            setCurrentPlayState(STATE_IDLE);
            //创建播放器
            checkMediaPlayer();
            //让播放器静音
            mute(true);
            mediaPlayer.setDataSource(this.mUrl);
            mediaPlayer.prepareAsync();  //异步加载视频
        }catch (Exception e){
            stop();
        }

    }

    /**
     * 设置播放器的声音 为静音
     * @param mute
     */
    public void mute(boolean mute) {
        isMute = mute;
        if(mediaPlayer != null && audioManager != null) {
            float volume = isMute ? 0.0f : 1.0f;
            //设置mediaPlayer的声音
            mediaPlayer.setVolume(volume,volume);
        }

    }

    private synchronized  void checkMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = createMediaPlayer();
        }

    }

    private MediaPlayer createMediaPlayer() {
        //将所有的mediaPlayer 都进行还原
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();

        mediaPlayer.setOnPreparedListener(this);   //加载的时候的监听
        mediaPlayer.setOnErrorListener(this);     //对错误的进行监听
        mediaPlayer.setOnCompletionListener(this);   //完成之后的监听
        mediaPlayer.setOnInfoListener(this);        //
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        //判断surface是否为空，并且可用
        if (videoSurface != null && videoSurface.isValid()) {
            mediaPlayer.setSurface(videoSurface);
        }else {
            stop();
        }

        return mediaPlayer;
    }

    /**
     * 清空播放器而且去重试
     */
    private void stop() {

        //相当于清空所有的mediaplayer所有的东西
        if (this.mediaPlayer != null) {
            //重置播放器
            this.mediaPlayer.reset();
            //设置播放器的进度条的追踪  停止发送消息  停止一切之前设置过这个监听器的一切的事务
            this.mediaPlayer.setOnSeekCompleteListener(null);
            //停止mediPlayer
            this.mediaPlayer.stop();
            //释放mediaplayer的缓存
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        //移除handler中的所有的回调和消息
        mHandler.removeCallbacksAndMessages(null);
        //设置播放器的状态
        setCurrentPlayState(STATE_IDLE);
        if (mCurrentCount < LOAD_TOTAL_TIME) {
            mCurrentCount++;
            load();
        }else {
            showPauseView(false);
        }


    }

    //播放时候的view的存在情况
    private void showPlayView (){
        mLoadingBar.clearAnimation();
        mLoadingBar.setVisibility(View.GONE);
        mMiniPlayBtn.setVisibility(View.GONE);
        mFrameView.setVisibility(View.GONE);
    }

    /**
     * 展示暂停的时候的布局
     * 如果为真 则 全屏的按钮 可见，  播放按钮的消失
     * 如果为假 则 全屏的按钮 不可见   播放按钮可见
     * @param show
     */
    private void showPauseView(boolean show) {
        mFullBtn.setVisibility(show ? View.VISIBLE : View.GONE);
        mMiniPlayBtn.setVisibility(show ? View.GONE : View.VISIBLE);

        mLoadingBar.clearAnimation();
        mLoadingBar.setVisibility(View.GONE);

        if (!show) {
            mFrameView.setVisibility(View.VISIBLE);
            loadFrameImage();
        }else {
            mFrameView.setVisibility(View.GONE);
        }
    }

    /**
     * 设置当前的播放状态
     * @param stateIdle
     */
    private void setCurrentPlayState(int stateIdle) {
        this.playerState = stateIdle;
    }


    /**
     * 在加载的过程中的其他布局的展现
     */
    private void showLoadingView() {
        mFullBtn.setVisibility(View.GONE);
        mFrameView.setVisibility(View.GONE);
        mMiniPlayBtn.setVisibility(View.GONE);

        mLoadingBar.setVisibility(View.VISIBLE);
        AnimationDrawable ad = (AnimationDrawable) mLoadingBar.getBackground();
        ad.start();

        loadFrameImage();

    }



    /**
     * 异步加载异帧图
     * 这个就相当于一个网络进行加载的过程中你显示的图片给用户来看，
     * 有的话就行服务器给予的图片
     * 如果没有的话就是加载你本地所有的默认图片
     */
    private void loadFrameImage() {
        if (mFrameLoadListener != null) {
            mFrameLoadListener.onStartFrameLoad(mFrameURI, new ImageLoaderListener() {
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    if (loadedImage != null) {
                        mFrameView.setScaleType(ImageView.ScaleType.FIT_XY);
                        mFrameView.setImageBitmap(loadedImage);
                    } else {
                        mFrameView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        mFrameView.setImageResource(R.drawable.xadsdk_img_error);
                    }
                }
            });
        }

    }

    public void destroy() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        setCurrentPlayState(STATE_IDLE);
        mCurrentCount = 0;
        setIsComplete(false);
        setIsRealPause(false);
        unRegisterBroadcastReceiver();
        mHandler.removeCallbacksAndMessages(null); //release all message and runnable
        showPauseView(false); //除了播放和loading外其余任何状态都显示pause
    }

    @Override
    public void onClick(View v) {
        if (v == this.mMiniPlayBtn) {
            if (this.playerState == STATE_PAUSING) {
                if (Utils.getVisiblePercent(mParentContainer)
                        > SDKConstant.VIDEO_SCREEN_PERCENT) {
                    resume();
                    this.listener.onClickPlay();
                }
            } else {
                load();
            }
        } else if (v == this.mFullBtn) {
            this.listener.onClickFullScreenBtn();
        } else if (v == mVideoView) {
            this.listener.onClickVideo();
        }

    }


    /**
     * 这个是TextureView的监听器跟MediaPLayer无关
     * 确定textureview是否已经准备完毕
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        videoSurface = new Surface(surface);
        checkMediaPlayer();
        mediaPlayer.setSurface(videoSurface);
        load();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /**
     * 播放器处于播放状态的时候
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        showPlayView();
        mediaPlayer = mp;
        if (mediaPlayer != null) {
            //设置mediaplayer的缓冲监听器
            mediaPlayer.setOnBufferingUpdateListener(this);
            //设置加载的次数为0次
            mCurrentCount = 0;
            //这只是一个外界的回调函数，通知外界做什么改变
            if (listener != null) {
               listener.onAdVideoLoadSuccess();
            }

            //两个判断条件
            //第一个判断条件是 判断当前网络是个什么状态
            //第二个判断条件是  判断当前的item的超过来屏幕来50%没有
            if (Utils.canAutoPlay(mContext, AdParameters.getCurrentSetting())
                    && Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT) {
                //开始播放 设置当前的播放状态是暂停状态
                setCurrentPlayState(STATE_PAUSING);
                resume();
            }else {
                //暂停
                setCurrentPlayState(STATE_PLAYING);
                pause();
            }

        }

    }

    /**
     * 暂停
     */
    private void pause() {
        if (playerState != STATE_PLAYING) {
            return;
        }
        //重新设置播放的状态
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            mediaPlayer.pause();
            if (!this.canPlay) {
                mediaPlayer.seekTo(0);
            }
        }
        //展示暂停的界面布局
        this.showPauseView(false);
        //移除掉所有的消息信息
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 重新开始播放
     */
    public void resume() {

        if (playerState != STATE_PAUSING) {
            return;
        }
        if (!isPlaying()) {   //当前没有在播放
            entryResumeState();
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.start();
            mHandler.sendEmptyMessage(TIME_MSG);
            showPauseView(true);
        }else {    //当前在播放
            showPauseView(false);
        }
    }

    /**
     * 进入播放状态的更新
     */
    private void entryResumeState() {
        canPlay = true;
        //设置当前播放状态为播放状态
        setCurrentPlayState(STATE_PLAYING);
        //没有真正的暂停
        setIsRealPause(false);
        //没有真正的播放完毕
        setIsComplete(false);

    }

    public void isShowFullBtn(boolean isShow) {
        mFullBtn.setImageResource(isShow ? R.drawable.xadsdk_ad_mini : R.drawable.xadsdk_ad_mini_null);
        mFullBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setIsComplete(boolean isRealComplete) {
        this.mIsComplete = isRealComplete;
    }

    public void setIsRealPause(boolean isRealPause) {
        this.mIsRealPause = isRealPause;
    }

    public boolean isRealPause() {
        return mIsRealPause;
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    public boolean isFrameHidden() {
        return mFrameView.getVisibility() == View.VISIBLE ? false : true;
    }


    /**
     * 判断当前mediaPlayer是否进行来播放
     * @return
     */
    public boolean isPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        this.playerState = STATE_ERROR;
        mediaPlayer = mp;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }
        if (mCurrentCount >= LOAD_TOTAL_TIME) {
            showPauseView(false);
            if (this.listener != null) {
                listener.onAdVideoLoadFailed();
            }
        }
        this.stop();//去重新load
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (listener != null) {
            listener.onAdVideoLoadComplete();
        }
        playBack();
        setIsComplete(true);
        setIsRealPause(true);

    }

    /**
     * 播放完成回到初始状态
     */
    private void playBack() {
        setCurrentPlayState(STATE_PAUSING);
        mHandler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();
        }
        this.showPauseView(false);
    }

    //全屏不显示暂停状态,后续可以整合，不必单独出一个方法
    public void pauseForFullScreen() {
        if (playerState != STATE_PLAYING) {
            return;
        }
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            mediaPlayer.pause();
            if (!this.canPlay) {
                mediaPlayer.seekTo(0);
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public int getCurrentPosition() {
        if (this.mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    //跳到指定点播放视频
    public void seekAndResume(int position) {
        if (mediaPlayer != null) {
            showPauseView(true);
            entryResumeState();
            mediaPlayer.seekTo(position);
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mediaPlayer.start();
                    mHandler.sendEmptyMessage(TIME_MSG);
                }
            });
        }
    }

    //跳到指定点暂停视频
    public void seekAndPause(int position) {
        if (this.playerState != STATE_PLAYING) {
            return;
        }
        showPauseView(false);
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            //这个跳转的完成是不定时的
            mediaPlayer.seekTo(position);
            //跳转事件的监听  就是seekTo的监听
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mediaPlayer.pause();
                    mHandler.removeCallbacksAndMessages(null);
                }
            });
        }
    }

    //这是view显示发生变化的时候监听，是任何view都有的方法
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && playerState == STATE_PAUSING) {
            if (isRealPause() || isComplete()) {
                pause();
            } else {
                decideCanPlay();
            }
        } else {
            pause();
        }
    }

    private void registerBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenEventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            getContext().registerReceiver(mScreenReceiver, filter);
        }
    }

    private void unRegisterBroadcastReceiver() {
        if (mScreenReceiver != null) {
            getContext().unregisterReceiver(mScreenReceiver);
        }
    }

    private void decideCanPlay() {
        if (Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT)
            //来回切换页面时，只有 >50,且满足自动播放条件才自动播放
            resume();
        else
            pause();
    }

    /**
     * 监听锁屏事件的广播接收器
     */
    private class ScreenEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //主动锁屏时 pause, 主动解锁屏幕时，resume
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    if (playerState == STATE_PAUSING) {
                        if (mIsRealPause) {
                            //手动点的暂停，回来后还暂停
                            pause();
                        } else {
                            decideCanPlay();
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (playerState == STATE_PLAYING) {
                        pause();
                    }
                    break;
            }
        }
    }

    /**
     * 再有错误或者警告的时候调用
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    public void setDataSource(String url) {
        this.mUrl = url;
    }

    public void setFrameURI(String url) {
        mFrameURI = url;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    //直接消耗触摸事件，防止和父控件发生冲突
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return true;
    }



    private ADFrameImageLoadListener mFrameLoadListener;

    public void setListener(ADVideoPlayerListener listener) {
        this.listener = listener;
    }

    public void setFrameLoadListener(ADFrameImageLoadListener frameLoadListener) {
        this.mFrameLoadListener = frameLoadListener;
    }
    /**
     * VideoPLayer的播放器的监听
     * 供slot层来实现具体点击逻辑,具体逻辑还会变，
     * 如果对UI的点击没有具体监测的话可以不回调
     */
    public interface ADVideoPlayerListener {

        public void onBufferUpdate(int time);

        public void onClickFullScreenBtn();

        public void onClickVideo();

        public void onClickBackBtn();

        public void onClickPlay();

        public void onAdVideoLoadSuccess();

        public void onAdVideoLoadFailed();

        public void onAdVideoLoadComplete();
    }


    /**
     * 这个是一个加载过程中的等待页的监听器
     */
    public interface ADFrameImageLoadListener {

        void onStartFrameLoad(String url, ImageLoaderListener listener);
    }

    public interface ImageLoaderListener {
        /**
         * 如果图片下载不成功，传null
         *
         * @param loadedImage
         */
        void onLoadingComplete(Bitmap loadedImage);
    }


}