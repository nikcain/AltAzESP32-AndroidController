package net.nikcain.altazgoto;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import net.nikcain.altazgoto.databinding.AlignmentFragmentBinding;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.List;

public class AlignmentFragment extends Fragment {

    private TelescopeTCPClient tcpclient;
    private AlignmentFragmentBinding binding;
    StarCalculations sc = new StarCalculations();

    private double m_lat = 52.6019682;
    private double m_long = -3.0955309;

    public AlignmentFragment() {
        // Required empty public constructor
    }

    public static AlignmentFragment newInstance() {
        AlignmentFragment fragment = new AlignmentFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tcpclient = new TelescopeTCPClient(((MainActivity)getActivity()).model);

        binding = AlignmentFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setAppviewmodel(((MainActivity) getActivity()).model);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        // Sync model with AlignmentMgr
        for (int i = 0; i < 3; i++) {
            if (((MainActivity) getActivity()).AlignmentMgr.isStarSet(i)) {
                ((MainActivity) getActivity()).model.setCalibrationPointSet(i, true);
            }
        }

        Button btn;
        CheckBox cbox;

        AlignmentStar star = ((MainActivity) getActivity()).model.getUiState().getValue().chosen.get(0);

        if (star != null) {
            btn = binding.star1button;
            cbox = binding.checkBoxStar1;

            double [] star_pos = sc.raDecToAltAz(star.baseStar.ra, star.baseStar.dec, m_lat, m_long);
            String txt = getString(R.string.alignment_btn_text, star.baseStar.constellation, star.baseStar.starname, star_pos[0], star_pos[1]);
            btn.setText(txt);
            AlignmentStar finalStar = star;
            btn.setOnClickListener(v->tcpclient.SendTarget(finalStar.baseStar));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(finalStar.baseStar,0));
        }
        star = ((MainActivity) getActivity()).model.getUiState().getValue().chosen.get(1);
        if (star != null)
        {
            btn = binding.star2button;
            cbox = binding.checkBoxStar2;
            double [] star_pos = sc.raDecToAltAz(star.baseStar.ra, star.baseStar.dec, m_lat, m_long);
            String txt = getString(R.string.alignment_btn_text, star.baseStar.constellation, star.baseStar.starname, star_pos[0], star_pos[1]);
            btn.setText(txt);
            AlignmentStar finalStar = star;
            btn.setOnClickListener(v->tcpclient.SendTarget(finalStar.baseStar));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(finalStar.baseStar,1));
        }
        star = ((MainActivity) getActivity()).model.getUiState().getValue().chosen.get(2);
        if (star != null)
        {
            btn = binding.star3button;
            cbox = binding.checkBoxStar3;
            double [] star_pos = sc.raDecToAltAz(star.baseStar.ra, star.baseStar.dec, m_lat, m_long);
            String txt = getString(R.string.alignment_btn_text, star.baseStar.constellation, star.baseStar.starname, star_pos[0], star_pos[1]);
            btn.setText(txt);
            AlignmentStar finalStar = star;
            btn.setOnClickListener(v->tcpclient.SendTarget(finalStar.baseStar));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(finalStar.baseStar,2));
        }

        binding.movementbtn.setOnClickListener(v ->
                NavHostFragment.findNavController(AlignmentFragment.this)
                        .navigate(R.id.action_alignmentFragment_to_movementFragment)
        );
    }

    public void ApplyCalibrationPoint(calibrationstars star, int starnum)
    {
        double [] pos = sc.raDecToAltAz(star.ra, star.dec, m_lat, m_long);
        ((MainActivity) getActivity()).AlignmentMgr.addAlignmentStar(star, starnum,
                ((MainActivity) getActivity()).model.getUiState().getValue().currentAlt,
                ((MainActivity) getActivity()).model.getUiState().getValue().currentAz,
                pos[0],
                pos[1]
        );

        ((MainActivity) getActivity()).model.setCalibrationPointSet(starnum, true);
    }
}