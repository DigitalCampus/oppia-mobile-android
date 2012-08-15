package org.digitalcampus.mtrain.listener;

import org.digitalcampus.mtrain.task.Payload;

public interface SubmitListener {
	void submitComplete(Payload response);
}
