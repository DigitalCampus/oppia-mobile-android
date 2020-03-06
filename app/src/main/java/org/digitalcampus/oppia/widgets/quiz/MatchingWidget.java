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


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MatchingWidget extends QuestionWidget {

	public static final String TAG = MatchingWidget.class.getSimpleName();

	private LinearLayout[] responseLayouts;
	
	public MatchingWidget(Activity activity, View v, ViewGroup container) {
		super(activity, v, container, R.layout.widget_quiz_matching);
	}

	@Override
	public void setQuestionResponses(List<String> currentAnswers) {
		// not used for this widget
	}

	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		LinearLayout responsesLL = view.findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();
    	
    	// this could be tidied up - to use ArrayAdapters/Lists
    	HashMap<String,String> possibleAnswers = new HashMap<>();
    	ArrayList<String> possibleAnswersShuffle = new ArrayList<>();
    	int noresponses = 0;
    	for (Response r : responses){
    		String[] temp = r.getTitle(currentUserLang).split(Quiz.MATCHING_REGEX,-1);
    		if(!temp[0].equals("")){
    			noresponses++;
    		}
    		possibleAnswers.put(temp[0].trim(),temp[1].trim());
    		possibleAnswersShuffle.add(temp[1].trim());
    	}

    	Iterator<Entry<String, String>> responseIt = possibleAnswers.entrySet().iterator();
    	int counter = 0;
    	responseLayouts = new LinearLayout[noresponses];
    	
    	while (responseIt.hasNext()) {
    		Map.Entry<String,String> responsePairs = responseIt.next();
    		// only add if there is question text
    		if(!responsePairs.getKey().equals("")){
	    		LinearLayout responseLayout = new LinearLayout(ctx);
	    		responseLayout.setOrientation(LinearLayout.VERTICAL);  
	    		TextView tv = new TextView(ctx);
	    		
	    		tv.setText(responsePairs.getKey());
	    		
	    		Spinner spinner = new Spinner(ctx);
	    		ArrayAdapter<CharSequence> responseAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item);
	    		responseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		spinner.setAdapter(responseAdapter); 
	    		
	    		responseAdapter.add("");
	    		Collections.shuffle(possibleAnswersShuffle);
	    		for (String s: possibleAnswersShuffle){
	    	        responseAdapter.add(s); 
	    	    }

	    		responseLayout.addView(tv);
	    		responseLayout.addView(spinner);
	    		
	    		// set the selected item based on current responses
	    		for (String s : currentAnswer){
	        		String[] temp = s.split(Quiz.MATCHING_REGEX,-1);
	        		if(temp[0].trim().equals(responsePairs.getKey())){
	        			int i = responseAdapter.getPosition(temp[1].trim());
	        			spinner.setSelection(i);	
	        		}
	        	}
	    		responsesLL.addView(responseLayout);
	    		responseLayouts[counter] = responseLayout;
	    		counter++;
    		} 
    	} 
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		
		List<String> userResponses = new ArrayList<>();
		
		for (LinearLayout ll : this.responseLayouts){
			TextView tv = (TextView) ll.getChildAt(0);
			Spinner sp = (Spinner) ll.getChildAt(1);
			if(!sp.getSelectedItem().toString().trim().equals("")){
				String response = tv.getText().toString().trim() + Quiz.MATCHING_SEPARATOR + sp.getSelectedItem().toString().trim();
				userResponses.add(response);
			}
		}
		if(userResponses.isEmpty()){
			return new ArrayList<>();
		}
    	return userResponses;
	}

	@Override
	public List<String> getQuestionResponses() {
		return new ArrayList<>();
	}

}
