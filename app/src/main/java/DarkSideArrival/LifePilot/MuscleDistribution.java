package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Spinner;

public class MuscleDistribution extends AppCompatActivity implements View.OnClickListener
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Trying to set exit and enter transitions. No luck so far.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            Transition enterSlide = new Slide(Gravity.LEFT);
            Transition exitSlide = new Slide(Gravity.RIGHT);
            getWindow().setEnterTransition(enterSlide);
            getWindow().setExitTransition(exitSlide);
        }

        //Initiating Spinner for Month Selection
        Spinner spinner = (Spinner) findViewById(R.id.monthSelect);
        setContentView(R.layout.muscle_distribution);
    }

    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {
        int id = view.getId();

        if(id == R.id.muscleHomeButton)
        {
            Intent userInt = new Intent(getApplicationContext(), MainActivity.class);
            finish();
            startActivity(userInt);
        }

        else if (id == R.id.muscleBack)
        {
            Intent userInt = new Intent(getApplicationContext(), Analytics.class);
            finish();
            startActivity(userInt);
        }


    }
}
