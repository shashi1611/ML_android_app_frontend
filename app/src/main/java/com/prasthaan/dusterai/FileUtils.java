package com.prasthaan.dusterai;

//public class FileUtils {
//}

//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//import android.provider.MediaStore;
//
//public class FileUtils {
//    public static String getPath(Context context, Uri uri) {
//
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor == null) return null;
//        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        String filePath = cursor.getString(columnIndex);
//        cursor.close();
//        return filePath;
//    }
//}


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    public static String getPath(Context context, Uri uri) {
        String filePath = null;

        // Handle content URI from MediaStore (Gallery)
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        // Handle File URI (from Camera)
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }

        // If the file path is still null, save a copy of the image and return its path
        if (filePath == null) {
            filePath = saveFileFromUri(context, uri);
        }

        return filePath;
    }

    private static String saveFileFromUri(Context context, Uri uri) {
        try {
            File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp_image.jpg");
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("FileUtils", "Error saving file from URI", e);
            return null;
        }
    }


}


