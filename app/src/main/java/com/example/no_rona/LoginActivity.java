package com.example.no_rona;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.no_rona.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class LoginActivity extends AppCompatActivity {

    ConstraintLayout ui;
    RelativeLayout spinner;
    EditText emailEt, passwordEt;
    Button loginBtn;
    TextView signupTv;
    FirebaseAuth mFirebaseAuth;
    String authUid;
    String predecessor;
    User savedUser;

    @Override
    public void onBackPressed() {
        // clear the activity stack to close the app when back pressed in the MainActivity
        Intent a = new Intent(Intent.ACTION_MAIN);
        //TODO: we want to go back to whichever screen was active before we started our app, not specifically home screen
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findUiComponentsIds();

        /*
        See if the SignupActivity is sending an intent
        */
        predecessor = getIntent().getStringExtra(getString(R.string.predecessor_key));

        if(predecessor != null){

            if (predecessor.equals(SignupActivity.class.getName())){
                String savedUid = getIntent().getStringExtra(getString(R.string.uid_key));
                String savedEmail = getIntent().getStringExtra(getString(R.string.current_user_email_key));
                String savedPassword = getIntent().getStringExtra(getString(R.string.current_user_password_key));
                savedUser = new User(savedUid, savedEmail, savedPassword);
                saveAsSharedPreference();
            }
            else if ( (predecessor.equals(AssessmentActivity.class.getName())) || (predecessor.equals(InstructionsActivity.class.getName())) ){
                savedUser = null;
                saveAsSharedPreference();
            }
        }

        loadData(this);
        if(savedUser != null){
            // Going to AssessmentActivity
            showProgress();
            Intent toAssessmentActivity = new Intent(LoginActivity.this, AssessmentActivity.class);
            putIntentExtras(toAssessmentActivity, savedUser.getAuthUid());
            startActivity(toAssessmentActivity);
        }
        // Show normal layout for the activity
        showUi();

        mFirebaseAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailEt.getText().toString().trim();
                final String password = passwordEt.getText().toString();
                if( (email.isEmpty()) && (!password.isEmpty()) ){
                    emailEt.setError(getText(R.string.missing_email));
                    emailEt.requestFocus();
                }
                else if( (!email.isEmpty()) && (password.isEmpty()) ){
                    passwordEt.setError(getText(R.string.missing_password));
                    passwordEt.requestFocus();
                }
                else if( (email.isEmpty()) && (password.isEmpty()) ){
                    Toast.makeText(LoginActivity.this,R.string.empty_fields,Toast.LENGTH_SHORT).show();
                }
                else if( (!email.isEmpty()) && (!password.isEmpty()) ){
                    // Show ProgressBar during authentication
                    showProgress();
                    mFirebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, R.string.wrong_entries_login, Toast.LENGTH_SHORT).show();
                                showUi();
                            }
                            else {
                                authUid = mFirebaseAuth.getUid();
                                savedUser = new User(authUid, email, password);
                                saveAsSharedPreference();
                                Intent toAssessmentActivity = new Intent(LoginActivity.this, AssessmentActivity.class);
                                putIntentExtras(toAssessmentActivity, authUid);
                                startActivity(toAssessmentActivity);
                                showUi();
                            }
                        }
                    });
                }
                else
                    Toast.makeText(LoginActivity.this,R.string.unsuccessful_login_signup,Toast.LENGTH_SHORT).show();
            }
        });

        signupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void findUiComponentsIds(){
        emailEt = findViewById(R.id.login_email);
        passwordEt = findViewById(R.id.login_password);
        loginBtn = findViewById(R.id.login_button);
        signupTv = findViewById(R.id.signup_question);
        ui = findViewById(R.id.login_ui);
        spinner = findViewById(R.id.login_progressbar);
    }

    private void showUi(){
        ui.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);
    }

    private void showProgress(){
        spinner.setVisibility(View.VISIBLE);
        ui.setVisibility(View.INVISIBLE);
    }

    private void saveAsSharedPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedUser);
        editor.putString("savedUser", json);
        editor.apply();
    }

    public void loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("User", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("savedUser", null);
        Type type = new TypeToken<User>() {}.getType();
        savedUser = gson.fromJson(json, type);
    }

    private void putIntentExtras(Intent intent, String uid){
        intent.putExtra(getString(R.string.predecessor_key), LoginActivity.class.getName());
        intent.putExtra(getString(R.string.uid_key), uid);
    }

}
