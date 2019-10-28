package com.dcgabriel.mygeocam;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = " CustomInfoWindow";
    private Context context;
    private final View view;

    public CustomInfoWindowAdapter(Context context) {
        this.context = context;
        view = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_info_layout, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView dateTime = view.findViewById(R.id.textview_date);
        TextView latitude = view.findViewById(R.id.textview_latitude);
        TextView longitude = view.findViewById(R.id.textview_longitude);
        ImageView imageView = view.findViewById(R.id.imageview_photo);


        Pic pic = (Pic) marker.getTag();
        String newDate = formatDate(pic);
        Log.d(TAG, "getInfoWindow: " + newDate);

        dateTime.setText(newDate);
        latitude.setText(String.valueOf(pic.getLatitude()));
        longitude.setText(String.valueOf(pic.getLongitude()));

        File imgFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), pic.getTitle());
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
        return view;
    }

    private String formatDate(Pic pic) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String dateString = format.format(new Date());
        Date date = new Date();
        try {
            date = format.parse(pic.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat newFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
        String newDateString = newFormat.format(date);

        return newDateString;
    }

    @Override
    public View getInfoContents(Marker marker) {

        return null;
    }


}
