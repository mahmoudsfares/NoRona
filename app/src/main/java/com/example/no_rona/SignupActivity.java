package com.example.no_rona;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.no_rona.apis.SendMail;
import com.example.no_rona.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.Random;

public class SignupActivity extends AppCompatActivity {

    // Normal layout for the activity
    ConstraintLayout ui;
    // ProgressBar layout for when the user creation and pushing is in progress
    RelativeLayout spinner;
    EditText emailEt, passwordEt, confirmPasswordEt, nameEt, idnoEt, addressEt, mobileEt;
    String email, password, confirmPassword, name, idno, address, mobile;
    Button signupBtn;
    TextView signinTv;
    // Newly created user object
    User newUser;
    // Firebase instance for authentication
    FirebaseAuth mFirebaseAuth;
    // Authorization uid that will be used to save data in the DB
    String authUid;
    // Newly signed up email verification number
    int verificationRandomNumber;
    // Firebase reference for the app database
    DatabaseReference mDbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ui = findViewById(R.id.signup_ui);
        spinner = findViewById(R.id.signup_progressbar);
        // Show normal layout for the activity
        showUi();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDbReference = FirebaseDatabase.getInstance().getReference().child("Users");

        findUiComponentsIds();

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                extractStringsFromEditTexts();

                if(email.isEmpty()){
                    emailEt.setError(getText(R.string.missing_email));
                    emailEt.requestFocus();
                }
                if(password.isEmpty()){
                    passwordEt.setError(getText(R.string.missing_password));
                    passwordEt.requestFocus();
                }
                else if(confirmPassword.isEmpty()){
                    confirmPasswordEt.setError(getText(R.string.missing_password_confirmation));
                    confirmPasswordEt.requestFocus();
                }
                else if(!password.equals(confirmPassword)){
                    confirmPasswordEt.setError(getText(R.string.mismatching_password_confirmation));
                    confirmPasswordEt.requestFocus();
                }
                if(name.isEmpty()){
                    nameEt.setError(getText(R.string.missing_name));
                    nameEt.requestFocus();
                }
                if(idno.isEmpty()){
                    idnoEt.setError(getText(R.string.missing_idno));
                    idnoEt.requestFocus();
                }
                if(address.isEmpty()){
                    addressEt.setError(getText(R.string.missing_address));
                    addressEt.requestFocus();
                }
                if(mobile.isEmpty()){
                    mobileEt.setError(getText(R.string.missing_mobile));
                    mobileEt.requestFocus();
                }

                else if( (!email.isEmpty()) &&
                        (!password.isEmpty()) &&
                        (!confirmPassword.isEmpty()) &&
                        (!name.isEmpty()) &&
                        (!idno.isEmpty()) &&
                        (!address.isEmpty()) &&
                        (!mobile.isEmpty())) {

                    sendVerificationCode(email);
                    verifyEmail();
                }
                else
                    Toast.makeText(SignupActivity.this, R.string.unsuccessful_login_signup, Toast.LENGTH_SHORT).show();
            }
        });

        signinTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });
    }

    private void showUi(){
        ui.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.INVISIBLE);
    }

    private void showProgress(){
        ui.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
    }

    private void extractStringsFromEditTexts(){
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString();
        confirmPassword = confirmPasswordEt.getText().toString();
        name = nameEt.getText().toString().trim();
        idno = idnoEt.getText().toString();
        address = addressEt.getText().toString();
        mobile = mobileEt.getText().toString();
    }

    private void findUiComponentsIds(){
        emailEt = findViewById(R.id.signup_email_et);
        passwordEt = findViewById(R.id.signup_password_et);
        confirmPasswordEt = findViewById(R.id.confirm_password_et);
        nameEt = findViewById(R.id.name_et);
        idnoEt = findViewById(R.id.idno_et);
        addressEt = findViewById(R.id.address_et);
        mobileEt = findViewById(R.id.mobile_et);
        signupBtn = findViewById(R.id.signup_button);
        signinTv = findViewById(R.id.login_question_tv);
    }

    private void pushUser(String authUid, String email, String password, String name, String idno, String address, String mobile){
        newUser = new User(email,password, name, idno, address, mobile);
        mDbReference.child(authUid).setValue(newUser);
        mDbReference.push();
    }

    private void putIntentExtras(Intent intent, String uid){
        intent.putExtra(getString(R.string.predecessor_key), SignupActivity.class.getName());
        intent.putExtra(getString(R.string.uid_key),uid);
    }

    private void sendVerificationCode(String email){
        String subject = "Sign-up email verification";
        verificationRandomNumber = new Random().nextInt(9000) + 1000;
        String message = "Your verification code is: " + verificationRandomNumber;
        //Creating SendMail object
        SendMail sm = new SendMail(this, email, subject, message);
        //Executing sendmail to send email
        sm.execute();
    }

    private void verifyEmail(){

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.alert_dialog_verification, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // If the code entered by user is correct, start authentication
                if(verificationRandomNumber == Integer.parseInt(userInput.getText().toString()))
                    authenticate();
                else
                    Toast.makeText(SignupActivity.this, R.string.unsuccessful_authorization, Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private void authenticate(){

        // Show ProgressBar, hide the rest of the layout
        showProgress();
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Invalid fields
                if (!task.isSuccessful()) {
                    Toast.makeText(SignupActivity.this, R.string.wrong_entries_signup, Toast.LENGTH_SHORT).show();
                    showUi();
                }
                else {
                    authUid = mFirebaseAuth.getUid();
                    // Push new user data to the DB
                    pushUser(authUid, email, password, name, idno, address, mobile);
                    Toast.makeText(SignupActivity.this, R.string.successful_signup, Toast.LENGTH_SHORT).show();
                    Intent toAssessmentActivity = new Intent(SignupActivity.this, LoginActivity.class);
                    putIntentExtras(toAssessmentActivity, authUid);
                    startActivity(toAssessmentActivity);
                    showUi();
                }
            }
        });
    }
}
