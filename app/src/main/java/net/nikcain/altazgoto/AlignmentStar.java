package net.nikcain.altazgoto;

public class AlignmentStar {
        double rawAlt, rawAz;   // Degrees
        double trueAlt, trueAz; // Degrees
        boolean set = false;

        calibrationstars baseStar;

    public AlignmentStar(double rawAlt, double rawAz, double trueAlt, double trueAz) {
        this.rawAlt = rawAlt;   this.rawAz = rawAz;
        this.trueAlt = trueAlt; this.trueAz = trueAz;
    }
}

