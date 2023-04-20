package com.example.crimeintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.crimeintent.Crime;
import com.example.crimeintent.database.CrimeDbSchema.CrimeTable;

import java.time.LocalDateTime;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {

    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {
        String uuidString = getString(getColumnIndex(CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeTable.Cols.TITLE));
        LocalDateTime localDateTime =LocalDateTime.parse(getString(getColumnIndex(CrimeTable.Cols.DATE)));
        int isSolved = getInt(getColumnIndex(CrimeTable.Cols.SOLVED));
        String suspect = getString(getColumnIndex(CrimeTable.Cols.SUSPECT));
        long number = getLong(getColumnIndex(CrimeTable.Cols.NUMBER));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDateTime(localDateTime);
        crime.setSolved(isSolved!=0);
        crime.setSuspect(suspect);
        crime.setNumber(number);
        return crime;
    }
}
