package DarkSideArrival.LifePilot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WorkoutRecycler extends RecyclerView.Adapter<WorkoutRecycler.ViewHolder>
{
    public String[] myWorkouts;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView textView;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            textView = itemView.findViewById(R.id.workoutList);
        }
    }

    public WorkoutRecycler(String[] myWorkouts)
    {
        this.myWorkouts = myWorkouts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.workoutsrowitem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position)
    {
        viewHolder.textView.setText(myWorkouts[position]);
    }

    @Override
    public int getItemCount()
    {
        return myWorkouts.length;
    }

}
