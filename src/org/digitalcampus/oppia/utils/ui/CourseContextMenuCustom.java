package org.digitalcampus.oppia.utils.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import org.digitalcampus.mobile.learning.R;

public class CourseContextMenuCustom {

    private ListView courseListView;
    private Context ctx;
    private Dialog contextMenuDialog;
    private int currentSelectedItem;
    private int selectedOption;
    private OnContextMenuListener listener;

    public CourseContextMenuCustom(Context ctx){
        this.ctx = ctx;
        createDialog();
    }

    public void registerForContextMenu(ListView courseListView, OnContextMenuListener listener) {
        this.listener = listener;
        courseListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentSelectedItem = i;
                selectedOption = -1;
                contextMenuDialog.show();
                return true;
            }
        });
    }

    private void createDialog(){
        contextMenuDialog = new Dialog(ctx);
        contextMenuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        contextMenuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        contextMenuDialog.setContentView(R.layout.dialog_course_contextmenu);
        registerMenuClick(R.id.course_context_reset);
        registerMenuClick(R.id.course_context_delete);
        registerMenuClick(R.id.course_context_update_activity);

        contextMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                listener.onContextMenuItemSelected(currentSelectedItem, selectedOption);
            }
        });
    }

    private void registerMenuClick(final int id){
        contextMenuDialog.findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedOption = id;
                contextMenuDialog.dismiss();
            }
        });
    }

    public interface OnContextMenuListener{
        void onContextMenuItemSelected(int position, int itemId);
    }

}
