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

package org.digitalcampus.mobile.quiz.model.questiontypes;

import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.Response;

import java.io.Serializable;

public class ShortAnswer extends UserResponseQuestion implements Serializable {

    private static final long serialVersionUID = 3539362553016059321L;
    public static final String TAG = ShortAnswer.class.getSimpleName();


    @Override
    public void mark(String lang){
        // loop through the responses
        // find whichever are set as selected and add up the responses

        float total = 0;
        for (Response r : responseOptions){
            for (String a : this.userResponses) {
                if (r.getTitle(lang).equalsIgnoreCase(a.toLowerCase())) {
                    total += r.getScore();
                    if (r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))) {
                        this.feedback = r.getFeedback(lang);
                    }
                }
            }
        }
        if (total == 0){
            this.setFeedback(lang);
        }
        this.calculateUserscore(total);
    }

    private void setFeedback(String lang){
        for (Response r : responseOptions){
            if (r.getTitle(lang).equalsIgnoreCase("*") && r.getFeedback(lang) != null && !(r.getFeedback(lang).equals(""))){
                this.feedback = r.getFeedback(lang);
            }
        }
    }

    @Override
    public int getMaxScore() {
        return Integer.parseInt(this.getProp(Quiz.JSON_PROPERTY_MAXSCORE));
    }


}