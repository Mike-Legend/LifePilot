package DarkSideArrival.LifePilot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Google Sign in variables
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        //Google Sign In variables
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        account = GoogleSignIn.getLastSignedInAccount(this); //is null if user is already signed in

        if (account != null) {
            setContentView(R.layout.activity_main);
        }else {
            setContentView(R.layout.sign_in);
        }
    }

    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {

        //TODO: animations work in progress
        Animation SlideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation SlideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        Animation SlideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation SlideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        int id = view.getId();
        if(id == R.id.Goal_Button) {
            view.startAnimation(SlideLeftIn);
            setContentView(R.layout.routine_goals);
        } else if (id == R.id.GoalBack_Button) {
            setContentView(R.layout.routine_list);
        } else if (id == R.id.routinesButton) {
            setContentView(R.layout.routine_list);
        } else if (id == R.id.analytics_button) {
            setContentView(R.layout.data_screen);
        } else if (id == R.id.Home_Button) {
            setContentView(R.layout.activity_main);
        } else if (id == R.id.NewRoutineCreate_Button) {
            FrameLayout routineoverlay = (FrameLayout) findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.CancelExercises_Button) {
            FrameLayout routineoverlay = (FrameLayout) findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.GONE);
        } else if (id == R.id.googleSignIn) {
            signIn();
            //setContentView(R.layout.experience_selection);
        } else if (id == R.id.temp_logout_button) {
            signOut();
            //setContentView(R.layout.experience_selection);
        } else {
            setContentView(R.layout.activity_main);
        }
    }

    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1; /* unique request id */

    //Google Sign in functions
    void signIn(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1200);
    }

    void signOut(){
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setContentView(R.layout.sign_in);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1200) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                setContentView(R.layout.experience_selection);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}