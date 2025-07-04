package net.nikcain.altazgoto;

public class AppDataModel {
    public boolean tracking;
    public boolean calibrating;
    public double currentRA;
    public double currentDEC;
    public targets selectedTarget;
    public targets currentTarget;

    public AppDataModel(boolean tracking,
                        boolean calibrating,
                        targets selectedTarget,
                        targets currentTarget,
                        double currentRA,
                        double currentDEC) {
        this.tracking = tracking;
        this.calibrating = calibrating;
        this.currentRA = currentRA;
        this.currentDEC = currentDEC;
        this.selectedTarget = selectedTarget;
        this.currentTarget = currentTarget;
    }
}
