package org.digitalcampus.oppia.listener;

public interface GamificationEventListener {
    void onEvent(String message, int points);
}
