package com.mediagoblin;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;

public class Main extends Activity
{
    private final String defaultServer =
            "http://mediagoblin.com/u/php";
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        ((EditText)findViewById(R.id.serverNameTxt)).setText(settings.getString("server", 
                defaultServer));
        ((EditText)findViewById(R.id.userNameTxt)).setText(settings.getString("username",
                ""));
        ((EditText)findViewById(R.id.passwordTxt)).setText(settings.getString("password",
                ""));

        (findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                final SharedPreferences settingsEdit = PreferenceManager.getDefaultSharedPreferences(Main.this);
                SharedPreferences.Editor edit = settingsEdit.edit();
                edit.putString("server",
                        ((EditText)findViewById(R.id.serverNameTxt)).getText().toString());
                edit.putString("username",
                        ((EditText)findViewById(R.id.userNameTxt)).getText().toString());
                edit.putString("password",
                        ((EditText)findViewById(R.id.passwordTxt)).getText().toString());
                edit.commit();
                finish();
            }
        });
    }
}
