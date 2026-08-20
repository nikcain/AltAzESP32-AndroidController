package net.nikcain.altazgoto;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.time.LocalDate;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.List;

import net.nikcain.altazgoto.databinding.AlignmentFragmentBinding;
import net.nikcain.altazgoto.databinding.ControlsFragmentBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlignmentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlignmentFragment extends Fragment {

    private AppDatabase appDatabase;
    private AlignmentFragmentBinding binding;
    private TelescopeTCPClient tcpclient;
    StarCalculations sc = new StarCalculations();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private double m_lat = 52.6019682;
    private double m_long = -3.0955309;

    public AlignmentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AlignmentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlignmentFragment newInstance(String param1, String param2) {
        AlignmentFragment fragment = new AlignmentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = AlignmentFragmentBinding.inflate(inflater, container, false);
        //binding.setAppviewmodel(((MainActivity)getActivity()).model);
        tcpclient = new TelescopeTCPClient(((MainActivity)getActivity()).model);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.alignment_fragment, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx;
        ctx = getContext();

        assert ctx != null;

        appDatabase = Room.databaseBuilder(ctx, AppDatabase.class, "skyObjects")
                .createFromAsset("skyObjects5.db")
                .fallbackToDestructiveMigration(true)
                .build();

        ListenableFuture<List<calibrationstars>> future = appDatabase.targetsDao().getCalibrationStars();
        Futures.addCallback(future,
                new FutureCallback<List<calibrationstars>>() {
                    public void onSuccess(List<calibrationstars> result) {
                        // handle success
                        findThreeBrightStars(result, view);
                    }

                    public void onFailure(@NonNull Throwable thrown) {
                        // handle failure
                    }
                },
                ctx.getMainExecutor()
        );
    }

    public void ApplyCalibrationPoint(calibrationstars star)
    {
        double [] pos = sc.raDecToAltAz(star.ra, star.dec, m_lat, m_long);
        ((MainActivity) getActivity()).AlignmentMgr.addAlignmentStar(
                ((MainActivity) getActivity()).model.getUiState().getValue().currentAlt,
                ((MainActivity) getActivity()).model.getUiState().getValue().currentAz,
                pos[0],
                pos[1]
        );
    }

    public void findThreeBrightStars(List<calibrationstars> stars, View view) {

        List<calibrationstars> chosen = new ArrayList<>();

        for (calibrationstars star:stars) {

            LocalDate date = LocalDate.now();//  .of(2025, 4, 16);

            // Get Julian day number
            long jd = date.getLong(JulianFields.JULIAN_DAY);

            double [] pos = sc.raDecToAltAz(star.ra, star.dec, m_lat, m_long);

            if (pos[0] > 30 && pos[0] < 60)
            {
                boolean usethisone = true;
                for (calibrationstars cmp_star:chosen)
                {
                    double [] cmp_pos = sc.raDecToAltAz(cmp_star.ra, cmp_star.dec, m_lat, m_long);
                    double az_separation = Math.abs(pos[1] - cmp_pos[1]);
                    if (az_separation < 70) { usethisone = false; }
                }
                if (usethisone) {
                    // we'll use this one
                    chosen.add(star);
                    if (chosen.size() > 2) break;
                }
            }
        }
        Button btn;
        CheckBox cbox;


        if (!chosen.isEmpty()) {
            btn = view.findViewById(R.id.star1button);
            cbox = view.findViewById(R.id.checkBoxStar1);
            String txt = getString(R.string.alignment_btn_text, chosen.get(0).constellation, chosen.get(0).starname);
            btn.setText(txt);
            btn.setOnClickListener(v->tcpclient.SendTarget(chosen.get(0)));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(chosen.get(0)));
        }
        if (chosen.size() > 1)
        {
            btn = view.findViewById(R.id.star2button);
            cbox = view.findViewById(R.id.checkBoxStar2);
            String txt = getString(R.string.alignment_btn_text, chosen.get(1).constellation, chosen.get(1).starname);
            btn.setText(txt);
            btn.setOnClickListener(v->tcpclient.SendTarget(chosen.get(1)));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(chosen.get(1)));
        }
        if (chosen.size() > 2)
        {
            btn = view.findViewById(R.id.star3button);
            cbox = view.findViewById(R.id.checkBoxStar3);
            String txt = getString(R.string.alignment_btn_text, chosen.get(2).constellation, chosen.get(2).starname);
            btn.setText(txt);
            btn.setOnClickListener(v->tcpclient.SendTarget(chosen.get(2)));
            cbox.setOnClickListener(v->ApplyCalibrationPoint(chosen.get(2)));
        }
    }
}