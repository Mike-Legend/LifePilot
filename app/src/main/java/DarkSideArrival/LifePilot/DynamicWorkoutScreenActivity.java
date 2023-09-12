package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DynamicWorkoutScreenActivity extends AppCompatActivity {

    private TextView workoutTitle;
    private TextView workoutDesc;
    private ImageView workoutImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_workout_screen);

        String exercise = getIntent().getStringExtra("name");
        workoutTitle = findViewById(R.id.workoutTitle);
        workoutTitle.setText(exercise);

        workoutDesc = findViewById(R.id.workoutDesc);

        workoutImage = findViewById(R.id.workoutPic);

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
}