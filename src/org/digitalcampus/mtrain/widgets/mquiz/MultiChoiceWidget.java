package org.digitalcampus.mtrain.widgets.mquiz;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mquiz.model.Response;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.widgets.QuizWidget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MultiChoiceWidget extends QuestionWidget{

	private static final String TAG = "MultipleChoiceWidget";
	
	private Context ctx;
	
	public MultiChoiceWidget(Context context) {
		this.ctx = context;
		
		LinearLayout ll = (LinearLayout) ((Activity) ctx).findViewById(R.id.quizResponseWidget);
		ll.removeAllViews();
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.widget_quiz_multichoice, null);
		ll.addView(vv);
	}

	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		LinearLayout responsesLL = (LinearLayout) ((Activity) ctx).findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();
    	RadioGroup responsesRG = new RadioGroup(ctx);
    	// TODO change to use getchild views (like the MultiSelect)
    	responsesRG.setId(234523465);
    	responsesLL.addView(responsesRG);
    	int id = 1;
    	for (Response r : responses){
    		RadioButton rb = new RadioButton(ctx);
    		rb.setId(id);
    	
			rb.setText(r.getText());
			responsesRG.addView(rb);
			Iterator<String> itr = currentAnswer.iterator();
			while(itr.hasNext()) {
				String answer = itr.next(); 
				if (r.getText() == answer){
					rb.setChecked(true);
				}
			}
			id++;
    	}
		
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		// TODO change to use getchild views (like the MultiSelect)
		RadioGroup responsesRG = (RadioGroup) ((Activity) ctx).findViewById(234523465);
		int resp = responsesRG.getCheckedRadioButtonId();
    	View rb = responsesRG.findViewById(resp);
    	int idx = responsesRG.indexOfChild(rb);
    	if (idx >= 0){
    		List<String> response = new ArrayList<String>();
			response.add(responses.get(idx).getText());
    		return response;
    	}
    	return null;
	}

}
