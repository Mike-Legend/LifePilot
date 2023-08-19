package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.animation.ObjectAnimator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.view.View;
import android.transition.Visibility;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.protobuf.NullValue;

import org.checkerframework.checker.units.qual.A;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<Button> userRoutines;
    private ArrayList<ArrayList<Button>> userExercisesArrayList;
    public int routineIDActive;

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set User session data
        userRoutines = new ArrayList<>();
        userExercisesArrayList = new ArrayList<ArrayList<Button>>();
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
            if(userRoutines.size() != 0) {
                LoadUserRoutines();
            }
        } else if (id == R.id.routinesButton) {
            setContentView(R.layout.routine_list);
            if(userRoutines.size() != 0) {
                LoadUserRoutines();
            }
        } else if (id == R.id.analytics_button) {
            setContentView(R.layout.data_screen);
        } else if (id == R.id.Home_Button) {
            setContentView(R.layout.activity_main);
        } else if (id == R.id.NewRoutineCreate_Button) {
            FrameLayout routinelistoverlay = findViewById(R.id.routinelistoverlay);
            routinelistoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.NewExerciseAdd_Button) {
            FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.VISIBLE);
        } else if (id == R.id.CancelNewRoutine_Button) {
            FrameLayout routinelistoverlay = findViewById(R.id.routinelistoverlay);
            routinelistoverlay.setVisibility(View.GONE);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
        } else if (id == R.id.CancelExercises_Button) {
            FrameLayout routineoverlay = findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.GONE);
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
                gradDraw.setColor(getResources().getColor(R.color.purple));
                btn.setBackground(gradDraw);
                btn.setPadding(0,0,0,8);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
                params.setMargins(0, 30, 0, 0);
                btn.setLayoutParams(params);
                ll.addView(btn);
                userExercisesArrayList.get(routineIDActive).add(btn);
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
            gradDraw.setColor(getResources().getColor(R.color.purple));
            btn.setBackground(gradDraw);
            btn.setPadding(0,0,0,8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 120);
            params.setMargins(0, 30, 0, 0);
            btn.setLayoutParams(params);
            ll.addView(btn);
            //if keyboard doesn't go away
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getApplicationWindowToken(),0);
            //added to routine list
            btn.setId(userRoutines.size());
            userRoutines.add(btn);
            //New Exercise Array
            userExercisesArrayList.add(new ArrayList<Button>());
        } else if (id < userRoutines.size() || id == userRoutines.size()) {
            setContentView(R.layout.routine_newlist);
            //Generate Name and routine separation
            routineIDActive = id;
            TextView titletext = findViewById(R.id.NewRoutineSet_TopText);
            for(int i = 0; i < userRoutines.size(); i++) {
                if(userRoutines.get(i).getId() == id) {
                    titletext.setText(userRoutines.get(i).getText());
                }
            }
            //Generate Routine Exercises
            if(userExercisesArrayList.get(routineIDActive).size() != 0) {
                LoadUserRoutineExercises();
            }
        } else if (id == R.id.ExerciseSave_Button){
            //TODO: Sync to firebase
            setContentView(R.layout.routine_list);
            LoadUserRoutines();
        }else {
            setContentView(R.layout.activity_main);
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