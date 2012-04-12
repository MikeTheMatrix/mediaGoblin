package com.mediagoblin;

import android.app.*;
import android.content.*;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URL;

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


public class SendService extends Service {

    private ProgressDialog progress;
    private String serverName;
    private String username;
    private String password;

    private Notification notification;
    private NotificationManager notificationManager;

    @Override
    public void onCreate(){
        super.onCreate();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int we){
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        serverName = settings.getString("server", "");
        username = settings.getString("username", "");
        password = settings.getString("password", "");

        if (serverName.length() == 0 || username.length() == 0 || password.length() ==0){
//            d.setMessage("Server, Username, or Password is not configured");
        }


        if (intent != null){
            String fileName = intent.getStringExtra("fileName");
            NotificationManager nm = notificationManager;
            Notification notif = new Notification(R.drawable.mgicon, "Uploading...", System.currentTimeMillis());
            notification = notif;
            notif.setLatestEventInfo(this, "Uploading...", getMessage2(fileName, serverName),
                    PendingIntent.getActivity(this, 0, new Intent(), 0));
            notif.flags = Notification.FLAG_AUTO_CANCEL;
            nm.notify(0, notif);

            new FilePoster().execute(fileName);
        }

        return START_NOT_STICKY;
    }


    private class FilePoster extends AsyncTask<String, Void, String> {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;

        protected String doInBackground(String... filename) {
            String ret = "";
            pathToOurFile = filename[0];
            Log.d("MGH", pathToOurFile);

            try{
                FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
                // FileInputStream fileInputStream = ctx.openFileInput( pathToOurFile );

                URL url = new URL(serverName);
                connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs & Outputs
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Enable POST method
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);


                outputStream = new DataOutputStream( connection.getOutputStream() );
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                Log.d("MGH", Integer.toString(serverResponseCode));
                Log.d("MGH", serverResponseMessage);

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                if (serverResponseCode == HttpURLConnection.HTTP_OK){
                    ret = "OK";
                }
                else {
                    ret = serverResponseMessage;
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
                ret = e.getMessage();
            }

            return ret;
        }

        protected void onPostExecute(String result) {
            String msg;
            if (result == null)
                msg = "Something went wrong: null result";
            else if (!result.equals("OK")){
                msg = "Something went wrong. "+ result;
            }
            else {
                msg = "Your media was uploaded";
            }

            notification.setLatestEventInfo(SendService.this, msg, getMessage2(pathToOurFile, serverName),
                    PendingIntent.getActivity(SendService.this, 0, new Intent(), 0));
            notification.tickerText = msg;
            notificationManager.notify(0, notification);

            if (progress != null){
                progress.dismiss();
            }
            if (result != null && result.equals("OK")){
//                finish();
            }
            else {
//                d.setTitle("Error");
//                d.setMessage(result);
            }
        }
    }

    private static String getMessage2(String path, String server){
        return path.substring(path.lastIndexOf("/")) + " to " +
                server.replace("http://", "").replace("https://", "");

    }
}
