/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */


package org.digitalcampus.oppia.utils.storage;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;

public class GrantStorageAccessFragment extends Fragment implements ListInnerBtnOnClickListener {

    private static final int REQUEST_GRANT_CODE = 12;
    private static final int RESULT_OK = 1;
    private static final int RESULT_DISMISS = -1;
    public static final String FRAGMENT_TAG = "GrantStorageAccessFragment";

    public interface AccessGrantedListener{
        void onAccessGranted(Uri pathAccessGranted);
    }

    private AccessGrantedListener listener;
    public void setListener(AccessGrantedListener listener){
        this.listener = listener;
    }

    /**      
     * @deprecated
     */
    @Override
    @Deprecated
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        InfoDialog dialog = new InfoDialog();
        dialog.setListener(this);
        dialog.show(fragmentManager, "dialog");
    }

    @Override
    public void onClick(int result) {
        if (result == RESULT_DISMISS){
            if (listener != null) listener.onAccessGranted(null);
        }
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_GRANT_CODE);
        }
    }

    /**      
     * @deprecated
     */
    @Override
    @Deprecated
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_GRANT_CODE && resultCode == Activity.RESULT_OK) {
            Uri treeUri = resultData.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                final ContentResolver resolver = getActivity().getContentResolver();
                resolver.takePersistableUriPermission(treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            if (listener != null) listener.onAccessGranted(treeUri);
        }
        else{
            listener.onAccessGranted(null);
        }
    }

    public static class InfoDialog extends DialogFragment implements View.OnClickListener {

        public InfoDialog(){
            // do nothing
        }

        private ListInnerBtnOnClickListener listener;
        public void setListener(ListInnerBtnOnClickListener listener){
            this.listener = listener;
        }
        private boolean intentSent = false;

        /**      
         * @deprecated
         */
        @Override
        @Deprecated
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View view = inflater.inflate(R.layout.dialog_lollipop_storage_access, container, false);
            Button acceptButton = view.findViewById(R.id.acceptBtn);

            acceptButton.setOnClickListener(this);
            getDialog().setCanceledOnTouchOutside(true);
            return view;
        }

        /**      
         * @deprecated
         */
        @Override
        @Deprecated
        public void onResume() {
            Window window = getDialog().getWindow();
            final WindowManager.LayoutParams attrs = window.getAttributes();

            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getMetrics(metrics);

            attrs.gravity = Gravity.CENTER;
            attrs.height = (int) (metrics.heightPixels * 0.8);
            attrs.width = (int) (metrics.widthPixels * 0.8);
            window.setAttributes(attrs);

            super.onResume();
        }

        /**      
         * @deprecated
         */
        @Override
        @Deprecated
        public void onCancel(DialogInterface dialog) {
            if (!intentSent && (listener!=null))
                listener.onClick(RESULT_DISMISS);
            listener = null;
            super.onCancel(dialog);
        }

        @Override
        public void onClick(View v) {
            intentSent = true;
            getDialog().cancel();
            if (listener!=null) listener.onClick(RESULT_OK);
        }
    }


}
