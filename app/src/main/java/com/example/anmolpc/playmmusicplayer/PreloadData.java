package com.example.anmolpc.playmmusicplayer;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.ArraySet;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.widget.ImageView;


import com.example.anmolpc.playmmusicplayer.fragments.AlbumObject;
import com.example.anmolpc.playmmusicplayer.fragments.ArtistObject;
import com.example.anmolpc.playmmusicplayer.fragments.PreloadObject;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static android.os.Environment.isExternalStorageRemovable;
import static com.example.anmolpc.playmmusicplayer.playeractivity.sArtworkUri;

/**
 * Created by Anmol Pc on 4/22/2017.
 */

public class PreloadData{

    private static Context mContext;
    public static DiskLruCache mDiskLruCache;
    private static final Object mDiskCacheLock = new Object();
    private static boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 100;
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    public static LruCache<String, Bitmap> imageCache;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int sizeDiv=8;
    final static int io_buffer=8*1024;


    public PreloadData(Context Context) {
        mContext=Context;
        final int cacheSize = maxMemory/sizeDiv;
        try {
            final File diskCacheDir = getDiskCacheDir(mContext, DISK_CACHE_SUBDIR );
            File[] contents = diskCacheDir.listFiles();
            if (contents.length>=0) {
                for(File file: contents)
                    if (!file.isDirectory())
                        file.delete();
            }
            mDiskLruCache = DiskLruCache.open( diskCacheDir, 1, 1, DISK_CACHE_SIZE );
            mDiskCacheStarting = false; // Finished initialization
            mDiskCacheLock.notifyAll();
        } catch (Exception e) {
            e.printStackTrace();
        }


        imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);

                final String Key=key;
                final Bitmap OldValue=oldValue;
                new Thread(new Runnable() {
                    public void run() {
                        DiskLruCache.Editor editor = null;
                        try {
                            editor = mDiskLruCache.edit(Key);
                            if (editor != null) {
                                BufferedOutputStream out = new BufferedOutputStream(editor.newOutputStream(0),io_buffer);
                                OldValue.compress(Bitmap.CompressFormat.JPEG,85,out);
                                out.close();
                                editor.commit();
                                Log.e("added",Key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }};
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Utils.isExternalStorageRemovable() ?
                        Utils.getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }





    public void  preloadData()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] projection = new String[] {MediaStore.Audio.Albums._ID};
                final Cursor cursor =mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null,null,null);

                synchronized (mContext) {
                    while (cursor.moveToNext()) {
                        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                        Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(cursor.getString(0)));
                        ContentResolver res = mContext.getContentResolver();
                        InputStream in = null;
                        try {
                            in = res.openInputStream(uri);
                            BitmapFactory.Options size = new BitmapFactory.Options();
                            size.inJustDecodeBounds = true;
                            BitmapFactory.decodeStream(in, null, size);
                            int width = size.outWidth;
                            int height = size.outHeight;
                            InputStream newin = null;
                            try {
                                newin = res.openInputStream(uri);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            Bitmap artwork;
                            if (height > 2000 || width > 3000) {
                                options.inSampleSize = 4;
                            } else if (height > 1000 && height <= 2000 || width > 2000 && width < 3000) {
                                options.inSampleSize = 3;
                            } else {
                                options.inSampleSize = 2;
                            }
                            artwork = BitmapFactory.decodeStream(newin, null, options);
                            imageCache.put(cursor.getString(0), artwork);

                        } catch (FileNotFoundException e) {
                        }
                    }
                }
            }
        }).start();
    }

    public static boolean checkCacheAvail()
    {
        return !(imageCache == null && mDiskLruCache == null);
    }

    public static void deleteCache() throws Exception {
        if(!checkCacheAvail())
        {
            imageCache.evictAll();
            mDiskLruCache.wait(500);
            mDiskLruCache.delete();
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        final String Key=key;
        Bitmap getBitmap = imageCache.get(key);
        if (getBitmap == null)
        {
            DiskLruCache.Snapshot snapshot;
            try
            {
                snapshot = mDiskLruCache.get(key);
                if ( snapshot == null ) {
                    return null;
                }
                final InputStream input = snapshot.getInputStream( 0 );
                if ( input != null ) {
                    BufferedInputStream buffered = new BufferedInputStream( input, io_buffer);
                    getBitmap = BitmapFactory.decodeStream(buffered);
                    imageCache.put(Key,getBitmap);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mDiskLruCache.remove(Key);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return getBitmap;
    }
}
