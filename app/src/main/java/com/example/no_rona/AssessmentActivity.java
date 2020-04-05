package com.example.no_rona;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.no_rona.apis.SendMail;
import com.example.no_rona.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AssessmentActivity extends AppCompatActivity {

    // Normal UI layout
    ConstraintLayout ui;
    // Loading spinner layout
    RelativeLayout spinner;
    // Welcome text with username
    TextView welcomeTv;
    // Assessment questions
    CheckBox q1, q2, q3, q4;
    List<Boolean> answers = new ArrayList<>();
    // Symptoms assessment score
    double score = 0;
    // Symptoms assessment result (positive = 1, negative = -1, unknown = 0)
    int result = 0;
    // Check if an email was sent before or not
    boolean isMailSent;
    Button submitResults;
    // String to hold predecessor class name to determine behavior when back is pressed (MainActivity or UserDataActivity)
    String authUid;
    // Logged in user
    User currentUser;
    DatabaseReference mDbReference;
    Button signoutBtn;

    @Override
    public void onBackPressed() {
        // clear the activity stack to close the app when back pressed in the MainActivity
        Intent a = new Intent(Intent.ACTION_MAIN);
        //TODO: we want to go back to whatever screen was active before we started our app, not specifically home screen
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        // Refer to the UI components IDs
        findUiComponentsIds();
        // Show the loading spinner while data is loading
        showProgress();

        // Getting latest signed up user uid to access his DB member
        authUid = getIntent().getStringExtra(getString(R.string.uid_key));
        mDbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(authUid);
        // This listener is to fetch the data stored in the Firebase DB
        mDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                welcomeTv.setText("Welcome, " + getUserFirstName(currentUser.getName()) + ".");
                // If the user have had an assessment before:
                if(currentUser.getResult() != 0){
                    answers = currentUser.getAnswers();
                    // Check the questions CheckBoxes according to the user previous answers
                    checkUserAnswers();
                    // Check predecessor activity
                    String predecessor = getIntent().getStringExtra(getString(R.string.predecessor_key));
                    // Go forward to instructions if it wasn't the predecessor
                    if( !predecessor.equals(InstructionsActivity.class.getName()) ){
                        // Go to the instructions
                        Intent toInstructionsActivity = new Intent(AssessmentActivity.this, InstructionsActivity.class);
                        putIntentExtras(toInstructionsActivity, authUid);
                        startActivity(toInstructionsActivity);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });

        // Delay showing the assessment questions until data fetching is done
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            // Code in run will be executed after 2s (2000ms)
            @Override
            public void run() {
                // Show normal UI "assessment questions"
                showUi();
                submitResults.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Push the data collected in the assessment after the user clicks the submit button
                        addUserData();
                        if(result == 1 && !isMailSent){
                            sendResults();
                            isMailSent = true;
                        }
                        currentUser.setMailSent(isMailSent);
                        mDbReference.setValue(currentUser);
                        // Go to the instructions
                        Intent toInstructionsActivity = new Intent(AssessmentActivity.this, InstructionsActivity.class);
                        putIntentExtras(toInstructionsActivity, authUid);
                        startActivity(toInstructionsActivity);
                    }
                });

                // Sign out
                signoutBtn = findViewById(R.id.assessment_signout_button);
                signoutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        Intent toLoginActivity = new Intent(AssessmentActivity.this, LoginActivity.class);
                        // We don't need to send the uid to the LoginActivity because we're signing out
                        toLoginActivity.putExtra(getString(R.string.predecessor_key), AssessmentActivity.class.getName());
                        startActivity(toLoginActivity);
                    }
                });
            }
        }, 3000);

    }

    private void findUiComponentsIds(){
        spinner = findViewById(R.id.assessment_progressbar);
        ui = findViewById(R.id.assessment_ui);
        welcomeTv = findViewById(R.id.assessment_welcome);
        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        submitResults = findViewById(R.id.submit_results);
    }

    private void showProgress(){
        spinner.setVisibility(View.VISIBLE);
        ui.setVisibility(View.INVISIBLE);
    }

    private void showUi(){
        spinner.setVisibility(View.INVISIBLE);
        ui.setVisibility(View.VISIBLE);
    }


    private void addUserData(){
        calculateResults();
        answers.clear();
        answers.add(q1.isChecked());
        answers.add(q2.isChecked());
        answers.add(q3.isChecked());
        answers.add(q4.isChecked());
        currentUser.setAnswers(answers);
        currentUser.setScore(score);
        currentUser.setResult(result);
    }

    private void calculateResults(){
        if(q1.isChecked())
            score = score + 0.9*(100-score);
        if(q2.isChecked())
            score = score + 0.6*(100-score);
        if(q3.isChecked())
            score = score + 0.2*(100-score);
        if(q4.isChecked())
            score = score + 0.2*(100-score);
        isProbablySick();
    }

    private void isProbablySick(){
        if(score >= 70)
            result = 1;
        else
            result = -1;
    }

    private void checkUserAnswers(){
        q1.setChecked(answers.get(0));
        q2.setChecked(answers.get(1));
        q3.setChecked(answers.get(2));
        q4.setChecked(answers.get(3));
    }

    private void putIntentExtras(Intent intent, String uid){
        intent.putExtra(getString(R.string.predecessor_key), AssessmentActivity.class.getName());
        intent.putExtra(getString(R.string.uid_key), uid);
    }

    private void sendResults(){
        String email = "mahmoudsaeed1996@gmail.com";
        String subject = "CORONAVIRUS CASE";
        String message = "This user probably has COVID-19!! \n" + currentUser.toString();
        //Creating SendMail object
        SendMail sm = new SendMail(this, email, subject, message);
        //Executing sendmail to send email
        sm.execute();
    }

    private String getUserFirstName(String username){
        int whiteSpacePosition = username.indexOf(" ");
        if(whiteSpacePosition == -1)
            return username;
        return username.substring(0, whiteSpacePosition);
    }
}
