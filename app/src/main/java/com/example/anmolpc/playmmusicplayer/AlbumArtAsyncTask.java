package com.example.anmolpc.playmmusicplayer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.speech.tts.Voice;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Anmol Pc on 4/25/2017.
 */

public class AlbumArtAsyncTask extends AsyncTask<Bitmap,Void,Bitmap> {
    Context mcontext;
    String albumid;
    public AsyncResponse delegate = null;
    public AlbumArtAsyncTask(Context context,String id)
    {
        this.mcontext=context;
        this.albumid=id;
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumid));
        ContentResolver res = mcontext.getContentResolver();
        InputStream in = null;
        try {
            in = res.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options size = new BitmapFactory.Options();
        size.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in,null,size);
        int width= size.outWidth;
        int height=size.outHeight;
        InputStream newin = null;
        try {
            newin = res.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options=new BitmapFactory.Options();
        Bitmap artwork;
        if(height>2000 || width>3000){
            options.inSampleSize=4;
        }else if(height>1000 && height<=2000 || width>2000 && width<3000){
            options.inSampleSize=2;
        }else{
            options.inSampleSize=1;
        }
        artwork = BitmapFactory.decodeStream(newin, null, options);
        return artwork;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        delegate.processFinish(bitmap);
    }
}
