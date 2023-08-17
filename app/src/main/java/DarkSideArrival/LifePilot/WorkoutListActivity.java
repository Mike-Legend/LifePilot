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
            "Side Lunges (Bodyweight)",
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


        workoutList.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}