package DarkSideArrival.LifePilot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.animation.ObjectAnimator;
import java.lang.reflect.Array;
import java.util.ArrayList;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.transition.Slide;
import android.transition.Visibility;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.protobuf.NullValue;
import org.checkerframework.checker.units.qual.A;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<Button> userRoutines;
    private ArrayList<CheckBox> userRoutineCheck;
    private ArrayList<Button> userGoals;
    private ArrayList<ArrayList<Button>> userExercisesArrayList;
    private ArrayList<ArrayList<TextView>> userGoalArrayList; //Future usage to add multiple goals to one routine
    public int routineIDActive, goalIDActive;
    private Scene routineAnimation, homeAnimation, goalAnimation, nRoutineAnimation;

    //Google Sign in variables
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.sign_in);
        //Google Sign In variables
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        account = GoogleSignIn.getLastSignedInAccount(this); //is null if user is already signed in

        if (account != null) {
            setContentView(R.layout.activity_main);
            //Home screen animation to layout - ONLY from home screen, duplicate to home button onClick
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionHomeLayout), R.layout.routine_list, this);
            //Welcome text change
            TextView welcome = findViewById(R.id.welcome_text);
            String name = account.getGivenName();
            welcome.setText("Welcome, "+name+"!");
        }else {
            setContentView(R.layout.sign_in);
        }

        //Set User session data
        userRoutines = new ArrayList<>();
        userRoutineCheck = new ArrayList<>();
        userGoals = new ArrayList<>();
        userExercisesArrayList = new ArrayList<ArrayList<Button>>();
        userGoalArrayList = new ArrayList<ArrayList<TextView>>();
    }

    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {
        int id = view.getId();
        if(id == R.id.Goal_Button) {
            Transition slide = new Slide(Gravity.RIGHT);
            TransitionManager.go(goalAnimation, slide);
            //Generate Goals
            if(userGoals.size() != 0) {
                LoadUserGoals();
            }
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionGoalLayout), R.layout.routine_list, this);
        } else if (id == R.id.GoalBack_Button) {
            Transition slide = new Slide(Gravity.LEFT);
            TransitionManager.go(routineAnimation, slide);
            if(userRoutines.size() != 0) {
                LoadUserRoutines();
            }
        } else if (id == R.id.routinesButton) {
            Transition slide = new Slide(Gravity.RIGHT);
            TransitionManager.go(routineAnimation, slide);
            if(userRoutines.size() != 0) {
                LoadUserRoutines();
            }
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.analytics_button) {
            //Adjusted to change activity
            Intent userInt = new Intent(getApplicationContext(), Analytics.class);
            startActivity(userInt);
        } else if (id == R.id.Home_Button) {
            Transition slide = new Slide(Gravity.LEFT);
            TransitionManager.go(homeAnimation, slide);
            //Next Buttons
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionHomeLayout), R.layout.routine_list, this);
        } else if (id == R.id.NewRoutineCreate_Button) {
            FrameLayout routinelistoverlay = findViewById(R.id.routinelistoverlay);
            routinelistoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.NewGoalCreate_Button) {
            FrameLayout routinegoallistoverlay = findViewById(R.id.routinegoallistoverlay);
            routinegoallistoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.NewExerciseAdd_Button) {
            FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.CancelNewRoutine_Button) {
            FrameLayout routinelistoverlay = findViewById(R.id.routinelistoverlay);
            routinelistoverlay.setVisibility(View.GONE);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
        } else if (id == R.id.CancelGoal_Button) {
            FrameLayout routinegoallistoverlay = findViewById(R.id.goaladdtoroutineoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
        } else if (id == R.id.CancelNewGoal_Button) {
            FrameLayout routinegoallistoverlay = findViewById(R.id.routinegoallistoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
        } else if (id == R.id.CancelExercises_Button) {
            FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.GONE);
        } else if (id == R.id.ConfirmNewGoal_Button) {
            //overlay trigger
            FrameLayout routinegoallistoverlay = findViewById(R.id.routinegoallistoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
            //button creation
            LinearLayout ll = findViewById(R.id.RoutineGoalButtonAddsHere);
            Button btn = new Button(this);
            btn.setAllCaps(false);
            TextView buttontext = findViewById(R.id.GoalNameEditText);
            if(buttontext.getText().length() == 0) {
                btn.setText("New Goal");
            } else {
                btn.setText(buttontext.getText());
            }
            btn.setTextSize(24);
            btn.setTextColor(Color.WHITE);
            btn.setClickable(true);
            btn.setOnClickListener(this::onClick);
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            btn.setBackground(gradDraw);
            btn.setPadding(0,0,0,8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            params.setMargins(0, 30, 0, 0);
            btn.setLayoutParams(params);
            ll.addView(btn);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
            buttontext.setText("");
            //added to goal list
            btn.setId(userGoals.size() + 100);
            userGoals.add(btn);
            //New Goal Array
            userGoalArrayList.add(new ArrayList<TextView>());
        } else if (id == R.id.AddExercises_Button) {
            //overlay trigger
            FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.GONE);
            //button creation
            LinearLayout ll = findViewById(R.id.ExerciseButtonAddsHere);
            int checks = 0;
            int[] eCheckIDs = new int[] {R.id.Exercise1Check, R.id.Exercise2Check, R.id.Exercise3Check, R.id.Exercise4Check};
            //check amount, based off number of available exercises
            for(int i = 0; i < 4; i++) {
                CheckBox echeck = findViewById(eCheckIDs[i]);
                if(echeck.isChecked()) {
                    checks++;
                }
            }
            //mass button create for selected exercises TODO: Change to select workout info later with array
            for(int i = 0; i < checks; i++) {
                Button btn = new Button(this);
                btn.setText("Temp Exercise");
                btn.setTextSize(24);
                btn.setTextColor(Color.WHITE);
                btn.setClickable(true);
                GradientDrawable gradDraw = new GradientDrawable();
                gradDraw.setShape(GradientDrawable.RECTANGLE);
                gradDraw.setCornerRadius(100);
                gradDraw.setColor(getResources().getColor(R.color.royalPurple));
                btn.setBackground(gradDraw);
                btn.setPadding(0,0,0,8);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                params.setMargins(0, 30, 0, 0);
                btn.setLayoutParams(params);
                ll.addView(btn);
                userExercisesArrayList.get(routineIDActive).add(btn);
            }
            //Uncheck exercises if on same screen
            for(int i = 0; i < 4; i++) {
                CheckBox echeck = findViewById(eCheckIDs[i]);
                if(echeck.isChecked()) {
                    echeck.setChecked(false);
                }
            }
        } else if (id == R.id.AddGoal_Button) { //TODO: Readding goal bugged, diff goals unchecked bugged
            FrameLayout routinegoallistoverlay = findViewById(R.id.goaladdtoroutineoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
            TextView goal = new TextView(this);
            goal.setVisibility(View.GONE);
            String g1 = "Goal: ";
            String g2 = String.valueOf(userGoals.get(goalIDActive).getText());
            goal.setText(g1 + g2);
            for(int i = 0; i < userRoutineCheck.size(); i++) {
                if(userRoutineCheck.get(i).isChecked()) {
                    if(i + 1 > userGoalArrayList.size()) {
                        userGoalArrayList.add(new ArrayList<TextView>());
                    } else if(userGoalArrayList.get(i).size() > 0) {
                        userGoalArrayList.get(i).remove(i);
                    }
                    userGoalArrayList.get(i).add(goal);
                } //else if(userRoutineCheck.get(i).isChecked() == false) {
                    //if(i + 1 > userGoalArrayList.size()) {
                        //userGoalArrayList.add(new ArrayList<TextView>());
                   //}
                    //if(userGoalArrayList.get(i).size() > 0) {
                        //userGoalArrayList.get(i).get(0).setText("");
                    //}
                //}
            }
        } else if (id == R.id.ConfirmNewRoutine_Button) {
            //overlay trigger
            FrameLayout routinelistoverlay = findViewById(R.id.routinelistoverlay);
            routinelistoverlay.setVisibility(View.GONE);
            //button creation
            LinearLayout ll = findViewById(R.id.RoutineButtonAddsHere);
            Button btn = new Button(this);
            btn.setAllCaps(false);
            TextView buttontext = findViewById(R.id.RoutineNameEditText);
            if(buttontext.getText().length() == 0) {
                btn.setText("New Routine");
            } else {
                btn.setText(buttontext.getText());
            }
            btn.setTextSize(24);
            btn.setTextColor(Color.WHITE);
            btn.setClickable(true);
            btn.setOnClickListener(this::onClick);
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            btn.setBackground(gradDraw);
            btn.setPadding(0,0,0,8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            params.setMargins(0, 30, 0, 0);
            btn.setLayoutParams(params);
            ll.addView(btn);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
            buttontext.setText("");
            //added to routine list
            btn.setId(userRoutines.size());
            userRoutines.add(btn);
            //Checkbox create
            CheckBox temp2 = new CheckBox(getApplicationContext());
            temp2.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
            temp2.setScaleX(2);
            temp2.setScaleY(2);
            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            params2.setMargins(0, 30, 0, 0);
            temp2.setLayoutParams(params);
            //added to routine check list
            userRoutineCheck.add(temp2);
            //New Exercise Array
            userExercisesArrayList.add(new ArrayList<Button>());
        } else if (id < userRoutines.size() || id == userRoutines.size()) {
            Transition slide = new Slide(Gravity.RIGHT);
            TransitionManager.go(nRoutineAnimation, slide);
            //Generate Name and routine separation
            routineIDActive = id;
            TextView titletext = findViewById(R.id.NewRoutineSet_TopText);
            for(int i = 0; i < userRoutines.size(); i++) {
                if(userRoutines.get(i).getId() == id) {
                    titletext.setText(userRoutines.get(i).getText());
                }
            }
            //Check goals
            if(userGoals.size() != 0) {
                if(routineIDActive + 1 > userGoalArrayList.size()) {
                    TextView goal = findViewById(R.id.GoalofRoutine_TopText);
                    goal.setVisibility(View.GONE);
                } else if(userGoalArrayList.get(routineIDActive) != null) {
                    TextView goal = findViewById(R.id.GoalofRoutine_TopText);
                    goal.setVisibility(View.VISIBLE);
                    goal.setText(userGoalArrayList.get(routineIDActive).get(0).getText());
                } else {
                    TextView goal = findViewById(R.id.GoalofRoutine_TopText);
                    goal.setVisibility(View.GONE);
                }
            } else {
                TextView goal = findViewById(R.id.GoalofRoutine_TopText);
                goal.setVisibility(View.GONE);
            }
            //Generate Routine Exercises
            if(userExercisesArrayList.get(routineIDActive).size() != 0) {
                LoadUserRoutineExercises();
            }
            //button usage animations
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id > 99 && id < userGoals.size() + 101) {
            FrameLayout routinegoaloverlay = findViewById(R.id.goaladdtoroutineoverlay);
            routinegoaloverlay.setVisibility(View.VISIBLE);
            goalIDActive = id - 100;
            //empty check text
            if(userRoutines.size() == 0) {
                TextView rl = findViewById(R.id.NoGoal_TopText);
                rl.setVisibility(View.VISIBLE);
            } else {
                TextView rl = findViewById(R.id.NoGoal_TopText);
                rl.setVisibility(View.GONE);
            }
            //Generate goal name
            int goalIdActive = id;
            TextView titletext = findViewById(R.id.GoalToRoutine_TopText);
            for(int i = 0; i < userGoals.size(); i++) {
                if(userGoals.get(i).getId() == id) {
                    titletext.setText(userGoals.get(i).getText());
                }
            }
            //list routines to add
            LinearLayout ll = findViewById(R.id.GoalRoutineListHere);
            Button temp;
            for(int i = 0; i < userRoutines.size(); i++) {
                temp = (userRoutines.get(i));
                if(temp.getParent() != null) {
                    ((ViewGroup)temp.getParent()).removeView(temp);
                }
                ll.addView(temp);
            }
            //list checkboxes to add
            LinearLayout ll2 = findViewById(R.id.GoalCheckRoutineListHere);
            CheckBox temp2;
            for(int i = 0; i < userRoutines.size(); i++) {
                temp2 = (userRoutineCheck.get(i));
                if(temp2.getParent() != null) {
                    ((ViewGroup)temp2.getParent()).removeView(temp2);
                }
                ll2.addView(temp2);
            }
        } else if (id == R.id.ExerciseSave_Button){
            //TODO: Sync to firebase
            setContentView(R.layout.routine_list);
            LoadUserRoutines();
        } else if (id == R.id.googleSignIn) {
            signIn();
            //setContentView(R.layout.experience_selection);
        } else if (id == R.id.temp_logout_button) {
            signOut();
            //setContentView(R.layout.experience_selection);
        } else {
            setContentView(R.layout.activity_main);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionHomeLayout), R.layout.routine_list, this);

            if (account != null){
                TextView welcome = findViewById(R.id.welcome_text);
                String name = account.getGivenName();
                welcome.setText("Welcome, "+name+"!");
            }
        }
    }

    //Google Sign in functions
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1200; /* unique request id */
    void signIn(){
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
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
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                setContentView(R.layout.experience_selection);
                account = GoogleSignIn.getLastSignedInAccount(this);
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //load custom routines
    public void LoadUserRoutines() {
        LinearLayout ll = findViewById(R.id.RoutineButtonAddsHere);
        for (int i = 0; i < userRoutines.size(); i++) {
            if(userRoutines.get(i).getParent() != null) {
                ((ViewGroup)userRoutines.get(i).getParent()).removeView(userRoutines.get(i));
            }
            ll.addView(userRoutines.get(i));
        }
    }

    //load custom goals
    public void LoadUserGoals() {
        LinearLayout ll = findViewById(R.id.RoutineGoalButtonAddsHere);
        for (int i = 0; i < userGoals.size(); i++) {
            if(userGoals.get(i).getParent() != null) {
                ((ViewGroup)userGoals.get(i).getParent()).removeView(userGoals.get(i));
            }
            ll.addView(userGoals.get(i));
        }
    }

    //load custom exercises per routine
    public void LoadUserRoutineExercises() {
        LinearLayout ll = findViewById(R.id.ExerciseButtonAddsHere);
        Button temp;
        for(int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
            temp = (userExercisesArrayList.get(routineIDActive).get(i));
            if(temp.getParent() != null) {
                ((ViewGroup)temp.getParent()).removeView(temp);
            }
            ll.addView(temp);
        }
    }
}