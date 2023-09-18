package darksidearrivals.lifepilot;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jmedeisis.draglinearlayout.DragLinearLayout;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //Routine and goal variables and arrays
    private ArrayList<Button> userRoutines, userGoals, routineDeleteList;
    private ArrayList<String> currentSelectedItems = new ArrayList<>();
    private ArrayList<CheckBox> userRoutineCheck;
    private ArrayList<ArrayList<Button>> userExercisesArrayList;
    private ArrayList<ArrayList<TextView>> userGoalArrayList; //Future usage to add multiple goals to one routine
    public int routineIDActive, goalIDActive;
    private Scene routineAnimation, homeAnimation, goalAnimation, nRoutineAnimation;

    //Arrays Containing Workout Logs For Each Muscle Group
    //SDK 26>= Cannot utilize LocalDateTime functions, as such, the tracking charts will not be a function for them.
    private ArrayList<LocalDateTime> chestExercisesLog = new ArrayList<>(), shoulderExercisesLog = new ArrayList<>(), bicepExercisesLog = new ArrayList<>(),
    tricepsExercisesLog = new ArrayList<>(), legExercisesLog = new ArrayList<>(), backExercisesLog = new ArrayList<>(), gluteExercisesLog = new ArrayList<>(), abExercisesLog = new ArrayList<>(),
    calvesExercisesLog = new ArrayList<>(), forearmFlexorsGripExercisesLog = new ArrayList<>(), forearmExtensorExercisesLog = new ArrayList<>(), cardioExercisesLog = new ArrayList<>(),
    bodyWeightLog = new ArrayList<>();

    //Creating Class to store weight and time combination for weight tracker.
    public class BodyWeightLog
    {
        float weightSnapShot;
        LocalDateTime timeLog;

        BodyWeightLog()
        {
            weightSnapShot =  userWeight;

            //SDK 26>= Cannot utilize LocalDateTime functions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                timeLog = LocalDateTime.now();
            }

            else
            {
                //I can  almost guarantee I might forget to put the version if check on something that uses the timeLog in calculation and crash the app on any version lower than 26.
                //Hopefully not.
                timeLog = null;
            }
        }
    }

    //Creating Array of body weight logs to store logs.
    private ArrayList<BodyWeightLog> bodyWeightChangeLog = new ArrayList<>();


    //Workout Spinner
    Spinner spinner;
    RecyclerView WorkoutRecyclerView;
    WorkoutRecycler workoutList;

    //Height and Weight Variables
    private float userWeight, userHeight;

    //Google Sign in variables
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.sign_in);
        //Google Sign In variables
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser(); //is null if user is not signed in
        account = GoogleSignIn.getLastSignedInAccount(this); //is null if user is not signed in

        if (account != null && user != null) {
            performAccountStartUp();
        }else {
            setContentView(R.layout.sign_in);
        }

        //User deletion arrays (do not store)
        routineDeleteList = new ArrayList<>();

        //Set User session data (load from firebase)
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
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.RoutineBack_Button) {
            //setContentView(R.layout.routine_list);
            Transition slide = new Slide(Gravity.LEFT);
            TransitionManager.go(routineAnimation, slide);
            if (userRoutines.size() != 0) {
                LoadUserRoutines();
            }
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
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
        } else if (id == R.id.EditRoutineList_Button) {
            //overlay trigger
            if(userRoutines.size() != 0) {
                setContentView(R.layout.routine_list);
                LoadUserRoutines();
                FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
                routineedit.setVisibility(View.VISIBLE);
                //place routines to edit
                DragLinearLayout dragLinearLayout = findViewById(R.id.PlaceEditRoutineList);
                for (int i = 0; i < userRoutines.size(); i++) {
                    if(userRoutines.get(i).getParent() != null) {
                        ((ViewGroup)userRoutines.get(i).getParent()).removeView(userRoutines.get(i));
                    }
                    dragLinearLayout.addView(userRoutines.get(i));
                }
                editChecker = 1;
                goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
                homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
                nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
            } else {
                //no routine available to edit message
            }
        } else if (id < userRoutines.size() && editChecker == 1) {
            //if selected
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.deleteRed));
            if (routineDeleteList.contains(userRoutines.get(id))) {
                gradDraw.setColor(getResources().getColor(R.color.royalPurple));
                userRoutines.get(id).setBackground(gradDraw);
                //remove from array
                routineDeleteList.remove(userRoutines.get(id));
            } else {
                userRoutines.get(id).setBackground(gradDraw);
                //add to array
                routineDeleteList.add(userRoutines.get(id));
            }
        } else if (id == R.id.CancelEditRoutine_Button) {
            //reset info
            editChecker = 0;
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            for(int i = 0; i < userRoutines.size(); i++) {
                for(int j = 0; j < routineDeleteList.size(); j++) {
                    if(userRoutines.get(i).getId() == routineDeleteList.get(j).getId()) {
                        userRoutines.get(i).setBackground(gradDraw);
                    }
                }
            }
            routineDeleteList.clear();
            //overlay trigger
            FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
            routineedit.setVisibility(View.GONE);
            LoadUserRoutines();
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.DeleteRoutines_Button) {
            //delete info
            editChecker = 0;
            for(int i = 0; i < userRoutines.size(); i++) {
                for(int j = 0; j < routineDeleteList.size(); j++) {
                    if(userRoutines.get(i).getId() == routineDeleteList.get(j).getId()) {
                        userRoutines.remove(i);
                        if(userExercisesArrayList.get(i).size() != 0) {
                            userExercisesArrayList.get(i).clear();
                            userExercisesArrayList.remove(i);
                        }
                    }
                }
            }
            //reset id for array sorting
            for(int i = 0; i < userRoutines.size(); i++) {
                userRoutines.get(i).setId(i);
                if(userExercisesArrayList.get(i).size() != 0) {
                    for(int j = 0; j < userExercisesArrayList.size(); j++) {
                        userExercisesArrayList.get(j).set(j, userExercisesArrayList.get(j).get(j));
                    }
                }
            }
            routineDeleteList.clear();
            //overlay trigger
            FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
            routineedit.setVisibility(View.GONE);
            LoadUserRoutines();
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        }

        else if (id == R.id.analytics_button)
        {
            setContentView(R.layout.data_screen);
           ShowDataScreen();
        }

        else if(id == R.id.analyticsHomeButton)
        {
            GoToHomeScreen();
        }

        else if (id == R.id.excerciseData)
        {
            //Initiating Spinner for workout breakdown.
            Spinner breakdownSpinner = (Spinner) findViewById(R.id.exerciseSpinner);
            setContentView(R.layout.exercise_data);
        }

        else if (id == R.id.breakDown)
        {
            //Initiating Spinner for Month Selection
            Spinner monthSpinner = (Spinner) findViewById(R.id.monthSelect);
            setContentView(R.layout.muscle_distribution);
        }

        else if (id == R.id.weight_height_save_button)
        {
                SetWeightHeight();
                GoToHomeScreen();
        }

        else if(id == R.id.exerciseBack)
        {
            setContentView(R.layout.data_screen);
           ShowDataScreen();
        }

        else if (id == R.id.exerciseHomeButton)
        {
            GoToHomeScreen();
        }

        else if(id == R.id.muscleHomeButton)
        {
            GoToHomeScreen();
        }

        else if (id == R.id.muscleBack)
        {
            setContentView(R.layout.data_screen);
            ShowDataScreen();
        }

        else if(id == R.id.log_Save)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                SetHeightWeightAndLog();
            }

            else
            {
                SetWeightHeight();
            }

            GoToHomeScreen();
        }

        else if (id == R.id.Home_Button) {
            GoToHomeScreen();
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
            KeyboardVanish(view);
        } else if (id == R.id.CancelGoal_Button) {
            FrameLayout routinegoallistoverlay = findViewById(R.id.goaladdtoroutineoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
        } else if (id == R.id.CancelNewGoal_Button) {
            FrameLayout routinegoallistoverlay = findViewById(R.id.routinegoallistoverlay);
            routinegoallistoverlay.setVisibility(View.GONE);
            KeyboardVanish(view);
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
            KeyboardVanish(view);
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

            LinearLayout ll = findViewById(R.id.ExerciseButtonAddsHere);
            for(int i = 0; i < currentSelectedItems.size(); i++) {
                Button btn = new Button(this);
                btn.setText(currentSelectedItems.get(i));
                btn.setTextSize(24);
                btn.setTextColor(Color.WHITE);
                btn.setAllCaps(false);
                btn.setClickable(true);
                btn.setOnClickListener(getOnClickForDynamicButtons(btn));
                GradientDrawable gradDraw = new GradientDrawable();
                gradDraw.setShape(GradientDrawable.RECTANGLE);
                gradDraw.setCornerRadius(90);
                gradDraw.setColor(getResources().getColor(R.color.royalPurple));
                btn.setBackground(gradDraw);
                btn.setPadding(20,0,20,8);
                if(btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                    userExercisesArrayList.get(routineIDActive).add(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                    userExercisesArrayList.get(routineIDActive).add(btn);
                }
            }
            //empty array for new selects
            currentSelectedItems.clear();
            //RESET Recycler
            GenerateWorkoutRecycler();
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
            KeyboardVanish(view);
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
            GenerateRoutineSelectScreen(id);
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.RoutineDynamicbackButton) {
            if(dynamicChecker == 0) {
                setContentView(R.layout.routine_newlist);
                id = routineIDActive;
                GenerateRoutineSelectScreen(id);
                FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
                routineoverlay.setVisibility(View.VISIBLE);
                GenerateSpinnerWorkouts();
                GenerateWorkoutRecycler();
                //setup next button animations
                goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
                homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
                routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
            } else {
                dynamicChecker = 0;
                setContentView(R.layout.routine_newlist);
                id = routineIDActive;
                GenerateRoutineSelectScreen(id);
                //setup next button animations
                goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
                homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
                routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
            }
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
        } else if (id == R.id.PreMadeRoutine3_Button) { //TODO: Finish premade routine lists
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Leg Day");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array


            //make save, a save to routine array list and sync

            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine2_Button) {
            setContentView(R.layout.routine_newlist);
        } else if (id == R.id.PreMadeRoutine1_Button) {
            setContentView(R.layout.routine_newlist);
        } else if (id == R.id.PreMadeRoutine4_Button) {
            setContentView(R.layout.routine_newlist);
        } else if (id == R.id.PreMadeRoutine5_Button) {
            setContentView(R.layout.routine_newlist);
        } else if (id == R.id.PreMadeRoutine6_Button) {
            setContentView(R.layout.routine_newlist);
        } else if (id == R.id.ExerciseSave_Button){
            //TODO: Sync to firebase
            //all array information
            /*Map<String, Object> userArray = new HashMap<>();
            ArrayList<String> userStringRoutines = new ArrayList<>();
            for (int i=0; i<userRoutines.size(); i++){

                 userStringRoutines.add(userRoutines.get(i).toString());
            }

            userArray.put("Routines", Arrays.asList(userStringRoutines.get(0)));
            db.collection("Users").document(user.getUid())
                    .update(userArray);*/
            setContentView(R.layout.routine_list);
            LoadUserRoutines();
        } else if (id == R.id.calendar_button) {
            setContentView(R.layout.calendar_screen);
        } else if (id == R.id.googleSignIn) {
            signIn();
            //setContentView(R.layout.experience_selection);
        } else if (id == R.id.temp_logout_button) {
            signOut();
            //setContentView(R.layout.experience_selection);
        }  else if (id == R.id.still_learning_button) {
            // Add "still learning" to firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("Experience", "Still Learning");
            db.collection("Users").document(user.getUid())
                    .update(userData);
            setContentView(R.layout.goal_selection);
        } else if (id == R.id.veteran_button) {
            // Add "veteran" to firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("Experience", "Veteran");
            db.collection("Users").document(user.getUid())
                    .update(userData);
            setContentView(R.layout.goal_selection);
        } else if (id == R.id.lose_weight_button) {
            // Add "lose weight" to firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("Goal", "Lose Weight");
            db.collection("Users").document(user.getUid())
                    .update(userData);
            setContentView(R.layout.weight_height_input);
            WeightHeightInputSetup();
        } else if (id == R.id.gain_weight_button) {
            // Add "gain weight" to firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("Goal", "Gain Weight");
            db.collection("Users").document(user.getUid())
                    .update(userData);
            setContentView(R.layout.weight_height_input);
            WeightHeightInputSetup();
        } else if (id == R.id.maintain_weight_button) {
            // Add "maintain weight" to firebase
            Map<String, Object> userData = new HashMap<>();
            userData.put("Goal", "Maintain Weight");
            db.collection("Users").document(user.getUid())
                    .update(userData);
            setContentView(R.layout.weight_height_input);
            WeightHeightInputSetup();
        }

        else {
            GoToHomeScreen();
        }
    }


    private void GoToHomeScreen(){
        setContentView(R.layout.activity_main);
        routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionHomeLayout), R.layout.routine_list, this);

        //Updating Home Screen text
        if (account != null && user != null){
            //Updating welcome text
            TextView welcome_text = findViewById(R.id.welcome_text);
            String name = user.getDisplayName();
            welcome_text.setText("Welcome, "+name+"!");

            //Adding Profile Picture
            ImageView profileButton = findViewById(R.id.profile_pic);
            Picasso.get().load(account.getPhotoUrl()).into(profileButton);

            //Updating goal text from Firebase
            TextView goal_text = findViewById(R.id.goal_text);
            db.collection("Users").document(user.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()){
                                    goal_text.setText("Goal: "+document.getData().get("Goal"));
                                }else{
                                    Toast.makeText(getApplicationContext(), "Error, user document doesn't exist", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to connect to database", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    void GenerateRoutineSelectScreen(int id) {
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
        GenerateSpinnerWorkouts();
        GenerateWorkoutRecycler();
        //button usage animations
        routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
    }

    void KeyboardVanish(View view) {
        //if keyboard doesn't go away
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
    }

    void GenerateSpinnerWorkouts() {
        //Set workout spinner
        spinner =  findViewById(R.id.exerciseSpin);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, workouts);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    void GenerateWorkoutRecycler() {
        //Set Recycler
        WorkoutRecyclerView = findViewById(R.id.workList);
        WorkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutList = new WorkoutRecycler(chestExercises, currentSelectedItems, new WorkoutRecycler.OnItemCheckListener() {
            @Override
            public void onItemCheck(String string) {
                currentSelectedItems.add(string);
            }
            @Override
            public void onItemUncheck(String string) {
                currentSelectedItems.remove(string);
            }
            @Override
            public void onButtonClick(String string) {
                DynamicScreenWorkouts(string);
            }
        });
        WorkoutRecyclerView.setAdapter(workoutList);
        workoutList.notifyDataSetChanged();
    }

    void DynamicScreenWorkouts(String string) {
        setContentView(R.layout.activity_dynamic_workout_screen);
        String exercise = string;
        TextView workoutTitle = findViewById(R.id.workoutTitle);
        workoutTitle.setText(exercise);
        TextView workoutDesc = findViewById(R.id.workoutDesc);
        GifImageView workoutImage = findViewById(R.id.workoutPic);
        InputStream textFile = getResources().openRawResource(R.raw.workoutdesc);
        BufferedReader textReader = new BufferedReader(new InputStreamReader(textFile));
        try
        {
            String textLine = textReader.readLine();
            while(textLine != null)
            {
                String [] columns = textLine.split("\\|");
                String exerciseName = columns[0];
                if(exerciseName.equals(exercise))
                {
                    String exerciseDesc = columns[1];
                    workoutDesc.setText(exerciseDesc);
                    break;
                }
                textLine = textReader.readLine();
            }
            textReader.close();
        }
        catch(IOException E)
        {
            E.printStackTrace();
        }
        String imageName = exercise.replaceAll(" ", "").replaceAll("-", "").toLowerCase();
        int resourceId = getResources().getIdentifier(imageName, "raw", getBaseContext().getPackageName());
        workoutImage.setImageResource(resourceId);
    }

    //Set for dynamic buttons to exercise info
    private int dynamicChecker, editChecker;
    View.OnClickListener getOnClickForDynamicButtons(final Button btn) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                dynamicChecker = 1;
                DynamicScreenWorkouts((String)btn.getText());
            }
        };
    }

    //Google Sign in functions
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1200; /* unique request id */
    void signIn(){
        //Start Sign in process
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    void signOut(){
        FirebaseAuth.getInstance().signOut();
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setContentView(R.layout.sign_in);
            }
        });
    }

    void performAccountStartUp(){
        /*
            This function:
            -Will add the user to firebase if it's a new user
            -Will take you to profile creation if you're a new user or somehow skipped it
            -Otherwise, it will pull your information from Firebase and take you to home screen
         */

        //Checking if user's firebase document exists
        db.collection("Users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Checking if skipped profile creation
                        if (!document.contains("Experience")){
                            setContentView(R.layout.experience_selection);
                        }else if (!document.contains("Goal")){
                            setContentView(R.layout.goal_selection);
                        }else if (!document.contains("Weight") || !document.contains("Height")){
                            setContentView(R.layout.weight_height_input);
                        }else{
                            //Returning User, Sync with Firebase
                            db.collection("Users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()){
                                                    double weight = ((double) document.getData().get("Weight"));
                                                    double height = ((double) document.getData().get("Height"));
                                                    userWeight = (float) weight;
                                                    userHeight = (float) height;
                                                }else{
                                                    Toast.makeText(getApplicationContext(), "Error, user document doesn't exist", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Failed to connect to database", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            GoToHomeScreen();
                        }
                    } else {
                        // Create a new user with a first and last name
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("First Name", account.getGivenName());
                        userData.put("Last Name", account.getFamilyName());

                        // Add a new document with their Google ID
                        db.collection("Users").document(user.getUid())
                                .set(userData);

                        // Go to profile creation
                        setContentView(R.layout.experience_selection);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "New User Detection Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Signing in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                account = GoogleSignIn.getLastSignedInAccount(this);

                //Getting an ID token from Google and using it to authenticate with Firebase
                String idToken = account.getIdToken();
                if (idToken != null){
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success
                                user = mAuth.getCurrentUser();
                                performAccountStartUp();

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getApplicationContext(), "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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

    //Workout lists
    private String[] workouts = new String[]{"Chest Exercises",
            "Shoulder Exercises",
            "Bicep Exercises",
            "Triceps Exercises",
            "Leg Exercises",
            "Back Exercises",
            "Glute Exercises",
            "Ab Exercises",
            "Calves Exercises",
            "Forearm Flexors and Grip Exercises",
            "Forearm Extensor Exercises",
            "Cardio Exercises",
            "Body Weight"};
    private String[] chestExercises = new String[]{"Bar Dip",
            "Bench Press",
            "Cable Chest Press",
            "Close-Grip Bench Press",
            "Close-Grip Feet-Up Bench Press",
            "Decline Bench Press",
            "Dumbbell Chest Fly",
            "Dumbbell Chest Press",
            "Dumbbell Decline Chest Press",
            "Dumbbell Floor Press",
            "Dumbbell Pullover",
            "Feet-Up Bench Press",
            "Floor Press",
            "Incline Bench Press",
            "Incline Dumbbell Press",
            "Incline Push-Up",
            "Kneeling Incline Push-Up",
            "Kneeling Push-Up",
            "Machine Chest Fly",
            "Machine Chest Press",
            "Pec Deck",
            "Push-Up",
            "Push-Up Against Wall",
            "Push-Ups With Feet in Rings",
            "Resistance Band Chest Fly",
            "Smith Machine Bench Press",
            "Smith Machine Incline Bench Press",
            "Standing Cable Chest Fly",
            "Standing Resistance Band Chest Fly"};
    private String[] shoulderExercises = new String[]{"Band External Shoulder Rotation",
            "Band Internal Shoulder Rotation",
            "Band Pull-Apart",
            "Barbell Front Raise",
            "Barbell Rear Delt Row",
            "Barbell Upright Row",
            "Behind the Neck Press",
            "Cable Lateral Raise",
            "Cable Rear Delt Row",
            "Dumbbell Front Raise",
            "Dumbbell Horizontal Internal Shoulder Rotation",
            "Dumbbell Horizontal External Shoulder Rotation",
            "Dumbbell Lateral Raise",
            "Dumbbell Rear Delt Row",
            "Dumbbell Shoulder Press",
            "Face Pull",
            "Front Hold",
            "Lying Dumbbell External Shoulder Rotation",
            "Lying Dumbbell Internal Shoulder Rotation",
            "Machine Lateral Raise",
            "Machine Shoulder Press",
            "Monkey Row",
            "Overhead Press",
            "Plate Front Raise",
            "Power Jerk",
            "Push Press",
            "Reverse Dumbbell Flyes",
            "Reverse Machine Fly",
            "Seated Dumbbell Shoulder Press",
            "Seated Barbell Overhead Press",
            "Seated Smith Machine Shoulder Press",
            "Snatch Grip Behind the Neck Press",
            "Squat Jerk",
            "Split Jerk"};
    private String[] bicepExercises = new String[]{"Barbell Curl",
            "Barbell Preacher Curl",
            "Bodyweight Curl",
            "Cable Curl With Bar",
            "Cable Curl With Rope",
            "Concentration Curl",
            "Dumbbell Curl",
            "Dumbbell Preacher Curl",
            "Hammer Curl",
            "Incline Dumbbell Curl",
            "Machine Bicep Curl",
            "Spider Curl"};
    private String[] tricepsExercises = new String[]{"Barbell Standing Triceps Extension",
            "Barbell Lying Triceps Extension",
            "Bench Dip",
            "Close-Grip Push-Up",
            "Dumbbell Lying Triceps Extension",
            "Dumbbell Standing Triceps Extension",
            "Overhead Cable Triceps Extension",
            "Tricep Bodyweight Extension",
            "Tricep Pushdown With Bar",
            "Tricep Pushdown With Rope"};
    private String[] legExercises = new String[]{"Air Squat",
            "Barbell Hack Squat",
            "Barbell Lunge",
            "Barbell Walking Lunge",
            "Belt Squat",
            "Body Weight Lunge",
            "Box Squat",
            "Bulgarian Split Squat",
            "Chair Squat",
            "Dumbbell Lunge",
            "Dumbbell Squat",
            "Front Squat",
            "Goblet Squat",
            "Hack Squat Machine",
            "Half Air Squat",
            "Hip Adduction Machine",
            "Landmine Hack Squat",
            "Landmine Squat",
            "Leg Extension",
            "Leg Press",
            "Lying Leg Curl",
            "Pause Squat",
            "Romanian Deadlift",
            "Safety Bar Squat",
            "Seated Leg Curl",
            "Shallow Body Weight Lunge",
            "Side Lunges Bodyweight",
            "Smith Machine Squat",
            "Squat",
            "Step Up"};
    private String[] backExercises = new String[]{"Back Extension",
            "Barbell Row",
            "Barbell Shrug",
            "Block Snatch",
            "Cable Close Grip Seated Row",
            "Cable Wide Grip Seated Row",
            "Chin-Up",
            "Clean",
            "Clean and Jerk",
            "Deadlift",
            "Deficit Deadlift",
            "Dumbbell Deadlift",
            "Dumbbell Row",
            "Dumbbell Shrug",
            "Floor Back Extension",
            "Good Morning",
            "Hang Clean",
            "Hang Power Clean",
            "Hang Power Snatch",
            "Hang Snatch",
            "Inverted Row",
            "Inverted Row with Underhand Grip",
            "Kettlebell Swing",
            "Lat Pulldown With Pronated Grip",
            "Lat Pulldown With Supinated Grip",
            "One-Handed Cable Row",
            "One-Handed Lat Pulldown",
            "Pause Deadlift",
            "Pendlay Row",
            "Power Clean",
            "Power Snatch",
            "Pull-Up",
            "Rack Pull",
            "Seal Row",
            "Seated Machine Row",
            "Snatch",
            "Snatch Grip Deadlift",
            "Stiff-Legged Deadlift",
            "Straight Arm Lat Pulldown",
            "Sumo Deadlift",
            "T-Bar Row",
            "Trap Bar Deadlift With High Handles",
            "Trap Bar Deadlift With Low Handles"};
    private String[] gluteExercises = new String[]{"Banded Side Kicks",
            "Cable Pull Through",
            "Clamshells",
            "Dumbbell Romanian Deadlift",
            "Dumbbell Frog Pumps",
            "Fire Hydrants",
            "Frog Pumps",
            "Glute Bridge",
            "Hip Abduction Against Band",
            "Hip Abduction Machine",
            "Hip Thrust",
            "Hip Thrust Machine",
            "Hip Thrust With Band Around Knees",
            "Lateral Walk With Band",
            "Machine Glute Kickbacks",
            "One-Legged Glute Bridge",
            "One-Legged Hip Thrust",
            "Romanian Deadlift",
            "Single Leg Romanian Deadlift",
            "Standing Glute Kickback in Machine",
            "Step Up"};
    private String[] abExercises = new String[]{"Cable Crunch",
            "Crunch",
            "Dead Bug",
            "Hanging Leg Raise",
            "Hanging Knee Raise",
            "Hanging Sit-Up",
            "High to Low Wood Chop with Band",
            "Horizontal Wood Chop with Band",
            "Kneeling Ab Wheel Roll-Out",
            "Kneeling Plank",
            "Kneeling Side Plank",
            "Lying Leg Raise",
            "Lying Windshield Wiper",
            "Lying Windshield Wiper with Bent Knees",
            "Machine Crunch",
            "Mountain Climbers",
            "Oblique Crunch",
            "Oblique Sit-Up",
            "Plank",
            "Side Plank",
            "Sit-Up"};
    private String[] calvesExercises = new String[]{"Eccentric Heel Drop" ,
            "Heel Raise" ,
            "Seated Calf Raise" ,
            "Standing Calf Raise"};
    private String[] forearmFlexExercises = new String[]{"Barbell Wrist Curl",
            "Barbell Wrist Curl Behind the Back",
            "Bar Hang",
            "Dumbbell Wrist Curl",
            "Farmers Walk",
            "Fat Bar Deadlift",
            "Gripper",
            "One-Handed Bar Hang",
            "Plate Pinch",
            "Plate Wrist Curl",
            "Towel Pull-Up"};
    private String[] forearmExtExercises = new String[]{"Barbell Wrist Extension",
            "Dumbbell Wrist Extension"};
    private String[] cardioExercises = new String[]{"Rowing Machine",
            "Stationary Bike",
            "Treadmill",
            "Elliptical",
            "Stair Climber",
            "Running",
            "Jogging",
            "Walking",
            "Yoga",
            "Sports"};
    private String[] bodyweight = new String[]{"Jumping Jacks",
            "Push-Ups"};

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String currentSel = spinner.getSelectedItem().toString();
        if(currentSel.equals("Chest Exercises"))
        {
            workoutList.myWorkouts = chestExercises;
        }
        else if(currentSel.equals("Shoulder Exercises"))
        {
            workoutList.myWorkouts = shoulderExercises;
        }
        else if(currentSel.equals("Bicep Exercises"))
        {
            workoutList.myWorkouts = bicepExercises;
        }
        else if(currentSel.equals("Triceps Exercises"))
        {
            workoutList.myWorkouts = tricepsExercises;
        }
        else if(currentSel.equals("Leg Exercises"))
        {
            workoutList.myWorkouts = legExercises;
        }
        else if(currentSel.equals("Back Exercises"))
        {
            workoutList.myWorkouts = backExercises;
        }
        else if(currentSel.equals("Glute Exercises"))
        {
            workoutList.myWorkouts = gluteExercises;
        }
        else if(currentSel.equals("Ab Exercises"))
        {
            workoutList.myWorkouts = abExercises;
        }
        else if(currentSel.equals("Calves Exercises"))
        {
            workoutList.myWorkouts = calvesExercises;
        }
        else if(currentSel.equals("Forearm Flexors and Grip Exercises"))
        {
            workoutList.myWorkouts = forearmFlexExercises;
        }
        else if(currentSel.equals("Forearm Extensor Exercises"))
        {
            workoutList.myWorkouts = forearmExtExercises;
        }
        else if(currentSel.equals("Cardio Exercises"))
        {
            workoutList.myWorkouts = cardioExercises;
        }
        else if(currentSel.equals("Body Weight"))
        {
            workoutList.myWorkouts = bodyweight;
        }
        workoutList.notifyDataSetChanged();
    }

    //Calculate BMI (Imperial)
    public float CalculateBMI()
    {
        float bmi;

        if(userHeight != 0 && userWeight != 0)
        {
            bmi = 703 * (userWeight/(userHeight*userHeight));
            BigDecimal bmiD = new BigDecimal(bmi);
            bmiD = bmiD.setScale(1, RoundingMode.HALF_UP);
            bmi = bmiD.floatValue();
        }

        else
        {
            bmi = 0;
        }

        return bmi;
    }

    //Self-explanatory, but just in case, function for displaying values on data screen.
    public void ShowDataScreen()
    {
        //Getting layout design IDs setup for use.
        ProgressBar bmiBar = (ProgressBar) findViewById(R.id.bmiBar);
        TextView weightVal = (TextView) findViewById(R.id.weightValue);
        TextView heightValFeet = (TextView) findViewById(R.id.heightValue);
        TextView heightValInches = (TextView) findViewById(R.id.userHeightInches);
        TextView bmiVal = (TextView) findViewById(R.id.bmiNumber);

        //Setting BMI bar value equal to BMI calculation.
        bmiBar.setProgress(Math.round(CalculateBMI()));
        bmiVal.setText(Float.toString(CalculateBMI()));

        //Turning user height (inches) into feet' inches" format.
        float heightLeft = userHeight;
        int heightValueFeet = (int)(heightLeft/12);
        heightLeft = (heightLeft/12)-heightValueFeet;
        int heightValueInches = (int)(heightLeft*12);
        heightValFeet.setText(Integer.toString(heightValueFeet)+"\'");
        heightValInches.setText(Integer.toString(heightValueInches)+"\"");
        weightVal.setText(Float.toString(userWeight)+" lbs");
    }

    //Function for setting weight and height from user input.
    public void SetWeightHeight()
    {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        String userHeightStr = heightInput.getText().toString().trim();
        String userWeightStr = weightInput.getText().toString().trim();
        boolean valid = true;
        Float heightValTest;
        Float weightValTest;

        try
        {
            heightValTest = Float.parseFloat(userHeightStr);
            weightValTest = Float.parseFloat(userWeightStr);
        }

        catch (NumberFormatException e)
        {
            valid = false;
        }

        if(valid == false)
        {
            heightInput.setText(Float.toString(userHeight));
            weightInput.setText(Float.toString(userWeight));
        }

        userWeight = Float.parseFloat(weightInput.getText().toString());
        userHeight = Float.parseFloat(heightInput.getText().toString());

        //Saving userWeight/Height to Firebase
        Map<String, Object> userData = new HashMap<>();
        userData.put("Weight", userWeight);
        userData.put("Height", userHeight);
        db.collection("Users").document(user.getUid())
                .update(userData);

    }

    //Function for Setting up Weight and Height input screen for display. Avoiding user leaving input boxes blank.
    //Will display existing values for height and weight.
    public void WeightHeightInputSetup()
    {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        heightInput.setText(Float.toString(userHeight));
        weightInput.setText(Float.toString(userWeight));
    }

    //Function for Saving Weight and Height Changes and logging the new weight and date/time of change.
    public void SetHeightWeightAndLog()
    {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        String userHeightStr = heightInput.getText().toString().trim();
        String userWeightStr = weightInput.getText().toString().trim();
        boolean valid = true;
        Float heightValTest;
        Float weightValTest;

        try
        {
            heightValTest = Float.parseFloat(userHeightStr);
            weightValTest = Float.parseFloat(userWeightStr);
        }

        catch (NumberFormatException e)
        {
            valid = false;
        }

        if(valid == false)
        {
            heightInput.setText(Float.toString(userHeight));
            weightInput.setText(Float.toString(userWeight));
        }

        userWeight = Float.parseFloat(weightInput.getText().toString());
        userHeight = Float.parseFloat(heightInput.getText().toString());

        if(valid == true && userWeight > 50.5)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                bodyWeightChangeLog.add(new BodyWeightLog());
            }
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {}
}