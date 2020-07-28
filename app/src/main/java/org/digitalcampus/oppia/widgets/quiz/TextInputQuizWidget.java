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

package org.digitalcampus.oppia.widgets.quiz;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class TextInputQuizWidget extends QuestionWidget {

    public TextInputQuizWidget(Activity activity, View v, ViewGroup container, int layout) {
        super(activity, v, container, layout);
    }

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

    @Override
    public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
        EditText et = view.findViewById(R.id.responsetext);
        Iterator<String> itr = currentAnswers.iterator();
        while(itr.hasNext()) {
            String answer = itr.next();
            et.setText(answer);
        }
        hideOnFocusLoss(et);
    }

    @Override
    public List<String> getQuestionResponses(List<Response> responses){
        EditText et = view.findViewById(R.id.responsetext);
        if(et.getText().toString().trim().equals("")){
            return new ArrayList<>();
        } else {
            List<String> response = new ArrayList<>();
            response.add(et.getText().toString().trim());
            return response;
        }
    }
}
