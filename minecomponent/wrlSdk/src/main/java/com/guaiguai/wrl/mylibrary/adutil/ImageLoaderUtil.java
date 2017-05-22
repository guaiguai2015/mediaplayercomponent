package com.guaiguai.wrl.mylibrary.adutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.guaiguai.wrl.mylibrary.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import io.realm.Realm;

/**
 * Created by WRL on 2017/5/11.
 */
public class ImageLoaderUtil {

    private static final int  THREAD_COUNT = 4;   //表示我们UIL加载图片的线程的数量
    private static final int  PRIORITY = 2;      //加载图片的优先级
    private static final int DISK_CACHE_SIZE = 50 * 1024 *1024;
    private static final int CONNECTION_TIME_OUT = 5 * 1000;
    private static final int READ_TIME_OUT = 30 * 1000;

    private ImageLoader mLoader = null;
    private static ImageLoaderUtil mInstance = null;

    public ImageLoaderUtil getmInstance (Context context){

        if (mInstance == null) {
            synchronized (ImageLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoaderUtil(context);
                }
            }
        }
        return mInstance;
    }

    private ImageLoaderUtil (Context context) {
        ImageLoaderConfiguration configuration =
                new ImageLoaderConfiguration.Builder(context)
                        .threadPoolSize(THREAD_COUNT)
                        .threadPriority(Thread.NORM_PRIORITY - PRIORITY)
                        .denyCacheImageMultipleSizesInMemory()   //禁止缓存多套尺寸
                        .memoryCache(new WeakMemoryCache())    //缓存的类型
                        .diskCacheSize(DISK_CACHE_SIZE)
                        .diskCacheFileNameGenerator( new Md5FileNameGenerator())  //缓存图片的文件命名格式是以MD5的格式命名
                        .tasksProcessingOrder(QueueProcessingType.LIFO)   //文件的形式是先进后出的形式
                        .defaultDisplayImageOptions(getDefaultOptions())  //显示图片的配置
                        .imageDownloader(new BaseImageDownloader(context,CONNECTION_TIME_OUT,READ_TIME_OUT))
                        .writeDebugLogs()
                        .build();

        ImageLoader.getInstance().init(configuration);
        mLoader = ImageLoader.getInstance();
    }

    private DisplayImageOptions getDefaultOptions() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中, 重要，否则图片不会缓存到内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中, 重要，否则图片不会缓存到硬盘中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .decodingOptions(new BitmapFactory.Options())//设置图片的解码配置
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .build();

        return options;
    }

    public DisplayImageOptions getOptionsWithNoCache() {

        DisplayImageOptions options = new
                DisplayImageOptions.Builder()
                //.cacheInMemory(true)//设置下载的图片是否缓存在内存中, 重要，否则图片不会缓存到内存中
                //.cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中, 重要，否则图片不会缓存到硬盘中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .decodingOptions(new BitmapFactory.Options())//设置图片的解码配置
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new FadeInBitmapDisplayer(400))
                .build();
        return options;
    }

    public void displayImage (ImageView imageView,String path) {
        displayImage(imageView,path,null);
    }

    public void displayImage (ImageView imageView, String path, ImageLoadingListener listener) {
        if (mLoader != null) {
            displayImage(imageView,path,listener,null);
        }
    }

    private void displayImage (ImageView imageView,String path,
                               ImageLoadingListener listener,DisplayImageOptions options) {
        if (mLoader != null) {
            displayImage(imageView,path,listener,options);
        }
    }

}
