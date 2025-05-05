package net.nikcain.altazgoto;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class targets {
    @PrimaryKey
    public int id;

    @ColumnInfo(name = "ra")
    public double ra;

    @ColumnInfo(name = "dec")
    public double dec;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "searchText")
    public String searchText;

}
