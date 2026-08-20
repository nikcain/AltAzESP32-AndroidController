package net.nikcain.altazgoto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class calibrationstars {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "Constellation")
    public String constellation;

    @ColumnInfo(name = "starname")
    public String starname;

    @ColumnInfo(name = "Magnitude")
    public double mag;

    @ColumnInfo(name = "RA")
    public double ra;

    @ColumnInfo(name = "DEC")
    public double dec;

}
