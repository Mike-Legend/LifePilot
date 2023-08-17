package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.view.View;
import android.transition.Visibility;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override //Initial App Generation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        } else if (id == R.id.AddExercises_Button) {
            FrameLayout routineoverlay = (FrameLayout) findViewById(R.id.routineoverlay);
            routineoverlay.setVisibility(View.GONE);

            LinearLayout ll = (LinearLayout)findViewById(R.id.ButtonAddsHere);
            Button btn = new Button(this);
            btn.setText("Manual Add");
            btn.setTextSize(24);
            btn.setTextColor(Color.WHITE);
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
        } else {
            setContentView(R.layout.activity_main);
        }
    }
}