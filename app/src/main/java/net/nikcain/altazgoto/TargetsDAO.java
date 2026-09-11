package net.nikcain.altazgoto;

import androidx.room.Dao;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface TargetsDAO {

    @Query("SELECT * FROM targets")
    ListenableFuture<List<targets>> getAll();

    @Query("select * from targets where searchText like '%' || :searchterm || '%'")
    ListenableFuture<List<targets>> findTarget(String searchterm);

    @Query("select * from calibrationstars")
    ListenableFuture<List<calibrationstars>> getCalibrationStars();
}

