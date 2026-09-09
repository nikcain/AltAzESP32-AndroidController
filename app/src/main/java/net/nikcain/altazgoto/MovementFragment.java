package net.nikcain.altazgoto;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Locale;

public class MovementFragment extends Fragment {

    private TextView coordsTextView;
    private boolean isEyepieceMode = false;
    private TelescopeTCPClient tcpclient;
    public MovementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tcpclient = new TelescopeTCPClient(((MainActivity)getActivity()).model);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.movement_fragment, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        coordsTextView = view.findViewById(R.id.coordsTextView);
        View touchArea = view.findViewById(R.id.touchArea);
        MaterialSwitch modeSwitch = view.findViewById(R.id.modeSwitch);
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEyepieceMode = isChecked;
            modeSwitch.setText(isChecked ? "Eyepiece" : "Body");
        });

        touchArea.setOnTouchListener((v, event) -> {
            float x = event.getX();
            float y = event.getY();
            
            captureTouchLocation(x, y, event.getAction());
            
            return true;
        });
    }

    private void captureTouchLocation(float x, float y, int action) {

        double movex = 0;
        double movey = 0;
        View touchArea = getView().findViewById(R.id.touchArea);
        double max_x = ((double) touchArea.getWidth()) / 2.0;
        double max_y = ((double) touchArea.getHeight()) / 2.0;
        String actionName;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                actionName = "DOWN";
                // sending a normalised value using the width, so all directions
                // have the same scale, but you could push the up and down harder

                // if eyepiece mode is on, then image is inverted, and so controls
                // need inverting too
                if (isEyepieceMode)
                {
                    movex = -1*(x-max_x)/max_x ;
                    movey = -1*(y-max_y)/max_y;
                }
                else {
                    movex = (x-max_x)/max_x;
                    movey = (y-max_y)/max_y;
                }
                tcpclient.Move(movex, movey);

                String coords = String.format(Locale.getDefault(), "Action: %s\nX: %.2f\nY: %.2f", actionName, movex, movey);
                coordsTextView.setText(coords);
                break;
            case MotionEvent.ACTION_MOVE:
                actionName = "MOVE";
                break;
            case MotionEvent.ACTION_UP:
                actionName = "UP";
                break;
            default:
                actionName = "OTHER";
                break;
        }
    }
}