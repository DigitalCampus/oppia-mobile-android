package org.digitalcampus.oppia.widgets.quiz;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;


public abstract class TextInputQuizWidget extends QuestionWidget {

    protected void hideOnFocusLoss(EditText et){
        View.OnFocusChangeListener ofcListener = new ResponseTextFocusChangeListener();
        et.setOnFocusChangeListener(ofcListener);
    }

    private class ResponseTextFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if(v.getId() == R.id.responsetext && !hasFocus) {
                InputMethodManager imm =  (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}
