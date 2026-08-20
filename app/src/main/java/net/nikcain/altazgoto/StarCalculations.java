package net.nikcain.altazgoto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

// thanks to gist.github.com/matshofman/4145718 (nobody else got it right!!!)
public class StarCalculations {

    public double[] raDecToAltAz(double RA, double Dec, double Lat, double Long)
    {
        return Calculate(RA, Dec, Lat, Long, LocalDateTime.now(ZoneOffset.UTC));
    }

    public double [] Calculate(double RA, double Dec, double Lat, double Long, LocalDateTime Date)
    {
        // Day offset and Local Siderial Time
        double dayOffset = ChronoUnit.DAYS.between(LocalDateTime.of(2000, 1, 1, 12, 0, 0, 0),Date);
        double LST = (100.46 + 0.985647 * dayOffset + Long + 15 * (Date.getHour() + Date.getMinute() / 60d) + 360) % 360;

        // Hour Angle
        double HA = (LST - RA + 360) % 360;

        // HA, DEC, Lat to Alt, AZ
        double x = Math.cos(HA * (Math.PI / 180)) * Math.cos(Dec * (Math.PI / 180));
        double y = Math.sin(HA * (Math.PI / 180)) * Math.cos(Dec * (Math.PI / 180));
        double z = Math.sin(Dec * (Math.PI / 180));

        double xhor = x * Math.cos((90 - Lat) * (Math.PI / 180)) - z * Math.sin((90 - Lat) * (Math.PI / 180));
        double yhor = y;
        double zhor = x * Math.sin((90 - Lat) * (Math.PI / 180)) + z * Math.cos((90 - Lat) * (Math.PI / 180));

        double az = Math.atan2(yhor, xhor) * (180 / Math.PI) + 180;
        double alt = Math.asin(zhor) * (180 / Math.PI);

        return new double[]{alt,az};
    }
}
