package net.nikcain.altazgoto;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    public final MutableLiveData<String> testName = new MutableLiveData("hello");
    private final MutableLiveData<AppDataModel> uiState =
            new MutableLiveData(new AppDataModel(   false,
                                                    false,
                                                    0,0,
                                                    0,0,
                                                    0,0));
    public LiveData<AppDataModel> getUiState() {
        return uiState;
    }
    public MutableLiveData<String> getTestName() {
        return testName;
    }

    public MutableLiveData<targets> getSelectedTarget() {
        targets t = new targets();
        AppDataModel a = uiState.getValue();
        t.ra = a.selectedTargetRA;
        t.dec = a.selectedTargetDEC;
        return new MutableLiveData(t);
    }
}
