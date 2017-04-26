package space.mstack.digitalsignage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {

    private EditText editText_url;
    private EditText editText_website;
    private Button button_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        editText_url = (EditText) findViewById(R.id.editText_url);
        editText_website = (EditText) findViewById(R.id.editText_website);
        button_save = (Button) findViewById(R.id.button_save);

        getConfigurations();

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConfigurations();

                finish();
            }
        });
    }

    private void getConfigurations() {
        SharedPreferences myPrefs = this.getSharedPreferences("SIGNAGE_PREF", Context.MODE_PRIVATE);

        editText_url.setText(myPrefs.getString("_signage_url", ""));
        editText_website.setText(myPrefs.getString("_signage_web", ""));
    }

    private void setConfigurations() {
        SharedPreferences myPrefs = this.getSharedPreferences("SIGNAGE_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();

        prefsEditor.putString("_signage_url", editText_url.getText().toString());
        prefsEditor.putString("_signage_web", editText_website.getText().toString());

        prefsEditor.apply();
        prefsEditor.commit();
    }
}
