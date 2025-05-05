package net.nikcain.altazgoto;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {targets.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TargetsDAO targetsDao();
}