package com.mona.personalizedtwitter.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;

import com.mona.personalizedtwitter.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import twitter4j.HttpResponse;

/**
 * Created by mona on 1/11/2016.
 */
public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;

    public ImageDownloaderTask(ImageView imageView) {
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    @Override
    // Actual download method, run in the task thread
    protected Bitmap doInBackground(String... params) {
        // params comes from the execute() call: params[0] is the url.
        return downloadBitmap(params[0]);
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null) {

                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageDrawable(imageView.getContext().getResources().getDrawable(R.drawable.avatar));
                }
            }

        }
    }

    static Bitmap downloadBitmap(String url) {
        InputStream in = null;
        int resCode = -1;
        try { URL url_con = new URL(url);
                  URLConnection urlConn = url_con.openConnection();
                  if (!(urlConn instanceof HttpURLConnection)) {
                    throw new IOException("URL is not an Http URL");
                   }
                HttpURLConnection httpConn = (HttpURLConnection) urlConn;
                httpConn.setAllowUserInteraction(false);
                httpConn.setInstanceFollowRedirects(true);
                httpConn.setRequestMethod("GET");
                httpConn.connect();
                resCode = httpConn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    in = httpConn.getInputStream();
                }
                else {
                    Log.w("ImageDownloader", "Error " + resCode
                            + " while retrieving bitmap from " + url);
                    return null;
                }
                final Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();
                return bitmap;

            }
            catch (Exception e)
            {Log.w("ImageDownloader", "Error while retrieving bitmap from " + url);}
            return null;
    }
}