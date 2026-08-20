package net.nikcain.altazgoto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppViewModel extends ViewModel {

    private final MutableLiveData<AppDataModel> uiState =
            new MutableLiveData(new AppDataModel(   false,
                                                    new targets(),
                                                    new targets(),
                                            0,
                                            0,
                                                    new String(""),
                                                    new ArrayList<CalibratedStar>(),
                                                    new boolean[3]
                                                    ));
    public LiveData<AppDataModel> getUiState() {
        return uiState;
    }

    public MutableLiveData<targets> getSelectedTarget() {
        AppDataModel a = uiState.getValue();
        return new MutableLiveData(a.selectedTarget);
    }
    public void setSelectedTarget(targets t) {
        AppDataModel a = uiState.getValue();
        a.selectedTarget = t;
    }
    public void setDebugText(String txt) {
        AppDataModel a = uiState.getValue();
        a.debugText = txt;
    }

    public MutableLiveData<String> getTargetText(targets target) {
        String ret = target.name + " RA:" + target.ra + " dec:" + target.dec;
        return new MutableLiveData<>(ret);
    }
    public MutableLiveData<String> getSelectedTargetText() {
        return getTargetText(uiState.getValue().selectedTarget);
    }
    public MutableLiveData<String> getCurrentTargetText() {
        String ret = "alt: " + uiState.getValue().currentAlt + " az: " + uiState.getValue().currentAz;
        return getTargetText(uiState.getValue().currentTarget);
    }

    public MutableLiveData<String> getDebugText() {
        return new MutableLiveData<>(uiState.getValue().debugText);
    }
}
