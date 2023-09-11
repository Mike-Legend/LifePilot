package DarkSideArrival.LifePilot;

import android.content.ClipData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WorkoutRecycler extends RecyclerView.Adapter<WorkoutRecycler.ViewHolder>
{
    public String[] myWorkouts;

    public WorkoutRecycler(String[] myWorkouts)
    {
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
            button = itemView.findViewById(R.id.recyclerworkoutbuttonadd);

            itemView.findViewById(R.id.recyclerworkoutbuttonadd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //goes to new workout screen here
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

    interface OnItemCheckListener {
        void onItemCheck(String string);
        void onItemUncheck(String string);
    }
    @NonNull
    private OnItemCheckListener onItemClick;

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position)
    {
        viewHolder.button.setText(myWorkouts[position]);
        if (viewHolder instanceof ViewHolder) {
            final String currentExercise = myWorkouts[position];

            ((ViewHolder) viewHolder).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewHolder) viewHolder).checkbox.setChecked(
                            !((ViewHolder) viewHolder).checkbox.isChecked());
                    if (((ViewHolder) viewHolder).checkbox.isChecked()) {
                        onItemClick.onItemCheck(currentExercise);
                    } else {
                        onItemClick.onItemUncheck(currentExercise);
                    }
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
