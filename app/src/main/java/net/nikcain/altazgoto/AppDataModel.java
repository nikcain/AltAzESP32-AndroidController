package net.nikcain.altazgoto;

import java.util.List;

public class AppDataModel {
    public boolean tracking;
    public double currentAlt;
    public double currentAz;
    public targets selectedTarget;
    public targets currentTarget;
    public String debugText;
    public List<CalibratedStar> calibrationPoints;
    public boolean [] calibrationPointsSet;

    public AppDataModel(boolean tracking,
                        targets selectedTarget,
                        targets currentTarget,
                        double currentAlt,
                        double currentAz,
                        String dbgTxt,
                        List<CalibratedStar> calibrationPoints,
                        boolean[] calibrationPointsSet) {
        this.tracking = tracking;
        this.currentAlt = currentAlt;
        this.currentAz = currentAz;
        this.selectedTarget = selectedTarget;
        this.currentTarget = currentTarget;
        this.debugText = dbgTxt;
        this.calibrationPoints = calibrationPoints;
        this.calibrationPointsSet = calibrationPointsSet;
    }
}
