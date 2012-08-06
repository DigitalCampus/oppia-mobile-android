package org.digitalcampus.mtrain.activity;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.R.layout;
import org.digitalcampus.mtrain.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class ModuleIndexActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        return true;
    }

    
}
