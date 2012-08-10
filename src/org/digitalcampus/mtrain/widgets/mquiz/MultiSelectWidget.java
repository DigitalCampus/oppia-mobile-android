package org.digitalcampus.mtrain.widgets.mquiz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mquiz.model.Response;
import org.digitalcampus.mtrain.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class MultiSelectWidget extends QuestionWidget {

	public static final String TAG = "MultiSelectWidget";
	private Context ctx;
	private LinearLayout responsesLL;
	
	public MultiSelectWidget(Context context) {
		this.ctx = context;
		
		LinearLayout ll = (LinearLayout) ((Activity) ctx).findViewById(R.id.quizResponseWidget);
		ll.removeAllViews();
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.widget_mquiz_multiselect, null);
		ll.addView(vv);
	}

	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		responsesLL = (LinearLayout) ((Activity) ctx).findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();
    	
    	for (Response r : responses){
    		CheckBox chk= new CheckBox(ctx);  
    		chk.setText(r.getText());
    		responsesLL.addView(chk);
    		Iterator<String> itr = currentAnswer.iterator();
    		while(itr.hasNext()){
    			String a = itr.next(); 
    			if(a.equals(r.getText())){
    				chk.setChecked(true);
    			}
    		}
    	}	
	}

	@Override
	public List<String> getQuestionResponses(List<Response> responses) {
		int count = responsesLL.getChildCount();
		List<String> response = new ArrayList<String>();
		for (int i=0; i<count; i++) {
			CheckBox cb = (CheckBox) responsesLL.getChildAt(i);
			if(cb.isChecked()){
				response.add(cb.getText().toString());
			}
		}

		if(response.size() == 0){
			return null;
		} else {
			return response;
		}
	}

}
