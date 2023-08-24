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
import android.widget.Button;

//Creating New Activity for data screens for code readability.
public class Analytics extends AppCompatActivity implements View.OnClickListener
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
        setContentView(R.layout.data_screen);
    }

    @Override //Used for on click section in layout button attribute to switch layouts.
    public void onClick(View view) //add button with an else-if statement
    {
        int id = view.getId();

        if(id == R.id.analyticsHomeButton)
        {
            Intent userInt = new Intent(getApplicationContext(), MainActivity.class);
            finish();
            startActivity(userInt);
        }

        else if (id == R.id.excerciseData)
        {
            Intent userInt = new Intent(getApplicationContext(), ExerciseData.class);
            finish();
            startActivity(userInt);
        }

        else if (id == R.id.breakDown)
        {
            Intent userInt = new Intent(getApplicationContext(), MuscleDistribution.class);
            finish();
            startActivity(userInt);
        }

    }
}
