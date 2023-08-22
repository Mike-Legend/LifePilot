package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
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
            setContentView(R.layout.experience_selection);
        } else {
            setContentView(R.layout.activity_main);
        }
    }
}