package org.digitalcampus.mobile.learning.widgets.mquiz;

import java.util.List;

import org.digitalcampus.mquiz.model.Response;

public abstract class QuestionWidget {

	// Abstract methods
	public abstract void setQuestionResponses(List<Response> responses, List<String> currentAnswers);

	public abstract List<String> getQuestionResponses(List<Response> responses);

}
