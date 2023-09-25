package darksidearrivals.lifepilot;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    //Routine and goal variables and arrays
    private ArrayList<Button> userRoutines, tempUserRoutines, userGoals, routineDeleteList;
    private ArrayList<String> currentSelectedItems = new ArrayList<>();
    private ArrayList<CheckBox> userRoutineCheck;
    private ArrayList<ArrayList<Button>> userExercisesArrayList, tempUserExercises;
    private ArrayList<ArrayList<TextView>> userGoalArrayList; //Future usage to add multiple goals to one routine
    public int routineIDActive, goalIDActive, playActiveExercise;
    private Scene routineAnimation, homeAnimation, goalAnimation, nRoutineAnimation;

    //Arrays Containing Workout Logs For Each Muscle Group
    //SDK 26>= Cannot utilize LocalDateTime functions, as such, the tracking charts will not be a function for them.
    private ArrayList<LocalDateTime> chestExercisesLog = new ArrayList<>(), shoulderExercisesLog = new ArrayList<>(), bicepExercisesLog = new ArrayList<>(),
            tricepsExercisesLog = new ArrayList<>(), legExercisesLog = new ArrayList<>(), backExercisesLog = new ArrayList<>(), gluteExercisesLog = new ArrayList<>(), abExercisesLog = new ArrayList<>(),
            calvesExercisesLog = new ArrayList<>(), forearmFlexorsGripExercisesLog = new ArrayList<>(), forearmExtensorExercisesLog = new ArrayList<>(), cardioExercisesLog = new ArrayList<>(),
            bodyWeightLog = new ArrayList<>();

    //Creating Class to store weight and time combination for weight tracker.
    public class BodyWeightLog {
        float weightSnapShot;
        LocalDateTime timeLog;

        BodyWeightLog() {
            weightSnapShot = userWeight;

            //SDK 26>= Cannot utilize LocalDateTime functions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timeLog = LocalDateTime.now();
            } else {
                //I can  almost guarantee I might forget to put the version if check on something that uses the timeLog in calculation and crash the app on any version lower than 26.
                //Hopefully not.
                timeLog = null;
            }
        }
    }

    //Creating Array of body weight logs to store logs.
    //Needs to be saved and tracked in FireBase for weight chart to reflect data across app sessions.
    private ArrayList<BodyWeightLog> bodyWeightChangeLog = new ArrayList<>();

    //Setting Up Variables for Weight Chart
    ArrayList weightEntries;
    LineData weightChartData;
    LineDataSet weightDataSet;

    //Variables for muscle group bar chart.
    BarData muscleBarData;
    BarDataSet muscleBarDataSet;
    ArrayList muscleEntriesArrayList;

    BarChart muscleGroupChart;

    //Variables for breakdown pie chart
    ArrayList breakdownEntries;
    PieChart breakdownChart;
    PieData breakdownData;
    PieDataSet breakdownDataSet;



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
        } else {
            setContentView(R.layout.sign_in);
        }

        //User deletion arrays (do not store)
        routineDeleteList = new ArrayList<>();
        tempUserRoutines = new ArrayList<>();
        tempUserExercises = new ArrayList<ArrayList<Button>>();

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
        if (id == R.id.Goal_Button) {
            Transition slide = new Slide(Gravity.RIGHT);
            TransitionManager.go(goalAnimation, slide);
            //Generate Goals
            if (userGoals.size() != 0) {
                LoadUserGoals();
            }
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionGoalLayout), R.layout.routine_list, this);
        } else if (id == R.id.GoalBack_Button) {
            Transition slide = new Slide(Gravity.LEFT);
            TransitionManager.go(routineAnimation, slide);
            if (userRoutines.size() != 0) {
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
            if (userRoutines.size() != 0) {
                LoadUserRoutines();
            }
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.EditRoutineList_Button) {
            //overlay trigger
            if (userRoutines.size() != 0) {
                setContentView(R.layout.routine_list);
                LoadUserRoutines();
                FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
                routineedit.setVisibility(View.VISIBLE);
                //place routines to edit
                DragLinearLayout dragLinearLayout = findViewById(R.id.PlaceEditRoutineList);
                //temp save
                tempUserRoutines.clear();
                tempUserExercises.clear();
                for (int i = 0; i < userRoutines.size(); i++) {
                    tempUserRoutines.add(userRoutines.get(i));
                }
                for (int i = 0; i < userRoutines.size(); i++) {
                    tempUserExercises.add(userExercisesArrayList.get(i));
                    //tempUserExercises.get(i).clear();
//                    for(int j = 0; j < userExercisesArrayList.get(i).size(); j++) {
//                        tempUserExercises.get(i).add(userExercisesArrayList.get(i).get(j));
//                    }
                }
                for (int i = 0; i < userRoutines.size(); i++) {
                    if (userRoutines.get(i).getParent() != null) {
                        ((ViewGroup) userRoutines.get(i).getParent()).removeView(userRoutines.get(i));
                    }
                    LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                    linearLayout.setPadding(10, 10, 10, 10);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(layoutParams);
                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setImageResource(R.drawable.baseline_drag_indicator_24);
                    ViewGroup.LayoutParams layoutParamsForImageView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(layoutParamsForImageView);
                    Button button = userRoutines.get(i);
                    linearLayout.setId(userRoutines.get(i).getId());
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.weight = 1;
                    button.setLayoutParams(layoutParams);
                    linearLayout.addView(imageView);
                    linearLayout.addView(button);
                    dragLinearLayout.addDragView(linearLayout, imageView);
                }
                dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
                    @Override
                    public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                        //routine swap
                        Button temp = userRoutines.get(firstPosition);
                        userRoutines.set(firstPosition, userRoutines.get(secondPosition));
                        userRoutines.set(secondPosition, temp);
                        //exercise swaps
                        ArrayList<Button> hold = userExercisesArrayList.get(firstPosition);
                        userExercisesArrayList.set(firstPosition, userExercisesArrayList.get(secondPosition));
                        userExercisesArrayList.set(secondPosition, hold);
                        //reset id for array reordering
                        if (routineDeleteList.size() != 0) {
                            for (int i = 0; i < routineDeleteList.size(); i++) {
                                routineDeleteList.get(i).setId(i);
                            }
                        }
                        for (int i = 0; i < userRoutines.size(); i++) {
                            userRoutines.get(i).setId(i);
                            if (userExercisesArrayList.get(i).size() != 0) {
                                for (int j = 0; j < userExercisesArrayList.get(i).size(); j++) {
                                    userExercisesArrayList.get(i).set(j, userExercisesArrayList.get(i).get(j));
                                }
                            }
                        }
                    }
                });
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
            //reset params
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            Button button;
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
            layoutParams.setMargins(0, 30, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
            //reset order
            userRoutines.clear();
            userExercisesArrayList.clear();
            for (int i = 0; i < tempUserRoutines.size(); i++) {
                userRoutines.add(tempUserRoutines.get(i));
            }
            for (int i = 0; i < tempUserExercises.size(); i++) {
                userExercisesArrayList.add(tempUserExercises.get(i));
//                userExercisesArrayList.get(i).clear();
//                for(int j = 0; j < tempUserExercises.get(i).size(); j++) {
//                    userExercisesArrayList.get(i).add(tempUserExercises.get(i).get(j));
//                }
            }
            for (int i = 0; i < userRoutines.size(); i++) {
                button = userRoutines.get(i);
                button.setLayoutParams(layoutParams);
                button.setBackground(gradDraw);
                for (int j = 0; j < routineDeleteList.size(); j++) {
                    if (userRoutines.get(i).getId() == routineDeleteList.get(j).getId()) {
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
            Button button;
            for (int i = 0; i < userRoutines.size(); i++) {
                button = userRoutines.get(i);
                if (routineDeleteList.size() != 0) {
                    for (int j = 0; j < routineDeleteList.size(); j++) {
                        if (userRoutines.get(i).getId() == routineDeleteList.get(j).getId()) {
                            userRoutines.remove(i);
                            if (userExercisesArrayList.get(i).size() != 0) {
                                userExercisesArrayList.get(i).clear();
                                userExercisesArrayList.remove(i);
                            }
                        }
                    }
                }
            }
            //reset id for array sorting after deletion
            for (int i = 0; i < userRoutines.size(); i++) {
                userRoutines.get(i).setId(i);
                if (userExercisesArrayList.get(i).size() != 0) {
                    for (int j = 0; j < userExercisesArrayList.get(i).size(); j++) {
                        userExercisesArrayList.get(i).set(j, userExercisesArrayList.get(i).get(j));
                    }
                }
            }
            //reset params
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
            layoutParams.setMargins(0, 30, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
            for (int i = 0; i < userRoutines.size(); i++) {
                button = userRoutines.get(i);
                button.setLayoutParams(layoutParams);
                button.setBackground(gradDraw);
            }
            routineDeleteList.clear();
            //Resync routines and exercises to firbase
            SyncUserRoutines();
            SyncUserRoutineExercises();
            //overlay trigger
            FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
            routineedit.setVisibility(View.GONE);
            LoadUserRoutines();
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.analytics_button) {
            setContentView(R.layout.data_screen);
           ShowDataScreen();
           WorkoutsThisMonth();
        }

        else if(id == R.id.analyticsHomeButton)
        {
            GoToHomeScreen();
        }

        else if (id == R.id.excerciseData)
        {
            //Initiating Spinner for workout breakdown.
            setContentView(R.layout.exercise_data);
            TextView avgDays = (TextView) findViewById(R.id.avgDays);
            GenerateExerciseSpinnerWorkouts();
            muscleGroupChart = findViewById(R.id.monthlyExerciseData);
            WeightChart();
        }

        else if (id == R.id.breakDown)
        {
            //Initiating Spinner for Month Selection
            setContentView(R.layout.muscle_distribution);
            breakdownChart = findViewById(R.id.muscleBreakdown);
            GenerateBreakdownSpinner();
        }

        else if (id == R.id.weight_height_save_button) {
            SetWeightHeight();
            GoToHomeScreen();
        }

        else if (id == R.id.exerciseBack)

        {
            setContentView(R.layout.data_screen);
           ShowDataScreen();
           WorkoutsThisMonth();
        }

        else if (id == R.id.exerciseHomeButton)
        {
            GoToHomeScreen();
        } else if (id == R.id.muscleHomeButton) {
            GoToHomeScreen();
        } else if (id == R.id.muscleBack) {
            setContentView(R.layout.data_screen);
            ShowDataScreen();
            WorkoutsThisMonth();
        }

        else if(id == R.id.log_Save)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                SetHeightWeightAndLog();
            } else {
                SetWeightHeight();
            }

            GoToHomeScreen();
        } else if (id == R.id.Home_Button) {
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
            if (buttontext.getText().length() == 0) {
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
            btn.setPadding(0, 0, 0, 8);
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
            for (int i = 0; i < currentSelectedItems.size(); i++) {
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
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
            for (int i = 0; i < userRoutineCheck.size(); i++) {
                if (userRoutineCheck.get(i).isChecked()) {
                    if (i + 1 > userGoalArrayList.size()) {
                        userGoalArrayList.add(new ArrayList<TextView>());
                    } else if (userGoalArrayList.get(i).size() > 0) {
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
            if (buttontext.getText().length() == 0) {
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
            btn.setPadding(0, 0, 0, 8);
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
            SyncUserRoutines();
        } else if (id == R.id.EditRoutineNewList_Button) {
            //overlay trigger
            if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                //setContentView(R.layout.routine_newlist);
                //GenerateRoutineSelectScreen(id);
                FrameLayout routineedit = findViewById(R.id.routineNewlistoverlayedit);
                routineedit.setVisibility(View.VISIBLE);
                TextView edit = findViewById(R.id.EditRoutineNewList_Text);
                edit.setText("Edit " + userRoutines.get(routineIDActive).getText());
                //place routines to edit
                DragLinearLayout dragLinearLayout = findViewById(R.id.PlaceEditRoutineNewList);
                //temp save
                tempUserExercises.clear();
//                for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
//                    tempUserRoutines.add(userExercisesArrayList.get(routineIDActive).get(i));
//                }
                for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                    if (userExercisesArrayList.get(routineIDActive).get(i).getParent() != null) {
                        ((ViewGroup) userExercisesArrayList.get(routineIDActive).get(i).getParent()).removeView(userExercisesArrayList.get(routineIDActive).get(i));
                    }
                    LinearLayout linearLayout = new LinearLayout(MainActivity.this);
                    linearLayout.setPadding(10, 10, 10, 10);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(layoutParams);
                    ImageView imageView = new ImageView(MainActivity.this);
                    imageView.setImageResource(R.drawable.baseline_drag_indicator_24);
                    ViewGroup.LayoutParams layoutParamsForImageView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    imageView.setLayoutParams(layoutParamsForImageView);
                    Button button = userExercisesArrayList.get(routineIDActive).get(i);
                    //linearLayout.setId(userExercisesArrayList.get(routineIDActive).get(i).getId());
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.weight = 1;
                    button.setLayoutParams(layoutParams);
                    linearLayout.addView(imageView);
                    linearLayout.addView(button);
                    dragLinearLayout.addDragView(linearLayout, imageView);
                }
                dragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
                    @Override
                    public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                        //exercise swaps
                        Button hold = userExercisesArrayList.get(routineIDActive).get(firstPosition);
                        userExercisesArrayList.get(routineIDActive).set(firstPosition, userExercisesArrayList.get(routineIDActive).get(secondPosition));
                        userExercisesArrayList.get(routineIDActive).set(secondPosition, hold);
                    }
                });
                editChecker = 1;
            } else {
                //no routine available to edit message
            }
        } else if (id == R.id.CancelEditRoutineNew_Button) {
            //reset info
            editChecker = 0;
            //reset params
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            Button button;
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
            layoutParams.setMargins(0, 30, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
            //reset order
//            userExercisesArrayList.get(routineIDActive).clear();
//            for (int i = 0; i < tempUserRoutines.size(); i++) {
//                userExercisesArrayList.get(routineIDActive).add(tempUserRoutines.get(i));
//            }
//            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
//                button = userExercisesArrayList.get(routineIDActive).get(i);
//                button.setLayoutParams(layoutParams);
//                button.setBackground(gradDraw);
//                for (int j = 0; j < routineDeleteList.size(); j++) {
//                    if (userExercisesArrayList.get(routineIDActive).get(i).getId() == routineDeleteList.get(j).getId()) {
//                        userExercisesArrayList.get(routineIDActive).get(i).setBackground(gradDraw);
//                    }
//                }
//            }
            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                button = userExercisesArrayList.get(routineIDActive).get(i);
                button.setLayoutParams(layoutParams);
                button.setBackground(gradDraw);
//                for (int j = 0; j < routineDeleteList.size(); j++) {
//                    if (userRoutines.get(i).getId() == routineDeleteList.get(j).getId()) {
//                        userRoutines.get(i).setBackground(gradDraw);
//                    }
//                }
            }
            routineDeleteList.clear();
            //overlay trigger
            FrameLayout routineedit = findViewById(R.id.routineNewlistoverlayedit);
            routineedit.setVisibility(View.GONE);
            GenerateRoutineSelectScreen(routineIDActive);
        } else if (id == R.id.DeleteRoutinesNew_Button) {
            //delete info
            editChecker = 0;
            //reset id for array reordering
            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                userExercisesArrayList.get(routineIDActive).get(i).setId(i);
                if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                    for (int j = 0; j < userExercisesArrayList.get(routineIDActive).size(); j++) {
                        userExercisesArrayList.get(routineIDActive).set(j, userExercisesArrayList.get(routineIDActive).get(j));
                    }
                }
            }
            Button button;
            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                button = userExercisesArrayList.get(routineIDActive).get(i);
                if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                    for (int j = 0; j < routineDeleteList.size(); j++) {
                        if (userExercisesArrayList.get(routineIDActive).get(i).getId() == routineDeleteList.get(j).getId()) {
                            userExercisesArrayList.get(routineIDActive).remove(i);
                            if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                                userExercisesArrayList.get(routineIDActive).remove(i);
                            }
                        }
                    }
                }
            }
            //reset id for array sorting after deletion
            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                userExercisesArrayList.get(routineIDActive).get(i).setId(i);
                if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                    for (int j = 0; j < userExercisesArrayList.get(routineIDActive).size(); j++) {
                        userExercisesArrayList.get(routineIDActive).set(j, userExercisesArrayList.get(routineIDActive).get(j));
                    }
                }
            }
            //reset params
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
            layoutParams.setMargins(0, 30, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
            for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
                button = userExercisesArrayList.get(routineIDActive).get(i);
                button.setLayoutParams(layoutParams);
                button.setBackground(gradDraw);
            }
            routineDeleteList.clear();
            //overlay trigger
            FrameLayout routineedit = findViewById(R.id.routinelistoverlayedit);
            routineedit.setVisibility(View.GONE);
            GenerateRoutineSelectScreen(routineIDActive);
        } else if (editChecker == 1) { //TODO: Fix
            //if selected
            GradientDrawable gradDraw = new GradientDrawable();
            gradDraw.setShape(GradientDrawable.RECTANGLE);
            gradDraw.setCornerRadius(100);
            gradDraw.setColor(getResources().getColor(R.color.deleteRed));
            if (routineDeleteList.contains(userExercisesArrayList.get(routineIDActive).get(id))) {
                gradDraw.setColor(getResources().getColor(R.color.royalPurple));
                userExercisesArrayList.get(routineIDActive).get(id).setBackground(gradDraw);
                //remove from array
                routineDeleteList.remove(userExercisesArrayList.get(routineIDActive).get(id));
            } else {
                userExercisesArrayList.get(routineIDActive).get(id).setBackground(gradDraw);
                //add to array
                routineDeleteList.add(userExercisesArrayList.get(routineIDActive).get(id));
            }
        } else if (id < userRoutines.size() || id == userRoutines.size()) {
            Transition slide = new Slide(Gravity.RIGHT);
            TransitionManager.go(nRoutineAnimation, slide);
            GenerateRoutineSelectScreen(id);
            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
        } else if (id == R.id.RoutineDynamicbackButton) {
            if (dynamicChecker == 0) {
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
            if (userRoutines.size() == 0) {
                TextView rl = findViewById(R.id.NoGoal_TopText);
                rl.setVisibility(View.VISIBLE);
            } else {
                TextView rl = findViewById(R.id.NoGoal_TopText);
                rl.setVisibility(View.GONE);
            }
            //Generate goal name
            int goalIdActive = id;
            TextView titletext = findViewById(R.id.GoalToRoutine_TopText);
            for (int i = 0; i < userGoals.size(); i++) {
                if (userGoals.get(i).getId() == id) {
                    titletext.setText(userGoals.get(i).getText());
                }
            }
            //list routines to add
            LinearLayout ll = findViewById(R.id.GoalRoutineListHere);
            Button temp;
            for (int i = 0; i < userRoutines.size(); i++) {
                temp = (userRoutines.get(i));
                if (temp.getParent() != null) {
                    ((ViewGroup) temp.getParent()).removeView(temp);
                }
                ll.addView(temp);
            }
            //list checkboxes to add
            LinearLayout ll2 = findViewById(R.id.GoalCheckRoutineListHere);
            CheckBox temp2;
            for (int i = 0; i < userRoutines.size(); i++) {
                temp2 = (userRoutineCheck.get(i));
                if (temp2.getParent() != null) {
                    ((ViewGroup) temp2.getParent()).removeView(temp2);
                }
                ll2.addView(temp2);
            }
        } else if (id == R.id.PreMadeRoutine1_Button) { //TODO: Finish premade routine lists
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Leg Training");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Hack Squat Machine");
            currentSelectedItems.add("Hip Adduction Machine");
            currentSelectedItems.add("Seated Leg Curl");
            currentSelectedItems.add("Leg extension");
            currentSelectedItems.add("Seated Calf Raise");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine2_Button) {
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Arm Workouts");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Cable curl with rope");
            currentSelectedItems.add("Machine bicep curl");
            currentSelectedItems.add("Barbell preacher curl");
            currentSelectedItems.add("Overhead cable triceps extension");
            currentSelectedItems.add("Tricep push down with bar");
            currentSelectedItems.add("Barbell wrist curl behind the back");
            currentSelectedItems.add("Barbell wrist extension");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine3_Button) {
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Shoulder and Back");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Face Pull");
            currentSelectedItems.add("Front hold");
            currentSelectedItems.add("Machine lateral raise");
            currentSelectedItems.add("Floor back extension");
            currentSelectedItems.add("Kettlebell swing");
            currentSelectedItems.add("Seated Machine Row");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine4_Button) {
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Chest Workouts");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Floor press");
            currentSelectedItems.add("Machine chest fly");
            currentSelectedItems.add("Machine chest press");
            currentSelectedItems.add("Pec deck");
            currentSelectedItems.add("Inclined smith machine bench press");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine5_Button) {
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Glutes and Abs");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Banded Side Kicks");
            currentSelectedItems.add("Clamshells");
            currentSelectedItems.add("Standing flute kickback in machine");
            currentSelectedItems.add("Cable crunch");
            currentSelectedItems.add("Hanging knee raise");
            currentSelectedItems.add("Machine crunch");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.PreMadeRoutine6_Button) {
            setContentView(R.layout.routine_newlist);
            Button newExercise = findViewById(R.id.NewExerciseAdd_Button);
            newExercise.setVisibility(View.GONE);
            TextView topText = findViewById(R.id.NewRoutineSet_TopText);
            topText.setText("Full Body Session");
            TextView goalText = findViewById(R.id.GoalofRoutine_TopText);
            goalText.setVisibility(View.GONE);
            //load premade exercise array
            currentSelectedItems.clear();
            currentSelectedItems.add("Smithing Machine Bench Press");
            currentSelectedItems.add("Machine Shoulder Press");
            currentSelectedItems.add("Machine Bicep Curl");
            currentSelectedItems.add("Tricep Pushdown with Bar");
            currentSelectedItems.add("Leg Press");
            currentSelectedItems.add("Back Extension");
            currentSelectedItems.add("Banded Side Kicks");
            currentSelectedItems.add("Machine Crunch");
            currentSelectedItems.add("Seated Calf Raise");
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
                btn.setPadding(20, 0, 20, 8);
                if (btn.getText().length() > 23) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 240);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                    params.setMargins(0, 30, 0, 0);
                    btn.setLayoutParams(params);
                    ll.addView(btn);
                }
            }
            currentSelectedItems.clear();
            //make save, a save to routine array list and sync
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.activity_main, this);
            routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
        } else if (id == R.id.ExerciseSave_Button) {
            //TODO: Sync to firebase
            SyncUserRoutines();
            SyncUserRoutineExercises();
            Transition slide = new Slide(Gravity.LEFT);
            TransitionManager.go(routineAnimation, slide);

            //setup next button animations
            goalAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_goals, this);
            homeAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.activity_main, this);
            nRoutineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionRoutineLayout), R.layout.routine_newlist, this);
           // setContentView(R.layout.routine_list);
            LoadUserRoutines();
        } else if (id == R.id.StartRoutine_Button) {
            if (userExercisesArrayList.get(routineIDActive).size() != 0) {
                //set what exercise is active
                playActiveExercise = 0;
                DynamicPlayWorkouts((String) userExercisesArrayList.get(routineIDActive).get(playActiveExercise).getText());
            }
        } else if (id == R.id.PlayBackButton) {
            if (0 != playActiveExercise) {
                playActiveExercise = playActiveExercise - 1;
                DynamicPlayWorkouts((String) userExercisesArrayList.get(routineIDActive).get(playActiveExercise).getText());
            } else {
                //go back to routine screen
            }
        } else if (id == R.id.PlayNextWorkout) {
            if (userExercisesArrayList.get(routineIDActive).size() != playActiveExercise + 1) {
                playActiveExercise = playActiveExercise + 1;
                DynamicPlayWorkouts((String) userExercisesArrayList.get(routineIDActive).get(playActiveExercise).getText());
            } else {
                //go to end screen once completed
            }
        } else if (id == R.id.calendar_button) {
            setContentView(R.layout.calendar_screen);
        } else if (id == R.id.googleSignIn) {
            signIn();
            //setContentView(R.layout.experience_selection);
        } else if (id == R.id.temp_logout_button) {
            signOut();
            //setContentView(R.layout.experience_selection);
        } else if (id == R.id.still_learning_button) {
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
        } else if (id == R.id.profile_pic) {
            setContentView(R.layout.profile_page);
            //onClick to setting page from profile
        } else if (id == R.id.button6) {
            setContentView(R.layout.setting_page);
            //onClick to support from setting page
        } else if (id == R.id.Support) {
            setContentView(R.layout.support_page1);
        }

        else {
            GoToHomeScreen();
        }
    }


    private void GoToHomeScreen() {
        setContentView(R.layout.activity_main);
        routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionHomeLayout), R.layout.routine_list, this);

        //Updating Home Screen text
        if (account != null && user != null) {
            //Updating welcome text
            TextView welcome_text = findViewById(R.id.welcome_text);
            String name = user.getDisplayName();
            welcome_text.setText("Welcome, " + name + "!");

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
                                if (document.exists()) {
                                    goal_text.setText("Goal: " + document.getData().get("Goal"));
                                } else {
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
        for (int i = 0; i < userRoutines.size(); i++) {
            if (userRoutines.get(i).getId() == id) {
                titletext.setText(userRoutines.get(i).getText());
            }
        }
        //Check goals
        if (userGoals.size() != 0) {
            if (routineIDActive + 1 > userGoalArrayList.size()) {
                TextView goal = findViewById(R.id.GoalofRoutine_TopText);
                goal.setVisibility(View.GONE);
            } else if (userGoalArrayList.get(routineIDActive) != null) {
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
        if (userExercisesArrayList.get(routineIDActive).size() != 0) {
            LoadUserRoutineExercises();
        }
        GenerateSpinnerWorkouts();
        GenerateWorkoutRecycler();
        //button usage animations
        routineAnimation = Scene.getSceneForLayout(findViewById(R.id.TransitionNewRoutineLayout), R.layout.routine_list, this);
    }

    void KeyboardVanish(View view) {
        //if keyboard doesn't go away
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    void GenerateSpinnerWorkouts() {
        //Set workout spinner
        spinner = findViewById(R.id.exerciseSpin);
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
        try {
            String textLine = textReader.readLine();
            while (textLine != null) {
                String[] columns = textLine.split("\\|");
                String exerciseName = columns[0];
                if (exerciseName.equals(exercise)) {
                    String exerciseDesc = columns[1];
                    workoutDesc.setText(exerciseDesc);
                    break;
                }
                textLine = textReader.readLine();
            }
            textReader.close();
        } catch (IOException E) {
            E.printStackTrace();
        }
        String imageName = exercise.replaceAll(" ", "").replaceAll("-", "").toLowerCase();
        int resourceId = getResources().getIdentifier(imageName, "raw", getBaseContext().getPackageName());
        workoutImage.setImageResource(resourceId);
    }

    void DynamicPlayWorkouts(String string) {
        setContentView(R.layout.activity_dynamic_play_workout_screen);
        String exercise = string;
        TextView workoutTitle = findViewById(R.id.workoutTitle);
        workoutTitle.setText(exercise);
        TextView workoutDesc = findViewById(R.id.workoutDesc);
        GifImageView workoutImage = findViewById(R.id.workoutPic);
        InputStream textFile = getResources().openRawResource(R.raw.workoutdesc);
        BufferedReader textReader = new BufferedReader(new InputStreamReader(textFile));
        try {
            String textLine = textReader.readLine();
            while (textLine != null) {
                String[] columns = textLine.split("\\|");
                String exerciseName = columns[0];
                if (exerciseName.equals(exercise)) {
                    String exerciseDesc = columns[1];
                    workoutDesc.setText(exerciseDesc);
                    break;
                }
                textLine = textReader.readLine();
            }
            textReader.close();
        } catch (IOException E) {
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
                DynamicScreenWorkouts((String) btn.getText());
            }
        };
    }

    //Google Sign in functions
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 1200; /* unique request id */

    void signIn() {
        //Start Sign in process
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    void signOut() {
        FirebaseAuth.getInstance().signOut();
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                setContentView(R.layout.sign_in);
            }
        });
    }

    void performAccountStartUp() {
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
                        if (!document.contains("Experience")) {
                            setContentView(R.layout.experience_selection);
                        } else if (!document.contains("Goal")) {
                            setContentView(R.layout.goal_selection);
                        } else if (!document.contains("Weight") || !document.contains("Height")) {
                            setContentView(R.layout.weight_height_input);
                        } else {
                            //Returning User, Sync with Firebase
                            db.collection("Users").document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    //Getting user weight and height
                                                    double weight = ((double) document.getData().get("Weight"));
                                                    double height = ((double) document.getData().get("Height"));
                                                    userWeight = (float) weight;
                                                    userHeight = (float) height;

                                                    //Getting user routines
                                                    if (document.getData().get("Routines") != null) {
                                                        ArrayList<String> routineNames = (ArrayList<String>) document.getData().get("Routines");
                                                        for (int i=0; i<routineNames.size(); i++){
                                                            //button creation
                                                            //LinearLayout ll = findViewById(R.id.RoutineButtonAddsHere);
                                                            Button btn = new Button(MainActivity.this);
                                                            btn.setAllCaps(false);
                                                            btn.setText(routineNames.get(i));
                                                            btn.setTextSize(24);
                                                            btn.setTextColor(Color.WHITE);
                                                            btn.setClickable(true);
                                                            btn.setOnClickListener(MainActivity.this::onClick);
                                                            GradientDrawable gradDraw = new GradientDrawable();
                                                            gradDraw.setShape(GradientDrawable.RECTANGLE);
                                                            gradDraw.setCornerRadius(100);
                                                            gradDraw.setColor(getResources().getColor(R.color.royalPurple));
                                                            btn.setBackground(gradDraw);
                                                            btn.setPadding(0,0,0,8);
                                                            btn.setId(i);
                                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                                                            params.setMargins(0, 30, 0, 0);
                                                            btn.setLayoutParams(params);
                                                            //added to routine list
                                                            userRoutines.add(btn);
                                                            //New Exercise Array
                                                            userExercisesArrayList.add(new ArrayList<Button>());
                                                        }
                                                    }

                                                    //Getting user routines exercises
                                                    if (document.getData().get("Routines") != null) {
                                                        Map<String, Object> firebaseExerciseArrays = (Map<String, Object>) document.getData().get("Exercises");
                                                        for (int i=0; i<firebaseExerciseArrays.size(); i++) {
                                                            ArrayList<String> userRoutineExerciseNames = (ArrayList<String>) firebaseExerciseArrays.get(Integer.toString(i));
                                                            for (int j = 0; j < userRoutineExerciseNames.size(); j++) {
                                                                Button btn = new Button(MainActivity.this);
                                                                btn.setText(userRoutineExerciseNames.get(j));
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
                                                                    userExercisesArrayList.get(i).add(btn);
                                                                } else {
                                                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                                                                    params.setMargins(0, 30, 0, 0);
                                                                    btn.setLayoutParams(params);
                                                                    userExercisesArrayList.get(i).add(btn);
                                                                }
                                                            }
                                                        }
                                                    }
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

    private void SyncUserRoutines() {
        //Saving button info from UserRoutines to firebase
        Map<String, Object> userArray = new HashMap<>();
        ArrayList<String> userStringRoutines = new ArrayList<>();
        for (int i=0; i<userRoutines.size(); i++){
            userStringRoutines.add(userRoutines.get(i).getId(),userRoutines.get(i).getText().toString());
        }
        userArray.put("Routines", userStringRoutines);
        db.collection("Users").document(user.getUid())
                .update(userArray);
    }

    private void SyncUserRoutineExercises(){
        //Saving button info from UserExercisesArrayList to firebase
        ArrayList<ArrayList<String>> userRoutineExerciseNames = new ArrayList<ArrayList<String>>();
        for (int i=0; i<userExercisesArrayList.size(); i++){
            userRoutineExerciseNames.add(new ArrayList<String>());
            for (int j=0; j<userExercisesArrayList.get(i).size(); j++){
                userRoutineExerciseNames.get(i).add(userExercisesArrayList.get(i).get(j).getText().toString());
            }

        }
        Map<String, Object> firebaseExerciseArrays = new HashMap<>();

        for (int i=0; i<userRoutineExerciseNames.size(); i++) {
            firebaseExerciseArrays.put(Integer.toString(i), userRoutineExerciseNames.get(i));
        }
        Map<String, Object> userArray = new HashMap<>();
        userArray.put("Exercises", firebaseExerciseArrays);
        db.collection("Users").document(user.getUid())
                .update(userArray);
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
                if (idToken != null) {
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
            if (userRoutines.get(i).getParent() != null) {
                ((ViewGroup) userRoutines.get(i).getParent()).removeView(userRoutines.get(i));
            }
            ll.addView(userRoutines.get(i));
        }
    }

    //load custom goals
    public void LoadUserGoals() {
        LinearLayout ll = findViewById(R.id.RoutineGoalButtonAddsHere);
        for (int i = 0; i < userGoals.size(); i++) {
            if (userGoals.get(i).getParent() != null) {
                ((ViewGroup) userGoals.get(i).getParent()).removeView(userGoals.get(i));
            }
            ll.addView(userGoals.get(i));
        }
    }

    //load custom exercises per routine
    public void LoadUserRoutineExercises() {
        LinearLayout ll = findViewById(R.id.ExerciseButtonAddsHere);
        Button temp;
        for (int i = 0; i < userExercisesArrayList.get(routineIDActive).size(); i++) {
            temp = (userExercisesArrayList.get(routineIDActive).get(i));
            if (temp.getParent() != null) {
                ((ViewGroup) temp.getParent()).removeView(temp);
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
    private String[] calvesExercises = new String[]{"Eccentric Heel Drop",
            "Heel Raise",
            "Seated Calf Raise",
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

    //Array for month select
    private String[] monthSelect = new String[]
        {
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        };

    private int[] colors = new int[]
        {   Color.rgb(162,25,255),
            Color.rgb(186,85,255),
            Color.GRAY,
            Color.rgb(219,165,255),
            Color.LTGRAY,
            Color.rgb(120, 81,169),
            Color.DKGRAY,
            Color.rgb(60, 0, 100),
            Color.rgb(227,185,255),
            Color.rgb(174,55,255),
            Color.rgb(95,0,160),
            Color.rgb(105,105,105),
            Color.rgb(198,115,255)
        };

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        //Editing for usage with multiple spinners.
        int spinId = spinner.getId();
        if(spinId == R.id.exerciseSpin)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Chest Exercises")) {
                workoutList.myWorkouts = chestExercises;
            } else if (currentSel.equals("Shoulder Exercises")) {
                workoutList.myWorkouts = shoulderExercises;
            } else if (currentSel.equals("Bicep Exercises")) {
                workoutList.myWorkouts = bicepExercises;
            } else if (currentSel.equals("Triceps Exercises")) {
                workoutList.myWorkouts = tricepsExercises;
            } else if (currentSel.equals("Leg Exercises")) {
                workoutList.myWorkouts = legExercises;
            } else if (currentSel.equals("Back Exercises")) {
                workoutList.myWorkouts = backExercises;
            } else if (currentSel.equals("Glute Exercises")) {
                workoutList.myWorkouts = gluteExercises;
            } else if (currentSel.equals("Ab Exercises")) {
                workoutList.myWorkouts = abExercises;
            } else if (currentSel.equals("Calves Exercises")) {
                workoutList.myWorkouts = calvesExercises;
            } else if (currentSel.equals("Forearm Flexors and Grip Exercises")) {
                workoutList.myWorkouts = forearmFlexExercises;
            } else if (currentSel.equals("Forearm Extensor Exercises")) {
                workoutList.myWorkouts = forearmExtExercises;
            } else if (currentSel.equals("Cardio Exercises")) {
                workoutList.myWorkouts = cardioExercises;
            } else if (currentSel.equals("Body Weight")) {
                workoutList.myWorkouts = bodyweight;
            }
            workoutList.notifyDataSetChanged();
        }

        if(spinId == R.id.exerciseSpinner)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Chest Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Shoulder Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Bicep Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Triceps Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Leg Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Back Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Glute Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Ab Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Calves Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Forearm Flexors and Grip Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Forearm Extensor Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Cardio Exercises"))
            {
                GenerateMuscleChart();
            }

            else if (currentSel.equals("Body Weight"))
            {
                GenerateMuscleChart();
            }
        }

        if(spinId == R.id.monthSelect)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("January")) {
                GenerateBreakdown();
            }

            if (currentSel.equals("February"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("March"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("April"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("May"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("June"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("July"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("August"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("September"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("October"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("November"))
            {
                GenerateBreakdown();
            }

            if (currentSel.equals("December"))
            {
                GenerateBreakdown();
            }
        }
    }

    //Calculate BMI (Imperial)
    public float CalculateBMI() {
        float bmi;

        if (userHeight != 0 && userWeight != 0) {
            bmi = 703 * (userWeight / (userHeight * userHeight));
            BigDecimal bmiD = new BigDecimal(bmi);
            bmiD = bmiD.setScale(1, RoundingMode.HALF_UP);
            bmi = bmiD.floatValue();
        } else {
            bmi = 0;
        }

        return bmi;
    }

    //Self-explanatory, but just in case, function for displaying values on data screen.
    public void ShowDataScreen() {
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
        BigDecimal heightValRound = new BigDecimal(heightLeft*12);
        heightValRound = heightValRound.setScale(0, RoundingMode.HALF_UP);
        heightLeft = heightValRound.floatValue();
        int heightValueInches = (int)(heightLeft);
        heightValFeet.setText(Integer.toString(heightValueFeet)+"\'");
        heightValInches.setText(Integer.toString(heightValueInches)+"\"");
        weightVal.setText(Float.toString(userWeight)+" lbs");
    }

    //Function for setting weight and height from user input.
    public void SetWeightHeight() {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        String userHeightStr = heightInput.getText().toString().trim();
        String userWeightStr = weightInput.getText().toString().trim();
        boolean valid = true;
        Float heightValTest;
        Float weightValTest;

        try {
            heightValTest = Float.parseFloat(userHeightStr);
            weightValTest = Float.parseFloat(userWeightStr);
        } catch (NumberFormatException e) {
            valid = false;
        }

        if (valid == false) {
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
    public void WeightHeightInputSetup() {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        heightInput.setText(Float.toString(userHeight));
        weightInput.setText(Float.toString(userWeight));
    }

    //Function for Saving Weight and Height Changes and logging the new weight and date/time of change.
    public void SetHeightWeightAndLog() {
        TextView heightInput = (TextView) findViewById(R.id.height_input);
        TextView weightInput = (TextView) findViewById(R.id.weight_input);
        String userHeightStr = heightInput.getText().toString().trim();
        String userWeightStr = weightInput.getText().toString().trim();
        boolean valid = true;
        Float heightValTest;
        Float weightValTest;

        try {
            heightValTest = Float.parseFloat(userHeightStr);
            weightValTest = Float.parseFloat(userWeightStr);
        } catch (NumberFormatException e) {
            valid = false;
        }

        if (valid == false) {
            heightInput.setText(Float.toString(userHeight));
            weightInput.setText(Float.toString(userWeight));
        }

        userWeight = Float.parseFloat(weightInput.getText().toString());
        userHeight = Float.parseFloat(heightInput.getText().toString());

        if (valid == true && userWeight > 50.5) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bodyWeightChangeLog.add(new BodyWeightLog());
            }
        }

        //Saving userWeight/Height to Firebase
        Map<String, Object> userData = new HashMap<>();
        userData.put("Weight", userWeight);
        userData.put("Height", userHeight);
        db.collection("Users").document(user.getUid())
                .update(userData);
    }

    //Function to construct weight tracking chart when data screen is displayed.
    public void WeightChart()
    {
        //If OS version allows for localdatetime functions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            LineChart weightTracker = findViewById(R.id.monthlyWeightMonitor);
            weightEntries = new ArrayList<>();
            if (bodyWeightChangeLog.size() != 0)
            {
                for (int i = 0; i < bodyWeightChangeLog.size(); i++)
                {
                    //If current year and month matches year and month of log. Reflecting data on monthly basis.
                    if(LocalDateTime.now().getYear() == bodyWeightChangeLog.get(i).timeLog.getYear() && LocalDateTime.now().getMonthValue() == bodyWeightChangeLog.get(i).timeLog.getMonthValue())
                    {
                        weightEntries.add(new Entry(bodyWeightChangeLog.get(i).timeLog.getDayOfMonth(), bodyWeightChangeLog.get(i).weightSnapShot));
                        //For each log, create a new entry with x position at the int of day of month and y at calorie value.

                        //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                        Collections.sort(weightEntries, new EntryXComparator());
                    }
                }


            }
            weightEntries.add(new Entry(3, 128));
            weightEntries.add(new Entry(5, 133));
            weightEntries.add(new Entry(10, 111));
            weightEntries.add(new Entry(7, 122));
            weightEntries.add(new Entry(21, 143));
            weightEntries.add(new Entry(25, 150));
            weightEntries.add(new Entry(1, 128));
            weightEntries.add(new Entry(3, 130));
            //Force Feeding Data for showcasing purposes, as WeightChangeLog is not synced to FireBase,
            //Thus, does not contain enough dataa for showcasing.

            //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
            Collections.sort(weightEntries, new EntryXComparator());

            weightDataSet = new LineDataSet(weightEntries, "Weight");
            weightChartData = new LineData(weightDataSet);
            weightTracker.setData(weightChartData);
            weightTracker.animateX(1000);
            weightDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            weightDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            weightDataSet.setLineWidth(2);
            weightDataSet.setColor(Color.MAGENTA);
            weightDataSet.setHighLightColor(Color.WHITE);
            weightDataSet.setValueTextColor(Color.WHITE);
            weightDataSet.setValueTextSize(12f);
            weightDataSet.setDrawFilled(true);
            weightDataSet.setFillColor(Color.rgb(120, 81, 169));

            //Setting Y axis color to white.
            weightTracker.getAxisLeft().setTextColor(Color.WHITE);
            weightTracker.getAxisLeft().setTextSize(12);
            weightTracker.getAxisRight().setTextColor(Color.WHITE);
            weightTracker.getAxisRight().setTextSize(12);

            // Setup X Axis
            XAxis dayofMonth = weightTracker.getXAxis();
            dayofMonth.setPosition(XAxis.XAxisPosition.TOP);
            dayofMonth.setGranularityEnabled(true);
            dayofMonth.setGranularity(1.0f);
            dayofMonth.setXOffset(1f);
            dayofMonth.setAxisMinimum(1);
            dayofMonth.setAxisMaximum(31);
            dayofMonth.setTextColor(Color.WHITE);
            dayofMonth.setTextSize(12);

            weightTracker.getLegend().setEnabled(false);
            weightTracker.invalidate();
        }
    }

    public void WorkoutsThisMonth()
    {
        int workedTotal = 0;
        CircularProgressIndicator daysWorked = (CircularProgressIndicator) findViewById(R.id.daysWorked);
        TextView daysWorkedText = (TextView)  findViewById(R.id.daysWorkedText);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            daysWorked.setMin(0);
            //Checking various muscle groups for entries on each date of month.
            if (chestExercisesLog.size() != 0)
            {
               //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < chestExercisesLog.size(); i++)
                {
                //Looping through Array
                    if (LocalDateTime.now().getYear() == chestExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == chestExercisesLog.get(i).getMonthValue())
                    {
                    //If year and month are current year and month.
                        if(i != 0)
                        {
                        //If this is not the first or only entry.
                            if (chestExercisesLog.get(i).getDayOfMonth() != chestExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }
            }

            if (shoulderExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < shoulderExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == shoulderExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == shoulderExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (shoulderExercisesLog.get(i).getDayOfMonth() != shoulderExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }
            }

            if (bicepExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < bicepExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == bicepExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == bicepExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (bicepExercisesLog.get(i).getDayOfMonth() != bicepExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (tricepsExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < tricepsExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == tricepsExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == tricepsExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (tricepsExercisesLog.get(i).getDayOfMonth() != tricepsExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (legExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < legExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == legExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == legExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (legExercisesLog.get(i).getDayOfMonth() != legExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (backExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < backExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == backExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == backExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (backExercisesLog.get(i).getDayOfMonth() != backExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (gluteExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < gluteExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == gluteExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == gluteExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (gluteExercisesLog.get(i).getDayOfMonth() != gluteExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (abExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < abExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == abExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == abExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (abExercisesLog.get(i).getDayOfMonth() != abExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (calvesExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < calvesExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == calvesExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == calvesExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (calvesExercisesLog.get(i).getDayOfMonth() != calvesExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (forearmFlexorsGripExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < forearmFlexorsGripExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == forearmFlexorsGripExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == forearmFlexorsGripExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (forearmFlexorsGripExercisesLog.get(i).getDayOfMonth() != forearmFlexorsGripExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (forearmExtensorExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < forearmExtensorExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == forearmExtensorExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == forearmExtensorExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (forearmExtensorExercisesLog.get(i).getDayOfMonth() != forearmExtensorExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (cardioExercisesLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < cardioExercisesLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == cardioExercisesLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == cardioExercisesLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (cardioExercisesLog.get(i).getDayOfMonth() != cardioExercisesLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }

            if (bodyWeightLog.size() != 0)
            {
                //Data should already be inputted in order of time, so sorting shouldn't be necessary.
                for (int i = 0; i < bodyWeightLog.size(); i++)
                {
                    //Looping through Array
                    if (LocalDateTime.now().getYear() == bodyWeightLog.get(i).getYear() && LocalDateTime.now().getMonthValue() == bodyWeightLog.get(i).getMonthValue())
                    {
                        //If year and month are current year and month.
                        if(i != 0)
                        {
                            //If this is not the first or only entry.
                            if (bodyWeightLog.get(i).getDayOfMonth() != bodyWeightLog.get(i - 1).getDayOfMonth())
                            {
                                //If the entry does not have the same date as the entry before it. If entries are in chronological order, which they should be,
                                //Only the first entry of each day should count.
                                workedTotal++;
                            }
                        }

                        else
                        {
                            //If it is the first entry or the only entry, it automatically counts.
                            workedTotal++;
                        }

                        //I hate nested if checks.
                    }
                }

            }
        }

        //Hardcoding total for demonstrative purposes.
        workedTotal = 27;

        //Passing in Progress value.
        daysWorked.setProgressCompat(workedTotal, true);
        daysWorkedText.setText(Integer.toString(workedTotal));
    }

    public void GenerateMuscleChart()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Chest Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (chestExercisesLog.size() != 0)
                {
                    for (int o = 0; o < chestExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == chestExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == chestExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (chestExercisesLog.get(o).getDayOfMonth() != chestExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(chestExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(8f, 11));
                muscleEntriesArrayList.add(new BarEntry(5f, 3f));
                muscleEntriesArrayList.add(new BarEntry(10, 4));
                muscleEntriesArrayList.add(new BarEntry(8, 5));
                muscleEntriesArrayList.add(new BarEntry(21, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 4));
                muscleEntriesArrayList.add(new BarEntry(1, 2));
                muscleEntriesArrayList.add(new BarEntry(3, 4));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 36;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Shoulder Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (shoulderExercisesLog.size() != 0)
                {
                    for (int o = 0; o < shoulderExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == shoulderExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == shoulderExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (shoulderExercisesLog.get(o).getDayOfMonth() != shoulderExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(shoulderExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(6f, 1));
                muscleEntriesArrayList.add(new BarEntry(3f, 5f));
                muscleEntriesArrayList.add(new BarEntry(10, 4));
                muscleEntriesArrayList.add(new BarEntry(8, 5));
                muscleEntriesArrayList.add(new BarEntry(21, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 4));
                muscleEntriesArrayList.add(new BarEntry(1, 2));
                muscleEntriesArrayList.add(new BarEntry(8, 9));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 42;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Bicep Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (bicepExercisesLog.size() != 0)
                {
                    for (int o = 0; o < bicepExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == bicepExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == bicepExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (bicepExercisesLog.get(o).getDayOfMonth() != bicepExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(bicepExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(6f, 1));
                muscleEntriesArrayList.add(new BarEntry(3f, 5f));
                muscleEntriesArrayList.add(new BarEntry(22, 4));
                muscleEntriesArrayList.add(new BarEntry(4, 21));
                muscleEntriesArrayList.add(new BarEntry(11, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 4));
                muscleEntriesArrayList.add(new BarEntry(30, 14));
                muscleEntriesArrayList.add(new BarEntry(8, 9));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 34;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Triceps Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (tricepsExercisesLog.size() != 0)
                {
                    for (int o = 0; o < tricepsExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == tricepsExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == tricepsExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (tricepsExercisesLog.get(o).getDayOfMonth() != tricepsExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(tricepsExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(6f, 12));
                muscleEntriesArrayList.add(new BarEntry(5f, 2f));
                muscleEntriesArrayList.add(new BarEntry(12, 3));
                muscleEntriesArrayList.add(new BarEntry(3, 11));
                muscleEntriesArrayList.add(new BarEntry(1, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 4));
                muscleEntriesArrayList.add(new BarEntry(30, 14));
                muscleEntriesArrayList.add(new BarEntry(15, 9));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 12;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Leg Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (legExercisesLog.size() != 0)
                {
                    for (int o = 0; o < legExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == legExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == legExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (legExercisesLog.get(o).getDayOfMonth() != legExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(legExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(3f, 7));
                muscleEntriesArrayList.add(new BarEntry(5f, 3f));
                muscleEntriesArrayList.add(new BarEntry(12, 3));
                muscleEntriesArrayList.add(new BarEntry(15, 14));
                muscleEntriesArrayList.add(new BarEntry(1, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 4));
                muscleEntriesArrayList.add(new BarEntry(30, 14));
                muscleEntriesArrayList.add(new BarEntry(16, 9));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 6;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Back Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (backExercisesLog.size() != 0)
                {
                    for (int o = 0; o < backExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == backExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == backExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (backExercisesLog.get(o).getDayOfMonth() != backExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(backExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(13f, 3));
                muscleEntriesArrayList.add(new BarEntry(17f, 11f));
                muscleEntriesArrayList.add(new BarEntry(12, 3));
                muscleEntriesArrayList.add(new BarEntry(15, 14));
                muscleEntriesArrayList.add(new BarEntry(3, 12));
                muscleEntriesArrayList.add(new BarEntry(25, 7));
                muscleEntriesArrayList.add(new BarEntry(10, 14));
                muscleEntriesArrayList.add(new BarEntry(16, 9));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 55;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Glute Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (gluteExercisesLog.size() != 0)
                {
                    for (int o = 0; o < gluteExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == gluteExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == gluteExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (gluteExercisesLog.get(o).getDayOfMonth() != gluteExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(gluteExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(15f, 3));
                muscleEntriesArrayList.add(new BarEntry(17f, 11f));
                muscleEntriesArrayList.add(new BarEntry(11, 21));
                muscleEntriesArrayList.add(new BarEntry(18, 4));
                muscleEntriesArrayList.add(new BarEntry(3, 12));
                muscleEntriesArrayList.add(new BarEntry(25, 13));
                muscleEntriesArrayList.add(new BarEntry(18, 11));
                muscleEntriesArrayList.add(new BarEntry(29, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 15;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Ab Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (abExercisesLog.size() != 0)
                {
                    for (int o = 0; o < abExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == abExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == abExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (abExercisesLog.get(o).getDayOfMonth() != abExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(abExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(1f, 5));
                muscleEntriesArrayList.add(new BarEntry(3f, 1f));
                muscleEntriesArrayList.add(new BarEntry(6, 12));
                muscleEntriesArrayList.add(new BarEntry(18, 4));
                muscleEntriesArrayList.add(new BarEntry(14, 13));
                muscleEntriesArrayList.add(new BarEntry(25, 13));
                muscleEntriesArrayList.add(new BarEntry(22, 13));
                muscleEntriesArrayList.add(new BarEntry(29, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 26;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Calves Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (calvesExercisesLog.size() != 0)
                {
                    for (int o = 0; o < calvesExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == calvesExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == calvesExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (calvesExercisesLog.get(o).getDayOfMonth() != calvesExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(calvesExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(11f, 5));
                muscleEntriesArrayList.add(new BarEntry(3f, 1f));
                muscleEntriesArrayList.add(new BarEntry(6, 12));
                muscleEntriesArrayList.add(new BarEntry(7, 7));
                muscleEntriesArrayList.add(new BarEntry(14, 13));
                muscleEntriesArrayList.add(new BarEntry(25, 13));
                muscleEntriesArrayList.add(new BarEntry(20, 11));
                muscleEntriesArrayList.add(new BarEntry(30, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 31;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Forearm Flexors and Grip Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (forearmFlexorsGripExercisesLog.size() != 0)
                {
                    for (int o = 0; o < forearmFlexorsGripExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == forearmFlexorsGripExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == forearmFlexorsGripExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (forearmFlexorsGripExercisesLog.get(o).getDayOfMonth() != forearmFlexorsGripExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(forearmFlexorsGripExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(17f, 1));
                muscleEntriesArrayList.add(new BarEntry(5f, 1f));
                muscleEntriesArrayList.add(new BarEntry(6, 12));
                muscleEntriesArrayList.add(new BarEntry(7, 7));
                muscleEntriesArrayList.add(new BarEntry(24, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 3));
                muscleEntriesArrayList.add(new BarEntry(19, 17));
                muscleEntriesArrayList.add(new BarEntry(30, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 28;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Forearm Extensor Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (forearmExtensorExercisesLog.size() != 0)
                {
                    for (int o = 0; o < forearmExtensorExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == forearmExtensorExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == forearmExtensorExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (forearmExtensorExercisesLog.get(o).getDayOfMonth() != forearmExtensorExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(forearmExtensorExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(13f, 12));
                muscleEntriesArrayList.add(new BarEntry(1f, 1f));
                muscleEntriesArrayList.add(new BarEntry(8, 12));
                muscleEntriesArrayList.add(new BarEntry(7, 7));
                muscleEntriesArrayList.add(new BarEntry(22, 3));
                muscleEntriesArrayList.add(new BarEntry(25, 3));
                muscleEntriesArrayList.add(new BarEntry(18, 19));
                muscleEntriesArrayList.add(new BarEntry(30, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 18;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Cardio Exercises"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (cardioExercisesLog.size() != 0)
                {
                    for (int o = 0; o < cardioExercisesLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == cardioExercisesLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == cardioExercisesLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (cardioExercisesLog.get(o).getDayOfMonth() != cardioExercisesLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(cardioExercisesLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(7f, 11));
                muscleEntriesArrayList.add(new BarEntry(4f, 3f));
                muscleEntriesArrayList.add(new BarEntry(10, 12));
                muscleEntriesArrayList.add(new BarEntry(11, 14));
                muscleEntriesArrayList.add(new BarEntry(24, 2));
                muscleEntriesArrayList.add(new BarEntry(25, 3));
                muscleEntriesArrayList.add(new BarEntry(21, 7));
                muscleEntriesArrayList.add(new BarEntry(30, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 24;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("Body Weight"))
            {
                int muscleGroupTally = 0;
                int dailyTally = 0;
                if (bodyWeightLog.size() != 0)
                {
                    for (int o = 0; o < bodyWeightLog.size(); o++)
                    {
                        //If current year and month matches year and month of log. Reflecting data on monthly basis.
                        if(LocalDateTime.now().getYear() == bodyWeightLog.get(o).getYear() && LocalDateTime.now().getMonthValue() == bodyWeightLog.get(o).getMonthValue())
                        {
                            //Increment total for month.
                            muscleGroupTally++;

                            //If this is not the first or only entry.
                            if(o != 0)
                            {
                                //If the current entry's day has gone over to the next day, array should be in chronological order
                                //daily tally is reset.
                                if (bodyWeightLog.get(o).getDayOfMonth() != bodyWeightLog.get(o - 1).getDayOfMonth())
                                {
                                    dailyTally = 0;
                                }
                            }

                            //If dates are the same, tally is incremented by 1.
                            dailyTally++;
                            muscleEntriesArrayList = new ArrayList<>();
                            muscleEntriesArrayList.add(new BarEntry(bodyWeightLog.get(o).getDayOfMonth(), dailyTally));
                            //For each log, create a new entry with x position at the int of day of month and y at dailyTally value.
                        }
                    }
                }
                muscleEntriesArrayList = new ArrayList<>();
                muscleEntriesArrayList.add(new BarEntry(1f, 1));
                muscleEntriesArrayList.add(new BarEntry(22f, 13f));
                muscleEntriesArrayList.add(new BarEntry(4, 2));
                muscleEntriesArrayList.add(new BarEntry(11, 14));
                muscleEntriesArrayList.add(new BarEntry(14, 5));
                muscleEntriesArrayList.add(new BarEntry(15, 3));
                muscleEntriesArrayList.add(new BarEntry(21, 7));
                muscleEntriesArrayList.add(new BarEntry(30, 12));
                //Force Feeding Data for showcasing purposes, as muscle group array is not synced to FireBase,
                //Thus, does not contain enough data for showcasing.

                //Sorting Entries as chart is created in order data is passed. Just in case data is out of order.
                Collections.sort(muscleEntriesArrayList, new EntryXComparator());
                muscleBarDataSet = new BarDataSet(muscleEntriesArrayList, "");
                muscleBarData = new BarData(muscleBarDataSet);
                muscleBarData.setDrawValues(false);
                muscleGroupChart.setData(muscleBarData);
                muscleBarDataSet.setColors(Color.rgb(120, 81, 169));
                muscleBarDataSet.setValueTextColor(Color.WHITE);
                muscleBarDataSet.setValueTextSize(10f);
                muscleBarData.setBarWidth(0.9f);
                //Setting Y axis color to white.
                muscleGroupChart.getAxisLeft().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisLeft().setTextSize(12);
                muscleGroupChart.getAxisLeft().setGranularityEnabled(true);
                muscleGroupChart.getAxisLeft().setGranularity(1f);
                muscleGroupChart.getAxisRight().setTextSize(12);
                muscleGroupChart.getAxisRight().setTextColor(Color.WHITE);
                muscleGroupChart.getAxisRight().setGranularityEnabled(true);
                muscleGroupChart.getAxisRight().setGranularity(1f);


                // Setup X Axis
                XAxis dayofMonth = muscleGroupChart.getXAxis();
                dayofMonth.setPosition(XAxis.XAxisPosition.BOTTOM);
                dayofMonth.setGranularityEnabled(false);
                dayofMonth.setGranularity(1.0f);
                dayofMonth.setXOffset(1f);
                dayofMonth.setAxisMinimum(1);
                dayofMonth.setAxisMaximum(31);
                dayofMonth.setTextColor(Color.WHITE);
                dayofMonth.setTextSize(12);
                muscleGroupChart.setDrawValueAboveBar(true);
                muscleGroupChart.getDescription().setEnabled(false);
                muscleGroupChart.getLegend().setEnabled(false);
                muscleGroupChart.setFitBars(true);
                muscleGroupChart.animateXY(500,500);
                muscleGroupChart.invalidate();

                TextView avgWorked = (TextView) findViewById(R.id.avgDays);
                muscleGroupTally = 64;

                //Getting average of workouts per day by dividing total workouts for the month by month length, assuming not a leap year.
                int daysOfMonth = LocalDateTime.now().getMonth().length(false);
                float averageDays = (float)muscleGroupTally/(float)daysOfMonth;
                BigDecimal avgD = new BigDecimal(averageDays);
                avgD = avgD.setScale(2, RoundingMode.HALF_UP);
                averageDays = avgD.floatValue();
                avgWorked.setText(Float.toString(averageDays));
                avgWorked.invalidate();
            }
        }
    }

    void GenerateExerciseSpinnerWorkouts()
    {
        //Set muscle group spinner
        spinner =  findViewById(R.id.exerciseSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, workouts);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    //Function for generating Breakdown chart.
    void GenerateBreakdownSpinner()
    {
        //Set breakdown spinner
        spinner =  findViewById(R.id.monthSelect);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, monthSelect);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    void GenerateBreakdown()
    {
        //month will be used to tell what month in date logs to look for without if checking every loop
        int month = 0;
        int chest = 0, shoulder = 0, bicep = 0, tricep = 0, leg = 0, back = 0, glute = 0, ab = 0, calves = 0,
            flexors = 0, extensors = 0, cardio = 0, body = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String currentSel = spinner.getSelectedItem().toString();
            if (currentSel.equals("January"))
            {
                month = 1;
                //Hard Coding data for demonstrative purposes
                chest = 21;
                shoulder =12;
                bicep = 13;
                tricep = 32;
                leg = 7;
                back = 6;
                glute = 14;
                ab = 22;
                calves = 17;
                flexors = 6;
                extensors = 9;
                cardio = 8;
                body = 33;
            }

            if (currentSel.equals("February"))
            {
                month = 2;
                //Hard Coding data for demonstrative purposes
                chest = 12;
                shoulder =33;
                bicep = 24;
                tricep = 55;
                leg = 44;
                back = 24;
                glute = 4;
                ab = 66;
                calves = 7;
                flexors = 12;
                extensors = 4;
                cardio = 32;
                body = 32;
            }

            if (currentSel.equals("March"))
            {
                month = 3;
                //Hard Coding data for demonstrative purposes
                chest = 71;
                shoulder =21;
                bicep = 13;
                tricep = 12;
                leg = 27;
                back = 32;
                glute = 4;
                ab = 24;
                calves = 27;
                flexors = 16;
                extensors = 10;
                cardio = 4;
                body = 23;
            }

            if (currentSel.equals("April"))
            {
                month = 4;
                //Hard Coding data for demonstrative purposes
                chest = 65;
                shoulder =2;
                bicep = 32;
                tricep = 22;
                leg = 21;
                back = 23;
                glute = 4;
                ab = 52;
                calves = 7;
                flexors = 46;
                extensors = 5;
                cardio = 0;
                body = 23;
            }

            if (currentSel.equals("May"))
            {
                month = 5;
                //Hard Coding data for demonstrative purposes
                chest = 43;
                shoulder =22;
                bicep = 44;
                tricep = 22;
                leg = 17;
                back = 16;
                glute = 4;
                ab = 32;
                calves = 27;
                flexors = 16;
                extensors = 19;
                cardio = 18;
                body = 13;
            }

            if (currentSel.equals("June"))
            {
                month = 6;
                //Hard Coding data for demonstrative purposes
                chest = 21;
                shoulder =2;
                bicep = 3;
                tricep = 2;
                leg = 7;
                back = 6;
                glute = 4;
                ab = 2;
                calves = 7;
                flexors = 16;
                extensors = 9;
                cardio = 8;
                body = 3;
            }

            if (currentSel.equals("July"))
            {
                month = 7;
                //Hard Coding data for demonstrative purposes
                chest = 30;
                shoulder =20;
                bicep = 10;
                tricep = 30;
                leg = 40;
                back = 20;
                glute = 10;
                ab = 20;
                calves = 20;
                flexors = 30;
                extensors = 10;
                cardio = 20;
                body = 30;
            }

            if (currentSel.equals("August"))
            {
                month = 8;
                //Hard Coding data for demonstrative purposes
                chest = 2;
                shoulder =1;
                bicep = 4;
                tricep = 3;
                leg = 123;
                back = 6;
                glute = 1;
                ab = 2;
                calves = 1;
                flexors = 6;
                extensors = 9;
                cardio = 8;
                body = 3;
            }

            if (currentSel.equals("September"))
            {
                month = 9;
                //Hard Coding data for demonstrative purposes
                chest = 200;
                shoulder =0;
                bicep = 0;
                tricep = 3;
                leg = 0;
                back = 4;
                glute = 12;
                ab = 2;
                calves = 1;
                flexors = 6;
                extensors = 9;
                cardio = 8;
                body = 3;
            }

            if (currentSel.equals("October"))
            {
                month = 10;
                //Hard Coding data for demonstrative purposes
                chest = 3;
                shoulder =4;
                bicep = 124;
                tricep = 2;
                leg = 72;
                back = 6;
                glute = 4;
                ab = 42;
                calves = 7;
                flexors = 4;
                extensors = 2;
                cardio = 3;
                body = 3;
            }

            if (currentSel.equals("November"))
            {
                month = 11;
                //Hard Coding data for demonstrative purposes
                chest = 2;
                shoulder =3;
                bicep = 3;
                tricep = 6;
                leg = 7;
                back = 6;
                glute = 4;
                ab = 164;
                calves = 8;
                flexors = 2;
                extensors = 1;
                cardio = 8;
                body = 3;
            }

            if (currentSel.equals("December"))
            {
                month = 12;
                //Hard Coding data for demonstrative purposes
                chest = 1;
                shoulder =15;
                bicep = 3;
                tricep = 32;
                leg = 5;
                back = 21;
                glute = 4;
                ab = 52;
                calves = 37;
                flexors = 2;
                extensors = 3;
                cardio = 4;
                body = 143;
            }

            //Time to loop through all the log arrays and get totals for each month.
            if(chestExercisesLog.size() != 0)
            {
                for (int i = 0; i < chestExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == chestExercisesLog.get(i).getYear() && chestExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        chest++;
                    }
                }
            }

            if(shoulderExercisesLog.size() != 0)
            {
                for (int i = 0; i < shoulderExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == shoulderExercisesLog.get(i).getYear() && shoulderExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        shoulder++;
                    }
                }
            }

            if(bicepExercisesLog.size() != 0)
            {
                for (int i = 0; i < bicepExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == bicepExercisesLog.get(i).getYear() && bicepExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        bicep++;
                    }
                }
            }

            if(tricepsExercisesLog.size() != 0)
            {
                for (int i = 0; i < tricepsExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == tricepsExercisesLog.get(i).getYear() && tricepsExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        tricep++;
                    }
                }
            }

            if(legExercisesLog.size() != 0)
            {
                for (int i = 0; i < legExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == legExercisesLog.get(i).getYear() && legExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        leg++;
                    }
                }
            }

            if(backExercisesLog.size() != 0)
            {
                for (int i = 0; i < backExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == backExercisesLog.get(i).getYear() && backExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        back++;
                    }
                }
            }

            if(gluteExercisesLog.size() != 0)
            {
                for (int i = 0; i < gluteExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == gluteExercisesLog.get(i).getYear() && gluteExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        glute++;
                    }
                }
            }

            if(abExercisesLog.size() != 0)
            {
                for (int i = 0; i < abExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == abExercisesLog.get(i).getYear() && abExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        ab++;
                    }
                }
            }

            if(calvesExercisesLog.size() != 0)
            {
                for (int i = 0; i < calvesExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == calvesExercisesLog.get(i).getYear() && calvesExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        calves++;
                    }
                }
            }

            if(forearmFlexorsGripExercisesLog.size() != 0)
            {
                for (int i = 0; i < forearmFlexorsGripExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == forearmFlexorsGripExercisesLog.get(i).getYear() && forearmFlexorsGripExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        flexors++;
                    }
                }
            }

            if(forearmExtensorExercisesLog.size() != 0)
            {
                for (int i = 0; i < forearmExtensorExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == forearmExtensorExercisesLog.get(i).getYear() && forearmExtensorExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        extensors++;
                    }
                }
            }

            if(cardioExercisesLog.size() != 0)
            {
                for (int i = 0; i < cardioExercisesLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == cardioExercisesLog.get(i).getYear() && cardioExercisesLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        cardio++;
                    }
                }
            }

            if(bodyWeightLog.size() != 0)
            {
                for (int i = 0; i < bodyWeightLog.size(); i++)
                {
                    if(LocalDateTime.now().getYear() == bodyWeightLog.get(i).getYear() && bodyWeightLog.get(i).getMonthValue() == month)
                    {
                        //If the current entry matches the current year and month, add to month tally.
                        body++;
                    }
                }
            }


            //Tallying the total for all exercises done for the current month.
            int monthTotal = chest+shoulder+bicep+tricep+leg+back+glute+ab+calves+flexors+extensors+cardio+body;

            TextView totalWorkOuts = (TextView) findViewById(R.id.totalWorkoutVal);
            totalWorkOuts.setText(Integer.toString(monthTotal));
            totalWorkOuts.invalidate();

            //Setting up Pie Chart Entries
            breakdownEntries = new ArrayList<>();
            breakdownEntries.add(new PieEntry(chest, "Chest"));
            breakdownEntries.add(new PieEntry(shoulder, "Shoulder"));
            breakdownEntries.add(new PieEntry(bicep, "Bicep"));
            breakdownEntries.add(new PieEntry(tricep, "Tricep"));
            breakdownEntries.add(new PieEntry(leg, "Leg"));
            breakdownEntries.add(new PieEntry(back, "Back"));
            breakdownEntries.add(new PieEntry(glute, "Glute"));
            breakdownEntries.add(new PieEntry(ab, "Ab"));
            breakdownEntries.add(new PieEntry(calves, "Calves"));
            breakdownEntries.add(new PieEntry(flexors, "Forearm Flexors and Grip"));
            breakdownEntries.add(new PieEntry(extensors, "Forearm Extensor"));
            breakdownEntries.add(new PieEntry(cardio, "Cardio"));
            breakdownEntries.add(new PieEntry(body, "Body Weight"));

            //Setting up dataset
            breakdownDataSet = new PieDataSet(breakdownEntries, "Monthly Breakdown");
            breakdownDataSet.setColors(ColorTemplate.createColors(colors));
            breakdownDataSet.setValueTextSize(14f);

            //Setting up Data
            breakdownData = new PieData(breakdownDataSet);

            //Setting up chart
            breakdownChart.setData(breakdownData);
            breakdownChart.animateXY(500,500);
            breakdownChart.setDrawEntryLabels(true);
            breakdownChart.setTransparentCircleColor(Color.rgb(120,81,169));
            breakdownChart.setEntryLabelColor(Color.WHITE);
            breakdownChart.getLegend().setTextColor(Color.WHITE);
            breakdownChart.setHoleColor(Color.TRANSPARENT);
            breakdownChart.getDescription().setEnabled(false);
            breakdownChart.invalidate();
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {}

}
