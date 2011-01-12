/*
Copyright (c) 2011 Luke Meyer
This program is licensed under the MIT license; see LICENSE file for details.
*/

package org.tridroid.demo.sqlite.affinity;

//import java.sql.Date;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
  
  DB Adapter for the application; where all the low-level interaction with the DB
  occurs, including creation/upgrade of the DB and storage of domain objects.

  @author Luke Meyer, Copyright 2010
  See LICENSE file for this file's GPLv3 distribution license.
  
 */

public class DbAdapter {

	
	/**	 name of the file used for the sqlite DB */
	private static final String DATABASE_NAME = "SqliteDemo.db";
	/** version of the DB (to determine if migration is needed) */
	private static final int DATABASE_VERSION = 1;
	/** Logging tag for this class */
    private static final String TAG = "SQLD.DBA";

    /** Helper that handles creating/migrating/opening the DB */
    private DbAdmin mDbHelper;
    /** Actual database handle */
    private SQLiteDatabase mDb;
    /** String resources from the application context */ 
	private final Resources resources;

	/** value-holding columns of the table */
	public static final String NONE_VAL = "noneVal";
	public static final String REAL_VAL = "realVal";
	public static final String INT_VAL = "intVal";
	public static final String NUMERIC_VAL = "numericVal";
	public static final String TEXT_VAL = "textVal";
	public final static String[] COLUMNS = {TEXT_VAL, NUMERIC_VAL, INT_VAL, REAL_VAL, NONE_VAL}; 
	
	/**
	 * Creates the database adapter that mediates all DB access
	 * 
	 * @param ctx The application context in which this adapter is created
	 */
    public DbAdapter(Context ctx) {
        resources = ctx.getResources();
        open(ctx);
	}
    
    /**
     * Opens a database handle, creating if needed. If it cannot be created, throw exception.
     * @param ctx 
     * 
     * @return this
     * @throws SQLException if the database could be neither opened or created
     */
    
    public DbAdapter open(Context ctx) throws SQLException {
    	if(mDbHelper != null) return this;
        mDbHelper = new DbAdmin(ctx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    /**
     * Close any open database handles.
     */
    public void close() {
    	if(mDbHelper == null) return;
        mDbHelper.close();
        mDb = null;
        mDbHelper = null;
    }
    
    public String getDbPath() {
    	return mDb.getPath();
    }
    
    /**
     *  Return a string defined in resource files
     * @param id The R.id for the string
     * @return The corresponding string requested
     */
	public String str(int id) {
		return (String) resources.getText(id);
	}
	
	public long createRow(ContentValues cv) {
		return mDb.insertOrThrow("demo", TEXT_VAL, cv);
	}

	public Cursor getRow(long rowid) {
		Cursor c = mDb.query("demo", COLUMNS, "_id = ?", new String[] {Long.toString(rowid)}, null, null, null);
		if(c != null)
			c.moveToFirst();
		return c;
	}
	
	public void deleteRow(long rowid) {
		mDb.delete("demo", "_id = ?", new String[] {Long.toString(rowid)});
	}
    
    // inner class for handling basic DB maintenance
	private class DbAdmin extends SQLiteOpenHelper {
		public DbAdmin(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override /* create DB tables */
		public void onCreate(SQLiteDatabase db) {
			onUpgrade(db, 0, DATABASE_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion == 0)
				Log.i(TAG, "Creating new database");
			else
				Log.i(TAG, "Upgrading database from version " + oldVersion
						+ " to " + newVersion);

			try {
				switch(oldVersion) {
				case 0:
					// initial schema creation
					db.execSQL(
							"create table demo (" +
							" _id INTEGER PRIMARY KEY, " +
							" textVal TEXT, " +
							" numericVal NUMBER, " +
							" intVal INTEGER, " +
							" realVal REAL, " +
							" noneVal" +
							")"
							);
					Log.i(TAG, "Created table demo");

					ContentValues firstRow = new ContentValues();
					for(String col : COLUMNS) {
						firstRow.put(col, "1");
					}
					db.insertOrThrow("demo", "", firstRow);
				case 1:
					// migration steps for 1 -> 2
				}

			} catch (SQLException e) {
				Log.e(TAG, e.getMessage(), e);
				throw (e);
			}
		}
	}
}
