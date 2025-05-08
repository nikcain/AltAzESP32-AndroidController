package net.nikcain.altazgoto;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import net.nikcain.altazgoto.databinding.FragmentControlsBinding;

public class ControlsFragment extends Fragment {

    AppViewModel model;
    private FragmentControlsBinding binding;

    private TelescopeTCPClient tcpclient = new TelescopeTCPClient();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        model = new ViewModelProvider(this).get(AppViewModel.class);

        binding = FragmentControlsBinding.inflate(inflater, container, false);
        binding.selectedtargetinfo.setText(tcpclient.status);

        binding.setAppviewmodel(model);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.selecttargetbtn.setOnClickListener(v ->
                NavHostFragment.findNavController(ControlsFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment)
        );

        binding.gototargetbtn.setOnClickListener(v->tcpclient.SendTarget(model.getSelectedTarget().getValue()));
        binding.calibrateonoff.setOnClickListener(v->tcpclient.SetCalibration(((Switch)v).isChecked()));
        binding.trackingonoff.setOnClickListener(v->tcpclient.SetTracking(((Switch)v).isChecked()));
        binding.upbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.up));
        binding.downbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.down));
        binding.leftbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.left));
        binding.rightbtn.setOnClickListener(v -> tcpclient.Move(TelescopeTCPClient.direction.right));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}