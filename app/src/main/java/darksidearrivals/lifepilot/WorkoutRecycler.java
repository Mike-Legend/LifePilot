package darksidearrivals.lifepilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WorkoutRecycler extends RecyclerView.Adapter<WorkoutRecycler.ViewHolder>
{
    public String[] myWorkouts;
    public ArrayList<String> string;

    interface OnItemCheckListener {
        void onItemCheck(String string);
        void onItemUncheck(String string);
        void onButtonClick(String string);
    }

    @NonNull
    private OnItemCheckListener onItemClick;
    public WorkoutRecycler(String[] myWorkouts, ArrayList<String> string, @NonNull OnItemCheckListener onItemCheckListener)
    {
        this.string = string;
        this.onItemClick = onItemCheckListener;
        this.myWorkouts = myWorkouts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public Button button;
        public CheckBox checkbox;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            textView = itemView.findViewById(R.id.workoutList);
            textView.setVisibility(View.GONE);
            checkbox = itemView.findViewById(R.id.recyclertestcheckbox);
            checkbox.setClickable(false);
            button = itemView.findViewById(R.id.recyclerworkoutbuttonadd);

            itemView.findViewById(R.id.recyclerworkoutbuttonadd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //goes to new workout screen here
                    //recycleid = v.getId();
                }
            });
        }
        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.workoutsrowitem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position)
    {
        viewHolder.button.setText(myWorkouts[position]);
        if(string.contains(myWorkouts[position])) {
            viewHolder.checkbox.setChecked(true);
        } else {
            viewHolder.checkbox.setChecked(false);
        }

        if (viewHolder instanceof ViewHolder) {
            final String currentExercise = myWorkouts[position];
            (viewHolder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    (viewHolder).checkbox.setChecked(
                            !(viewHolder).checkbox.isChecked());
                    if ((viewHolder).checkbox.isChecked()) {
                        onItemClick.onItemCheck(currentExercise);
                    } else {
                        onItemClick.onItemUncheck(currentExercise);
                    }
                }
            });
            (viewHolder).button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onButtonClick(currentExercise);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return myWorkouts.length;
    }

}
