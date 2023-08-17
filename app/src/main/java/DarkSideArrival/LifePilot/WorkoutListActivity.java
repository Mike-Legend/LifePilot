package DarkSideArrival.LifePilot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class WorkoutListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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
        "Cardio Exercises"};

    private String[] chestExercises = new String[]{"chest press", "flys", "something"};
    private String[] shoulderExercises = new String[]{"chest press", "flys", "something", "bull"};
    Spinner spinner;

    RecyclerView WorkoutRecyclerView;

    WorkoutRecycler workoutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_list);

        spinner =  findViewById(R.id.exerciseSpin);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, workouts);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        WorkoutRecyclerView = findViewById(R.id.workList);
        WorkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        workoutList = new WorkoutRecycler(chestExercises);
        WorkoutRecyclerView.setAdapter(workoutList);
        workoutList.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String currentSel = spinner.getSelectedItem().toString();
        if(currentSel.equals("Shoulder Exercises"))
        {
            workoutList.myWorkouts = shoulderExercises;
        }
        workoutList.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}