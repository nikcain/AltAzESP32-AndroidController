package net.nikcain.altazgoto;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import net.nikcain.altazgoto.databinding.ControlsFragmentBinding;

import java.util.Calendar;
import java.util.Date;

public class ControlsFragment extends Fragment {
    public static final String LOG_TAG = "ControlsFragment";

    private ControlsFragmentBinding binding;

    private TelescopeTCPClient tcpclient;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = ControlsFragmentBinding.inflate(inflater, container, false);

        binding.setAppviewmodel(((MainActivity)getActivity()).model);
        tcpclient = new TelescopeTCPClient(((MainActivity)getActivity()).model);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppViewModel avm = ((MainActivity)getActivity()).model;

        binding.selecttargetbtn.setOnClickListener(v ->
                NavHostFragment.findNavController(ControlsFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        binding.alignbtn.setOnClickListener(v ->
                NavHostFragment.findNavController(ControlsFragment.this)
                        .navigate(R.id.action_controls_to_alignment)
        );

        binding.gototargetbtn.setOnClickListener(v->tcpclient.SendTarget(avm.getSelectedTarget().getValue()));
        binding.gotoAltAzbtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View vw = getView();
                EditText alt = vw.findViewById(R.id.alt_value);
                EditText az = vw.findViewById(R.id.az_value);
                goToAltAz(Double.parseDouble(alt.getText().toString()), Double.parseDouble(az.getText().toString()));
            }
        });

        binding.trackingonoff.setOnClickListener(v->tcpclient.SetTracking(((Switch)v).isChecked()));
        binding.upbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.up));
        binding.downbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.down));
        binding.leftbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.left));
        binding.rightbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.right));
        binding.stopbtn.setOnClickListener(v -> tcpclient.Stop());
        binding.resetbtn.setOnClickListener(v -> tcpclient.Reset());


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                View vw = getView();
                ImageView img;
                if (vw != null) {
                    img = (ImageView) vw.findViewById(R.id.connectedIndicator);
                    if (tcpclient.isConnected) {
                        Log.i(LOG_TAG, "connection good");
                        img.setImageResource(android.R.drawable.checkbox_on_background);
                    } else {
                        Log.i(LOG_TAG, "connection bad");
                        img.setImageResource(android.R.drawable.checkbox_off_background);
                    }
                    TextView tv = (TextView)vw.findViewById(R.id.LogText);
                    tv.setText(avm.getDebugText().getValue());
                }
                tcpclient.GetStatus();
                if (vw != null) {
                    img = (ImageView) vw.findViewById(R.id.trackingIndicator);
                    if (avm.getUiState().getValue().tracking) {
                        Log.i(LOG_TAG, "tracking on");
                        img.setImageResource(android.R.drawable.btn_star_big_on);
                    } else {
                        Log.i(LOG_TAG, "tracking off");
                        img.setImageResource(android.R.drawable.btn_star_big_off);
                    }
                }

                handler.postDelayed(this, 1000);
            }
        }, 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void goToAltAz(double alt, double az)
    {
        if (((MainActivity)getActivity()).AlignmentMgr.isCalibrated())
        {
            // calibration exists. Transform alt az before sending
            ((MainActivity)getActivity()).AlignmentMgr.transform(alt, az);
        }
        tcpclient.SendAltAzTarget(alt, az);
    }
}