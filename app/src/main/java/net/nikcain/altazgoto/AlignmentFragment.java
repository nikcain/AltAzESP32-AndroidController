package net.nikcain.altazgoto;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import net.nikcain.altazgoto.databinding.AlignmentFragmentBinding;

import java.util.Objects;

public class AlignmentFragment extends Fragment {

    private TelescopeTCPClient tcpclient;
    private AlignmentFragmentBinding binding;
    StarCalculations sc = new StarCalculations();

    private final double m_lat = 52.6019682;
    private final double m_long = -3.0955309;

    public AlignmentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        tcpclient = new TelescopeTCPClient(((MainActivity) requireActivity()).model);

        binding = AlignmentFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setAppviewmodel(((MainActivity) requireActivity()).model);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        // Sync model with AlignmentMgr
        for (int i = 0; i < 3; i++) {
            if (((MainActivity) requireActivity()).AlignmentMgr.isStarSet(i)) {
                ((MainActivity) requireActivity()).model.setCalibrationPointSet(i, true);
            }
        }

        Button btn;
        CheckBox cbox;

        AlignmentStar star = Objects.requireNonNull(((MainActivity) requireActivity()).model.getUiState().getValue()).chosen.get(0);

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
        star = Objects.requireNonNull(((MainActivity) requireActivity()).model.getUiState().getValue()).chosen.get(1);
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
        star = Objects.requireNonNull(((MainActivity) requireActivity()).model.getUiState().getValue()).chosen.get(2);
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
        ((MainActivity) requireActivity()).AlignmentMgr.addAlignmentStar(star, starnum,
                Objects.requireNonNull(((MainActivity) requireActivity()).model.getUiState().getValue()).currentAlt,
                Objects.requireNonNull(((MainActivity) requireActivity()).model.getUiState().getValue()).currentAz,
                pos[0],
                pos[1]
        );

        ((MainActivity) requireActivity()).model.setCalibrationPointSet(starnum, true);
    }
}