package ru.spbau.nonograms.local_database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * A helper for using database.
 * Database is a table which has links to different files,
 * where serialized crosswords are stored theirselves.
 */
public class CrosswordDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "CrosswordList";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NUMBER_OF_COLORS = "number_of_colors";
    public static final String COLUMN_CROSSWORD_NAME = "crossword_name";
    public static final String COLUMN_AUTHOR_NAME = "author_name";


    private Context context;

    /**
     * A database helper init.
     * @param context a context of the application.
     */
    public CrosswordDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    /**
     * A method which is called when there's no needed database.
     * @param db database to create.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_NUMBER_OF_COLORS + " INTEGER," +
                 COLUMN_CROSSWORD_NAME + " TEXT," + COLUMN_AUTHOR_NAME + " TEXT )");
        for (int i = 0; i < StandartCrosswords.toAdd.length; i++) {
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CROSSWORD_NAME, "standart");
                values.put(COLUMN_AUTHOR_NAME, "standart");
                values.put(COLUMN_NUMBER_OF_COLORS, StandartCrosswords.toAdd[i].getColors().length);
                long rowId = db.insert(DATABASE_NAME, null, values);
                try (ObjectOutputStream outputStream =
                             new ObjectOutputStream(context.openFileOutput("standart" + rowId, Context.MODE_PRIVATE))) {
                    outputStream.writeObject(StandartCrosswords.toAdd[i]);
                    outputStream.close();
                }
            } catch (IOException e) {
                Log.e("DBHelper: ", e.getMessage());
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(db);
    }

    /**
     * Update crossword by its file name.
     * @param filename the name of a file, where crossword to renew stored.
     * @param crosswordState a crossword to save.
     * @throws IOException if didn't manage to access the file or to put data there.
     */
    public void updateByFilename(String filename, CurrentCrosswordState crosswordState) throws IOException {
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(context.openFileOutput(filename, Context.MODE_PRIVATE))) {
            outputStream.writeObject(crosswordState);
            outputStream.close();
        }
    }

    /**
     * Add crossword to the database
     * @param crosswordState a crossword to add
     * @param crosswordName a name of  crossword
     * @param authorName a name of author of the crossword
     * @throws IOException if didn't manage to create file or save crossword
     */
    public void addCrossword(CurrentCrosswordState crosswordState,
                             String crosswordName, String authorName) throws IOException {
        try (SQLiteDatabase database = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CROSSWORD_NAME, crosswordName);
            values.put(COLUMN_AUTHOR_NAME, authorName);
            values.put(COLUMN_NUMBER_OF_COLORS, crosswordState.getColors().length);
            Log.d("Colors: ", crosswordState.getColors().length + " inserted");
            long rowId = database.insert(DATABASE_NAME, null, values);
            try (ObjectOutputStream outputStream =
                         new ObjectOutputStream(context.openFileOutput(crosswordName + rowId, Context.MODE_PRIVATE))) {
                outputStream.writeObject(crosswordState);
                outputStream.close();
            }
            Log.d("Database", "inserted row number " + rowId);
        }
    }

    /**
     * Returns crossword stored in database by its filename.
     * @param filename a name of a file, where crossword stores.
     * @return crossword stored in the database.
     * @throws IOException if didn't manage to access file.
     * @throws ClassNotFoundException if didn't manage to deserialize a crossword.
     */
    public CurrentCrosswordState getCrosswordByFilename(String filename)
            throws IOException, ClassNotFoundException {
        CurrentCrosswordState result = null;
        try (SQLiteDatabase database = this.getReadableDatabase();
             Cursor cursor = database.rawQuery("SELECT * FROM " + DATABASE_NAME, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_CROSSWORD_NAME));
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    if (filename.equals(name + id)) {
                        result = getCurrentCrosswordStateByCursor(cursor);
                        break;
                    }
                } while (cursor.moveToNext());
            }
        }
        return result;
    }

    /**
     * Returns a list of information pieces about crosswords stored in database.
     * @return a list of information pieces about crosswords stored in database.
     */
    public ArrayList<CrosswordInfo> getAllCrosswords() {
        ArrayList<CrosswordInfo> result = new ArrayList<>();
        try (SQLiteDatabase database = this.getReadableDatabase();
             Cursor cursor = database.rawQuery("SELECT * FROM " + DATABASE_NAME, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_CROSSWORD_NAME));
                    String authorName = cursor.getString(cursor.getColumnIndex(COLUMN_AUTHOR_NAME));
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    int colors = cursor.getInt(cursor.getColumnIndex(COLUMN_NUMBER_OF_COLORS));
                    CrosswordInfo info = new CrosswordInfo(name, authorName, colors, id);
                    result.add(info);
                } while (cursor.moveToNext());
            }
        }
        return result;
    }

    private CurrentCrosswordState getCurrentCrosswordStateByCursor(Cursor cursor)
            throws IOException, ClassNotFoundException {
        String name = cursor.getString(cursor.getColumnIndex(COLUMN_CROSSWORD_NAME));
        int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        CurrentCrosswordState crossword = null;
        try (ObjectInputStream inputStream = new ObjectInputStream(context.openFileInput(name + id))) {
            crossword = (CurrentCrosswordState) inputStream.readObject();
        }
        return crossword;
    }
}
