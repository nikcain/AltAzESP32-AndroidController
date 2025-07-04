package net.nikcain.altazgoto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    public final MutableLiveData<String> testName = new MutableLiveData("hello");
    private final MutableLiveData<AppDataModel> uiState =
            new MutableLiveData(new AppDataModel(   false,
                                                    true,
                                                    new targets(),
                                                    new targets(),
                                                    0,0));
    public LiveData<AppDataModel> getUiState() {
        return uiState;
    }
    public MutableLiveData<String> getTestName() {
        return testName;
    }

    public MutableLiveData<targets> getSelectedTarget() {
        AppDataModel a = uiState.getValue();
        return new MutableLiveData(a.selectedTarget);
    }
    public void setSelectedTarget(targets t) {
        AppDataModel a = uiState.getValue();
        a.selectedTarget = t;
    }

    public MutableLiveData<String> getTargetText(targets target) {
        String ret = target.name + " RA:" + target.ra + " dec:" + target.dec;
        return new MutableLiveData<String>(ret);
    }
    public MutableLiveData<String> getSelectedTargetText() {
        return getTargetText(uiState.getValue().selectedTarget);
    }
    public MutableLiveData<String> getCurrentTargetText() {
        String ret = "alt: " + uiState.getValue().currentRA + " az: " + uiState.getValue().currentDEC;
        return getTargetText(uiState.getValue().currentTarget);
    }
}
