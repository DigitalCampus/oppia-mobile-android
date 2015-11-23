package org.digitalcampus.oppia.utils.storage;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class GrantStorageAccessFragment extends Fragment {

    private static final int REQUEST_GRANT_CODE = 12;
    public static final String FRAGMENT_TAG = "GrantStorageAccessFragment";

    public interface AccessGrantedListener{
        void onAccessGranted(Uri pathAccessGranted);
    }
    private AccessGrantedListener listener;


    public void setListener(AccessGrantedListener listener){
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            this.startActivityForResult(intent, REQUEST_GRANT_CODE);
        }
    }

    @Override
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
}
