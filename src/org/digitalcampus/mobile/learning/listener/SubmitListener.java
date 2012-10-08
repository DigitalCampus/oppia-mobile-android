package org.digitalcampus.mobile.learning.listener;

import org.digitalcampus.mobile.learning.task.Payload;

public interface SubmitListener {
	void submitComplete(Payload response);
}
