package de.conradowatz.jkgvertretung.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.pnikosis.materialishprogress.ProgressWheel;
import de.conradowatz.jkgvertretung.R;
import de.conradowatz.jkgvertretung.tools.PreferenceReader;
import de.conradowatz.jkgvertretung.tools.VertretungsAPI;


public class LoginActivity extends AppCompatActivity {

    EditText usernameEdit;
    EditText passwordEdit;
    Button loginButton;
    ProgressWheel loginProgressWheel;
    TextView loginErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        usernameEdit = (EditText) findViewById(R.id.usernameEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        loginButton = (Button) findViewById(R.id.loginButton);
        loginProgressWheel = (ProgressWheel) findViewById(R.id.loginProgressWheel);
        loginErrorText = (TextView) findViewById(R.id.loginErrorText);

        //Wenn Button gedrückt -> eonlogen
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        //Wenn ENTER geklickt wird -> einloggen
        passwordEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    login();
                }
                return false;
            }
        });
    }


    private void login() {
        final String benutzerName = usernameEdit.getText().toString();
        final String passwort = passwordEdit.getText().toString();

        loginButton.setVisibility(View.INVISIBLE);
        loginProgressWheel.setVisibility(View.VISIBLE);
        loginErrorText.setVisibility(View.INVISIBLE);
        passwordEdit.setEnabled(false);
        usernameEdit.setEnabled(false);

        new VertretungsAPI().checkLogin(benutzerName, passwort, new VertretungsAPI.AsyncLoginResponseHandler() {

            @Override
            public void onLoggedIn() {

                PreferenceReader.saveStringToPreferences(getApplicationContext(), "username", benutzerName);
                PreferenceReader.saveStringToPreferences(getApplicationContext(), "password", passwort);

                //Anwendung schließen
                Intent backToMain = new Intent();
                backToMain.putExtra("ExitCode", "LoggedIn");
                setResult(RESULT_OK, backToMain);
                finish();

            }

            @Override
            public void onLoginFailed(Throwable throwable) {
                throwable.printStackTrace();

                loginButton.setVisibility(View.VISIBLE);
                loginProgressWheel.setVisibility(View.INVISIBLE);
                passwordEdit.setEnabled(true);
                usernameEdit.setEnabled(true);
                loginErrorText.setVisibility(View.VISIBLE);
                loginErrorText.setText("Fehler bei der Anmeldung!");
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Anwendung schließen
        Intent backToMain = new Intent();
        backToMain.putExtra("ExitCode", "Exit");
        setResult(RESULT_OK, backToMain);
        finish();
    }
}
