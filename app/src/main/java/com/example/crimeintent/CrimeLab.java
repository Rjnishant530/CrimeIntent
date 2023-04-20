package com.example.crimeintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.crimeintent.database.CrimeBaseHelper;
import com.example.crimeintent.database.CrimeCursorWrapper;
import com.example.crimeintent.database.CrimeDbSchema;
import com.example.crimeintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;


    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext)
                .getWritableDatabase();
    }
    public static CrimeLab getInstance(Context context){
        if(sCrimeLab== null)
            sCrimeLab = new CrimeLab(context);
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        CrimeCursorWrapper cursor = queryCrimes(null, null);

        List<Crime> crimes= new ArrayList<>();
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return crimes;
    }


    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    public void addCrime(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeDbSchema.CrimeTable.NAME, null, values);
    }

    public void deleteItem(UUID uuid){
        String uuidString = uuid.toString();
        mDatabase.delete(CrimeTable.NAME,CrimeTable.Cols.UUID+" = ?",new String[]{uuidString});
    }

    public int size(){
        return (int)DatabaseUtils.longForQuery(mDatabase,"SELECT count(*) FROM "+ CrimeTable.NAME,null);
    }

    public Crime get(int index){
        CrimeCursorWrapper crimeCursorWrapper = queryCrimes(null,null);
        try {
            crimeCursorWrapper.moveToPosition(index);
            return crimeCursorWrapper.getCrime();
        }
        finally {
            crimeCursorWrapper.close();
        }

    }

    public int getIndex(UUID uuid){
        CrimeCursorWrapper cursor = queryCrimes(null, null);

       int index=0;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if(cursor.getCrime().getId().compareTo(uuid)==0){
                    return index;
                }
                cursor.moveToNext();
                index++;
            }
            return 0;
        } finally {
            cursor.close();
        }

    }
    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDateTime().toString());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
        values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
        values.put(CrimeTable.Cols.NUMBER,crime.getNumber());

        return values;
    }

    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME, values,
                CrimeTable.Cols.UUID + " = ?",
                new String[] { uuidString });

    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, // columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );
        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

}
