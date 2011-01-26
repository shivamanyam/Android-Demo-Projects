package net.sosiouxme.listviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends ListActivity {
	
	private static final int DIALOG_LAYOUT = 0;
	private static final int DIALOG_SIZE = 1;
	private static final int DIALOG_SCROLLING_HEADER = 2;
	private static final int DIALOG_SCROLLING_FOOTER = 3;
	private static final int DIALOG_EVENTS = 4;
	private static final int DIALOG_STRATEGIES = 5;
	private static final String PREF_LAYOUTS = "layouts";
	private static final String PREF_SIZES = "sizes";
	private static final String PREF_SCROLLING_HEADER = "scrolling_header";
	private static final String PREF_SCROLLING_FOOTER = "scrolling_footer";
	public static final String TAG = "Listviews";
	private SharedPreferences mPrefs = null;
	private View mHeaderView = null;
	private View mFooterView = null;
	public int mDialogShown;
	
	private static int[] LAYOUTS = {
		R.layout.l_01_plain,
		R.layout.l_02_static_header_linear,
		R.layout.l_03_static_footer_linear,
		R.layout.l_04_naive_static_both_linear,
		R.layout.l_04b_static_both_linear_fix,
		R.layout.l_05_naive_static_both_relative,
		R.layout.l_05b_static_both_relative
	};
	private static int[] SIZES = {
		R.array.size_0,
		R.array.size_1,
		R.array.size_3,
		R.array.size_10
	};
	private static int[] SCROLLING_HEADERS = {
		0,
		R.layout.w_scrolling_header,
		R.layout.w_scrolling_header_wbutton,
		R.layout.w_scrolling_header_wedit,
		R.layout.w_scrolling_header_wboth
	};
	private static int[] SCROLLING_FOOTERS = {
		0,
		R.layout.w_scrolling_footer,
		R.layout.w_scrolling_footer_wbutton,
		R.layout.w_scrolling_footer_wedit,
		R.layout.w_scrolling_footer_wboth
	};

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setupListView();        
    }

    private void setupListView() {
        ListView lv = getListView();

        // get rid of any previous header/footer settings
    	if(mHeaderView != null) lv.removeHeaderView(mHeaderView);
    	if(mFooterView != null) lv.removeFooterView(mFooterView);
    	mHeaderView = mFooterView = null;
    	setListAdapter(null); // can't set new headers/footers with adapter still there
    	
    	// now set up the listview and adapter
    	setContentView(LAYOUTS[getIntPref(PREF_LAYOUTS)]);
        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, SIZES[getIntPref(PREF_SIZES)], android.R.layout.simple_list_item_1);
        lv = getListView();

        // add a header if one is specified
        int sHeaderLayout = SCROLLING_HEADERS[getIntPref(PREF_SCROLLING_HEADER)];
        if(sHeaderLayout > 0) {
        	EventListener el = new EventListener("header", R.menu.header_item);
        	mHeaderView = getLayoutInflater().inflate(sHeaderLayout, null);
        	lv.addHeaderView(mHeaderView);
        	addHFEventListeners(mHeaderView, el);
        }

        // add a footer if one is specified
        int sFooterLayout = SCROLLING_FOOTERS[getIntPref(PREF_SCROLLING_FOOTER)];
        if(sFooterLayout > 0) {
        	EventListener el = new EventListener("footer", R.menu.footer_item);
        	mFooterView = getLayoutInflater().inflate(sFooterLayout, null);
        	lv.addFooterView(mFooterView);
        	addHFEventListeners(mFooterView, el);
        }
        
        // must come after adding header/footer
        setListAdapter(aa);

        EventListener el = new EventListener("list");
        
        // set a listener to respond when something is clicked on
        if(getBoolPref(Events.LI_CLICK)) lv.setOnItemClickListener(el);
        // set a listener to respond when something is long-clicked
        if(getBoolPref(Events.LI_LONGCLICK)) lv.setOnItemLongClickListener(el);
        // set a listener to respond when a context menu is created
        if(getBoolPref(Events.LI_CTXMENU)) lv.setOnCreateContextMenuListener(el);
        // set a listener to set focus properly when sub-elements get focus
        if(getBoolPref(Strategy.FOCUS_SELECT_LISTEN)) {
        	lv.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        	lv.setOnItemSelectedListener(new FocusSelectListener());
        }
        if(getBoolPref(Strategy.SET_ITEMS_CAN_FOCUS)) {
        	lv.setItemsCanFocus(true);
        }
    }

	private void addHFEventListeners(View v, EventListener el) {
        // set a listener to respond when something is clicked on
        if(getBoolPref(Events.HF_CLICK)) v.setOnClickListener(el);
        // set a listener to respond when a button is clicked on
		if(getBoolPref(Events.HF_BUTTON_CLICK)) {
			Button b = (Button) v.findViewById(R.id.button);
			if(b != null)
				b.setOnClickListener(el);
		}
        // set a listener to respond when something is long-clicked
		if(getBoolPref(Events.HF_LONGCLICK))
			v.setOnLongClickListener(el);
        // set a listener to respond when a context menu is created
		if(getBoolPref(Events.HF_CTXMENU))
			v.setOnCreateContextMenuListener(el);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
    	return super.onCreateOptionsMenu(menu);
   	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.layout:
			showDialog(DIALOG_LAYOUT);
			break;
		case R.id.size:
			showDialog(DIALOG_SIZE);
			break;
		case R.id.scrolling_header:
			showDialog(DIALOG_SCROLLING_HEADER);
			break;
		case R.id.scrolling_footer:
			showDialog(DIALOG_SCROLLING_FOOTER);
			break;
		case R.id.events:
			showDialog(DIALOG_EVENTS);
			break;
		case R.id.strategies:
			showDialog(DIALOG_STRATEGIES);
			break;
		}
    	return super.onOptionsItemSelected(item);
    }
	

	
	private void setIntPref(String pref, int i) {
		Log.d(TAG, "set " + pref + " to " + i);
		mPrefs.edit().putInt(pref, i).commit();
	}
	private int getIntPref(String pref) {
		return mPrefs.getInt(pref, 0);
	}
	protected void setBoolPref(String pref, boolean b) {
		Log.d(TAG, "set " + pref + " to " + b);
		mPrefs.edit().putBoolean(pref, b).commit();
	}
	protected boolean getBoolPref(String pref) {
		return mPrefs.getBoolean(pref, false);
	}
	protected boolean getBoolPref(Events ev) {
		return getBoolPref(ev.toString());
	}
	protected boolean getBoolPref(Strategy s) {
		return getBoolPref(s.toString());
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_LAYOUT:
		return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_layout)
			.setSingleChoiceItems(R.array.layout, getIntPref(PREF_LAYOUTS), new DialogListener(PREF_LAYOUTS))
			.create();
		case DIALOG_SIZE:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_size)
			.setSingleChoiceItems(R.array.size, getIntPref(PREF_SIZES), new DialogListener(PREF_SIZES))
			.create();
		case DIALOG_SCROLLING_HEADER:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_scrolling_header)
			.setSingleChoiceItems(R.array.scrolling_header, getIntPref(PREF_SCROLLING_HEADER), new DialogListener(PREF_SCROLLING_HEADER))
			.create();
		case DIALOG_SCROLLING_FOOTER:
			return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_scrolling_footer)
			.setSingleChoiceItems(R.array.scrolling_footer, getIntPref(PREF_SCROLLING_FOOTER), new DialogListener(PREF_SCROLLING_FOOTER))
			.create();
		case DIALOG_EVENTS:
			DialogListener listener = new DialogListener("events");
			return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_events)
			.setMultiChoiceItems(R.array.events, Events.getPrefs(this), listener)
			.setPositiveButton(R.string.ok_button, listener)
			.setOnCancelListener(listener)
			.create();
		case DIALOG_STRATEGIES:
			listener = new DialogListener("strategies");
			return new AlertDialog.Builder(this)
			.setTitle(R.string.choose_strategy)
			.setMultiChoiceItems(R.array.strategy, Strategy.getPrefs(this), listener)
			.setPositiveButton(R.string.ok_button, listener)
			.setOnCancelListener(listener)
			.create();
		}
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		mDialogShown = id;
		super.onPrepareDialog(id, dialog);
	}
	
	class EventListener implements View.OnClickListener, OnCreateContextMenuListener, OnItemClickListener, OnItemLongClickListener, OnLongClickListener {
		private final String context;
		private final int menuId;

		public EventListener(String what) {
				context = what;
				menuId = R.menu.list_item;
		}
		public EventListener(String what, int menuId) {
			context = what;
			this.menuId = menuId;
		}
		
		// View.OnClickListener
		public void onClick(View v) {
			String msg = "clicked on " + context + " " + v.getClass().getSimpleName();
			Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
			Log.d(TAG, msg);
		}

		// OnCreateContextMenuListener		
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			String msg = "Creating ctxt menu for View "
							+ v.getClass().getSimpleName()
							+ (menuInfo == null ? "" : menuInfo.toString());
			Toast.makeText(Main.this, msg, Toast.LENGTH_LONG).show();
			Log.d(TAG, msg);
			if(menu.size() > 0) {
				Log.d(TAG, "Menu already created, skipping.");				
			} else {
				getMenuInflater().inflate(menuId, menu);
			}
		}

		// OnItemClickListener 
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// When clicked, show a toast with the position
			String msg = "Clicked on item at position " + position;
			Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
			Log.d(TAG, msg);
		}

		// OnItemLongClickListener
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			String msg = "Long-clicked on item at position " + position;
			Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
			Log.d(TAG, msg);
			return false;
		}
		
		// OnLongClickListener 
		public boolean onLongClick(View v) {
			String msg = "Long-clicked on view " + v.getClass().getSimpleName();
			Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
			Log.d(TAG, msg);
			return false;
		}
	}
	
	class FocusSelectListener implements OnItemSelectedListener {
		// OnItemSelectedListener, part of the FOCUS_SELECT_LISTEN strategy
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			ListView listView = getListView();
			Log.d(TAG, "onItemSelected gave us " + view.toString());
			Button b = (Button) view.findViewById(R.id.button);
			EditText et = (EditText) view.findViewById(R.id.editor);
		    if (b != null || et != null) {
		        // Use afterDescendants to keep ListView from getting focus
		        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		        if(et!=null) et.requestFocus();
		        else if(b!=null) b.requestFocus();
		    } else {
		        if (!listView.isFocused()) {
		            // Use beforeDescendants so that previous selections don't re-take focus
		            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		            listView.requestFocus();
		        }
		    }

		}
		public void onNothingSelected(AdapterView<?> parent) {
			// happens on scrolling - make sure listview gets focus by default
			getListView().setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	}
	
	class DialogListener implements Dialog.OnClickListener, OnMultiChoiceClickListener, OnCancelListener {
		private final String pref;
		public DialogListener(String pref) {
			this.pref = pref;
		}
		
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			setIntPref(this.pref, which);
			setupListView();
		}

		public void onClick(DialogInterface dialog, int which, boolean isChecked) {
			if(mDialogShown == DIALOG_EVENTS)
				setBoolPref(Events.values()[which].toString(), isChecked);
			else 		
				setBoolPref(Strategy.values()[which].toString(), isChecked);
		}

		// in the case of the multi-item select, make sure to re-create
		// the screen on cancel as options may have changed
		public void onCancel(DialogInterface dialog) {
			setupListView();
		}
		
	}
	
	enum Events {
		// need to be the same order as the array describing event enablement 
		LI_CLICK("event_li_click"),
		LI_BUTTON_CLICK("event_li_button_click"),
		LI_LONGCLICK("event_li_lclick"),
		LI_CTXMENU("event_li_ctx"),
		HF_CLICK("event_hf_click"),
		HF_BUTTON_CLICK("event_hf_button_click"),
		HF_LONGCLICK("event_hf_lclick"),
		HF_CTXMENU("event_hf_ctx"),
		;
		
		private final String pref;
		private Events(String pref) {
			this.pref = pref;
		}
		@Override
		public String toString() {
			return pref;
		}
		
		public static boolean[] getPrefs(Main m) {
			Events[] values = values();
			boolean[] arr = new boolean[values.length];
			for(int i = 0; i < values.length; i++) {
				arr[i] = m.getBoolPref(values[i].pref);
			}
		  return arr;	
		}
		public static void setPrefs(Main m, boolean[] values) {
			Events[] events = values();
			for(int i = 0; i < values.length; i++) {
				m.setBoolPref(events[i].pref, values[i]);
			}			
		}
	}
	
	enum Strategy {
		// need to be the same order as the array describing strategy setting
		
		SET_ITEMS_CAN_FOCUS("strategy_items_can_focus"),
		// Select whether the item gets focus or the subelements; use a listener
		// when the item is selected and check children and assign focus; see
		// http://stackoverflow.com/questions/2679948/focusable-edittext-inside-listview
		FOCUS_SELECT_LISTEN("strategy_enable_subelement_focus")
		;
		
		private final String pref;
		private Strategy(String pref) {
			this.pref = pref;
		}
		@Override
		public String toString() {
			return pref;
		}
		
		public static boolean[] getPrefs(Main m) {
			Strategy[] values = values();
			boolean[] arr = new boolean[values.length];
			for(int i = 0; i < values.length; i++) {
				arr[i] = m.getBoolPref(values[i].pref);
			}
		  return arr;	
		}
		public static void setPrefs(Main m, boolean[] values) {
			Strategy[] events = values();
			for(int i = 0; i < values.length; i++) {
				m.setBoolPref(events[i].pref, values[i]);
			}			
		}
	}

}