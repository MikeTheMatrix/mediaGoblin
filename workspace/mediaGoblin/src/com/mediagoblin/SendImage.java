package com.mediagoblin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

/* the following PHP page will accept this HTTPPost
<?php
$target_path  = "./";
$target_path = $target_path . basename( $_FILES['uploadedfile']['name']);
$target_path =  $_FILES['uploadedfile']['name'];


print_r($_FILES);
echo "<br/>";


if(move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $target_path)) {
 echo "The file ".  basename( $_FILES['uploadedfile']['name']).
 " has been uploaded";
} else{
 echo "There was an error uploading the file, please try again!";
}
?>
 */


public class SendImage extends Activity {
    
    private String serverName;
    private String username;
    private String password;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        serverName = settings.getString("server", "");
        username = settings.getString("username", "");
        password = settings.getString("password", "");
        
        if (serverName.length() == 0 || username.length() == 0 || password.length() ==0){
            final AlertDialog d = new AlertDialog.Builder(SendImage.this).create();
            d.setTitle("Error");
            d.setMessage("Server, Username, or Password is not configured");
            d.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    d.dismiss();
                    finish();
                }
            });
            d.show();
        }
        else {
            Intent intent = getIntent();
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                    String scheme = uri.getScheme();
                    if (scheme.equals("content")) {
//                        String mimeType = intent.getType();
                        ContentResolver contentResolver = getContentResolver();
                        Cursor cursor = contentResolver.query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                        Intent sendIntent = new Intent(this, SendService.class);
                        sendIntent.putExtra("fileName", filePath);
                        Log.d("MGH", "before startService()");
                        startService(sendIntent);
                        finish();

                    }
                }
            }
        }
    }
}
