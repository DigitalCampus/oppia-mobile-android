package org.digitalcampus.mtrain.listener;

import org.digitalcampus.mtrain.task.Payload;

public interface LoginListener {
	void loginComplete(Payload response);
}
