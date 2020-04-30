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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.no_rona.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InstructionsActivity extends AppCompatActivity {

    // Normal UI
    ConstraintLayout ui;
    // ProgressBar for when the data is still loading
    RelativeLayout spinner;
    // Welcome text with username
    TextView welcomeTv;
    // Depends on the result
    TextView instructionsTv;
    int result;
    DatabaseReference mDbReference;
    // Goes to AssessmentActivity to edit the answers
    Button editAssessmentBtn;
    Button signoutBtn;
    String authUid;
    User currentUser;

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
        setContentView(R.layout.activity_instructions);

        // Refer to the UI components IDs
        findUiComponentsIds();
        // Show the loading spinner while data is loading
        showProgress();

        authUid = getIntent().getStringExtra(getString(R.string.uid_key));
        mDbReference = FirebaseDatabase.getInstance().getReference().child("Users").child(authUid);
        // This listener is to fetch the data stored in the Firebase DB
        mDbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                result = currentUser.getResult();
                welcomeTv.setText("Welcome, " + getUserFirstName(currentUser.getName()) + ".");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){}
        });

        // This sets a delay to 2s (2000ms) until the data is fetched so the instructions can be set based on the results
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showUi();
                // In case of probable positivity
                if(result == 1) {
                    instructionsTv.setText(R.string.instructions_probably_positive);
                }
                // In case of probable negativity
                else if(result == -1) {
                    instructionsTv.setText(R.string.instructions_probably_negative);
                }
            }
        }, 2000);

        // Sign out
        signoutBtn = findViewById(R.id.instructions_signout_button);
        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent toLoginActivity = new Intent(InstructionsActivity.this, LoginActivity.class);
                toLoginActivity.putExtra(getString(R.string.predecessor_key), AssessmentActivity.class.getName());
                startActivity(toLoginActivity);
            }
        });

        // Go back to the AssessmentActivity to modify your answers
        editAssessmentBtn = findViewById(R.id.edit_assessment_btn);
        editAssessmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toAssessmentActivity = new Intent(InstructionsActivity.this, AssessmentActivity.class);
                putIntentExtras(toAssessmentActivity, authUid);
                startActivity(toAssessmentActivity);
            }
        });

    }

    private void findUiComponentsIds(){
        ui = findViewById(R.id.instructions_ui);
        spinner = findViewById(R.id.instructions_progressbar);
        welcomeTv = findViewById(R.id.instructions_welcome);
        instructionsTv = findViewById(R.id.instructions);
    }

    private void showProgress(){
        spinner.setVisibility(View.VISIBLE);
        ui.setVisibility(View.INVISIBLE);
    }

    private void showUi(){
        spinner.setVisibility(View.INVISIBLE);
        ui.setVisibility(View.VISIBLE);
    }

    private String getUserFirstName(String username){
        int whiteSpacePosition = username.indexOf(" ");
        if(whiteSpacePosition == -1)
            return username;
        return username.substring(0, whiteSpacePosition);
    }

    private void putIntentExtras(Intent intent, String uid){
        intent.putExtra(getString(R.string.predecessor_key), InstructionsActivity.class.getName());
        intent.putExtra(getString(R.string.uid_key), uid);
    }
}
