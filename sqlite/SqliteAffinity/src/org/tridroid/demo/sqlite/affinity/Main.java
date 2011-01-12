/*
Copyright (c) 2011 Luke Meyer
This program is licensed under the MIT license; see LICENSE file for details.
*/
package org.tridroid.demo.sqlite.affinity;

import java.util.HashMap;
import java.util.Map;

import org.tridroid.demo.sqlite.affinity.R;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends Activity implements OnClickListener {
    private EditText mInput;
	private Spinner mInDataType;
	private DbAdapter mDb;
	private Map<String, TextView> mOutputs;
	private Spinner mOutDataType;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mInput = (EditText) findViewById(R.id.input);
        mInDataType = (Spinner) findViewById(R.id.inDataType);
        mOutDataType = (Spinner) findViewById(R.id.outDataType);
        
        mOutputs = new HashMap<String, TextView>();
        mOutputs.put(DbAdapter.TEXT_VAL, (TextView) findViewById(R.id.textVal));
        mOutputs.put(DbAdapter.NUMERIC_VAL, (TextView) findViewById(R.id.numericVal));
        mOutputs.put(DbAdapter.INT_VAL, (TextView) findViewById(R.id.intVal));
        mOutputs.put(DbAdapter.REAL_VAL, (TextView) findViewById(R.id.realVal));
        mOutputs.put(DbAdapter.NONE_VAL, (TextView) findViewById(R.id.noneVal));
        
        findViewById(R.id.go).setOnClickListener(this);
        
        mDb = new DbAdapter(this);
    }

	@Override
	public void onClick(View v) {
		// convert value to type, save value into the db, then retrieve as type
		String value = mInput.getText().toString();
		ContentValues values = new ContentValues();

		
		for(String key : DbAdapter.COLUMNS) {
			try {
			switch(mInDataType.getSelectedItemPosition()) {
			case 0: // text 
			default: values.put(key, value); break;
			case 1: // int
				values.put(key, Integer.parseInt(value));
				break;
			case 2: // long
				values.put(key, Long.parseLong(value));
				break;
			case 3: // float
				values.put(key, Float.parseFloat(value));
				break;
			case 4: // double
				values.put(key, Double.parseDouble(value));
				break;
			}
			} catch (Exception e) {
				// throw away parsing errors
			}
		}

		// create and retrieve
		long rowId = mDb.createRow(values);
		Cursor row = mDb.getRow(rowId);
		for(String key : DbAdapter.COLUMNS) {
			int col = row.getColumnIndex(key);
			String retrieved; 
			switch(mOutDataType.getSelectedItemPosition()) {
				case 0: 
				default: retrieved = row.getString(col); break;
				case 1: retrieved = Integer.toString(row.getInt(col)); break;
				case 2: retrieved = Long.toString(row.getLong(col)); break;
				case 3: retrieved = Float.toString(row.getFloat(col)); break;
				case 4: retrieved = Double.toString(row.getDouble(col)); break;
			}
			mOutputs.get(key).setText(retrieved);
		}
		
		// clean up the row we created
		mDb.deleteRow(rowId);
	}
}