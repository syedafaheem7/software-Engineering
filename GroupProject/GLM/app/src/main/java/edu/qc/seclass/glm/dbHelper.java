package edu.qc.seclass.glm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;

public class dbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="GroceryDB";
    private static final String TABLE_NAME ="ListsofLists";
    private static final String GL_ID="GL_ID";
//    private static final String GL_NAME="GL_NAME";
    private static final String GL_DATA="DATA";



    private final Context context;


    public dbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME + " ("+GL_ID+" INTEGER PRIMARY KEY, "+GL_DATA+" BLOB);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void update(ListOfLists l){
        byte[] lolB = makeByte(l);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(dbHelper.GL_DATA, lolB);
        db.update(TABLE_NAME, cv, GL_ID + "=2", null);
    }

    public byte[] makeByte(ListOfLists gl){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(gl);
            byte[] glByte = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(glByte);

            return glByte;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ListOfLists getByte(byte[] glByte) {
        if (glByte != null) {

            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(glByte);
                ObjectInputStream ois = new ObjectInputStream(bais);
                ListOfLists gl = (ListOfLists) ois.readObject();

                return gl;
            } catch(IOException e){
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public void insertData(ListOfLists gl){
      byte[] data = makeByte(gl);
       SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cvs = new ContentValues();
        //cvs.put(dbHelper.TABLE_NAME,  name);
        cvs.put(dbHelper.GL_ID, 2);
        cvs.put(dbHelper.GL_DATA, data);
        db.insert(dbHelper.TABLE_NAME, null, cvs);
    }

    public ListOfLists getData(){
        ListOfLists lol = null;
        String[] glData = {dbHelper.GL_DATA};
        SQLiteDatabase db = getWritableDatabase();
        Cursor data  = db.query(true, dbHelper.TABLE_NAME, glData, null, null, null, null, null, null, null);
        while(data.moveToNext()){
            byte[] lolBytes = data.getBlob(data.getColumnIndex(dbHelper.GL_DATA));
            lol = getByte(lolBytes);
        }
        return lol;
    }

    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "message" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);

        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);

            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {

                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(Exception ex){
            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }
    }

}
