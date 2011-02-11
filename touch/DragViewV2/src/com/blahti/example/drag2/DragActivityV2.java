package com.blahti.example.drag2;

import com.blahti.example.drag2.R;

import android.app.Activity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity presents a screen on which images can be added and moved around.
 * It also defines areas on the screen where the dragged views can be dropped. Feedback is
 * provided to the user as the objects are dragged over these drop zones.
 *
 * <p> Like the DragActivity in the previous version of the DragView example application, the
 * code here is derived from the Android Launcher code.
 * 
 */

public class DragActivityV2 extends Activity 
    implements View.OnLongClickListener, View.OnClickListener
{


/**
 */
// Constants

private static final int ENABLE_S2_MENU_ID = Menu.FIRST;
private static final int DISABLE_S2_MENU_ID = Menu.FIRST + 1;
private static final int ADD_OBJECT_MENU_ID = Menu.FIRST + 2;

/**
 */
// Variables

private DragController mDragController;   // Object that sends out drag-drop events while a view is being moved.
private DragLayer mDragLayer;             // The ViewGroup that supports drag-drop.
private DropSpot mSpot2;                  // The DropSpot that can be turned on and off via the menu.

public static final boolean Debugging = false;

/**
 */
// Methods

/**
 * onCreate - called when the activity is first created.
 * 
 * Creates a drag controller and sets up three views so click and long click on the views are sent to this activity.
 * The onLongClick method starts a drag sequence.
 *
 */

 protected void onCreate(Bundle savedInstanceState) 
{
    super.onCreate(savedInstanceState);
    mDragController = new DragController(this);

    setContentView(R.layout.main);
    setupViews ();
}

/**
 * Build a menu for the activity.
 *
 */    

public boolean onCreateOptionsMenu (Menu menu) 
{
    super.onCreateOptionsMenu(menu);
    
    menu.add(0, ENABLE_S2_MENU_ID, 0, "Enable Spot2").setShortcut('1', 'c');
    menu.add(0, DISABLE_S2_MENU_ID, 0, "Disable Spot2").setShortcut('2', 'c');
    menu.add(0, ADD_OBJECT_MENU_ID, 0, "Add View").setShortcut('9', 'z');

    return true;
}

/**
 * Handle a click on a view.
 *
 */    

public void onClick(View v) 
{

    toast ("You clicked. Try a long click");
}

/**
 * Handle a long click.
 *
 * @param v View
 * @return boolean - true indicates that the event was handled
 */    

public boolean onLongClick(View v) 
{
    trace ("onLongClick in view: " + v);

    // Make sure the drag was started by a long press as opposed to a long click.
    // (Note: I got this from the Workspace object in the Android Launcher code. 
    //  I think it is here to ensure that the device is still in touch mode as we start the drag operation.)
    if (!v.isInTouchMode()) {
       toast ("isInTouchMode returned false. Try touching the view again.");
       return false;
    }
    return startDrag (v);
}

/**
 * Perform an action in response to a menu item being clicked.
 *
 */

public boolean onOptionsItemSelected (MenuItem item) 
{
    //mPaint.setXfermode(null);
    //mPaint.setAlpha(0xFF);

    switch (item.getItemId()) {
        case ENABLE_S2_MENU_ID:
            if (mSpot2 != null) mSpot2.setDragLayer (mDragLayer);
            return true;
        case DISABLE_S2_MENU_ID:
            if (mSpot2 != null) mSpot2.setDragLayer (null);
            return true;
        case ADD_OBJECT_MENU_ID:
            // Add a new object to the DragLayer and see if it can be dragged around.
            ImageView newView = new ImageView (this);
            newView.setImageResource (R.drawable.hello);
            int w = 60;
            int h = 60;
            int left = 80;
            int top = 100;
            DragLayer.LayoutParams lp = new DragLayer.LayoutParams (w, h, left, top);
            mDragLayer.addView (newView, lp);
            newView.setOnClickListener(this);
            newView.setOnLongClickListener(this);
            return true;
    }

    return super.onOptionsItemSelected(item);
}

/**
 * Start dragging a view.
 *
 */    

public boolean startDrag (View v)
{
    // Let the DragController initiate a drag-drop sequence.
    // I use the dragInfo to pass along the object being dragged.
    // I'm not sure how the Launcher designers do this.
    Object dragInfo = v;
    mDragController.startDrag (v, mDragLayer, dragInfo, DragController.DRAG_ACTION_MOVE);
    return true;
}

/**
 * Finds all the views we need and configure them to send click events to the activity.
 *
 */
private void setupViews() 
{
    DragController dragController = mDragController;

    mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
    mDragLayer.setDragController(dragController);
    dragController.addDropTarget (mDragLayer);

    ImageView i1 = (ImageView) findViewById (R.id.Image1);
    ImageView i2 = (ImageView) findViewById (R.id.Image2);

    i1.setOnClickListener(this);
    i1.setOnLongClickListener(this);

    i2.setOnClickListener(this);
    i2.setOnLongClickListener(this);

    TextView tv = (TextView) findViewById (R.id.Text1);
    tv.setOnLongClickListener(this);

    // Set up some drop targets and enable them by connecting them to the drag layer
    // and the drag controller.
    // Note: If the dragLayer is not set, the drop spot will not accept drops.
    // That is the initial state of the second drop spot.
    DropSpot drop1 = (DropSpot) mDragLayer.findViewById (R.id.drop_spot1);
    drop1.setup (mDragLayer, dragController, R.color.drop_target_color1);

    DropSpot drop2 = (DropSpot) mDragLayer.findViewById (R.id.drop_spot2);
    drop2.setup (null, dragController, R.color.drop_target_color2);

    DropSpot drop3 = (DropSpot) mDragLayer.findViewById (R.id.drop_spot3);
    drop3.setup (mDragLayer, dragController, R.color.drop_target_color1);

    // Save the second area so we can enable and disable it via the menu.
    mSpot2 = drop2;

    // Note: It might be interesting to allow the drop spots to be movable too.
    // Unfortunately, in the current implementation, that does not work
    // because the parent view of the DropTarget objects is not the drag layer.
    // The current DragLayer.onDrop method makes assumptions about how to reposition a dropped view.

    // Give the user a little guidance.
    Toast.makeText (getApplicationContext(), "Touch a view and hold to move a view. Use the menu to add new views.",
                    Toast.LENGTH_LONG).show ();

}

/**
 * Show a string on the screen via Toast.
 * 
 * @param msg String
 * @return void
 */

public void toast (String msg)
{
    Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_SHORT).show ();
} // end toast

/**
 * Send a message to the debug log and display it using Toast.
 */

public void trace (String msg) 
{
    if (!Debugging) return;
    Log.d ("DragActivity", msg);
    toast (msg);
}

} // end class
