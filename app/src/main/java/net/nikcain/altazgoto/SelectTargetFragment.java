package net.nikcain.altazgoto;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import net.nikcain.altazgoto.databinding.FragmentSelecttargetBinding;

import java.util.Collections;
import java.util.List;

public class SelectTargetFragment extends Fragment {

    private FragmentSelecttargetBinding binding;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSelecttargetBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context ctx;
        ctx = getContext();

        assert ctx != null;

        appDatabase = Room.databaseBuilder(ctx, AppDatabase.class, "skyObjects")
                .createFromAsset("skyObjects2.db")
                .build();

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(SelectTargetFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );


        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        List<targets> items = Collections.emptyList();
        CustomAdapter customAdapter = new CustomAdapter(items);
        recyclerView.setAdapter(customAdapter);

        binding.btnSearchDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText text = (EditText)getView().findViewById(R.id.searchText);
                String searchTerm = text.getText().toString();
                ListenableFuture<List<targets>> future = appDatabase.targetsDao().findTarget(searchTerm);
                Futures.addCallback(future,
                        new FutureCallback<List<targets>>() {
                            public void onSuccess(List<targets> result) {
                                // handle success
                                customAdapter.setDataset(result);
                                customAdapter.notifyDataSetChanged();
                            }

                            public void onFailure(@NonNull Throwable thrown) {
                                // handle failure
                            }
                        },
                        ctx.getMainExecutor()
                );
            }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}