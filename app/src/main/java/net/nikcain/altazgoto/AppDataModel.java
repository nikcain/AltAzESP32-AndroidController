package net.nikcain.altazgoto;

public class AppDataModel {
    public final boolean tracking;
    public final boolean calibrating;
    public final double currentTargetRA;
    public final double currentTargetDEC;
    public final double selectedTargetRA;
    public final double selectedTargetDEC;
    public final double currentRA;
    public final double currentDEC;

    public AppDataModel(boolean tracking,
                        boolean calibrating,
                        double currentTargetRA,
                        double currentTargetDEC,
                        double selectedTargetRA,
                        double selectedTargetDEC,
                        double currentRA,
                        double currentDEC) {
        this.tracking = tracking;
        this.calibrating = calibrating;
        this.currentTargetRA = currentTargetRA;
        this.currentTargetDEC = currentTargetDEC;
        this.selectedTargetRA = selectedTargetRA;
        this.selectedTargetDEC = selectedTargetDEC;
        this.currentRA = currentRA;
        this.currentDEC = currentDEC;
    }
}
