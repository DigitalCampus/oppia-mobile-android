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

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class APIUserRequestTask extends APIRequestTask<String, Object, BasicResult>{

	private APIRequestListener requestListener;
	private boolean apiKeyInvalidated = false;

    public APIUserRequestTask(Context ctx) { super(ctx); }
    public APIUserRequestTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected BasicResult doInBackground(String... params){

        long now = System.currentTimeMillis();
        String url = params[0];
        
        BasicResult result = new BasicResult();
        
		try {

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, url)).build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                result.setSuccess(true);
                result.setResultMessage(response.body().string());
            }
            else{
                switch (response.code()) {
                    case 401:
                        invalidateApiKey(result);
                        apiKeyInvalidated = true;
                        break;

                    case 403: // unauthorised
                        result.setSuccess(false);
                        result.setResultMessage(ctx.getString(R.string.error_login));
                        break;

                    default:
                        result.setSuccess(false);
                        result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

		}  catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IO exception", e);
			result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection));
		}

        long spent = System.currentTimeMillis() - now;
        Log.d(TAG, "Spent " + spent + " ms");
        return result;
	}
	
	@Override
	protected void onPostExecute(BasicResult result) {

		synchronized (this) {
            if (requestListener != null) {
                if (apiKeyInvalidated){
                    requestListener.apiKeyInvalidated();
                }
                else{
                    Payload payload = new Payload(); // TODO PAYLOAD REFACTOR
                    payload.setResult(result.isSuccess());
                    payload.setResultResponse(result.getResultMessage());

                    // TODO oppia-577 remove
                    payload.setResultResponse("{\"courses\":[{\"resource_uri\":\"/api/v2/course/73/\",\"id\":73,\"version\":20160220235615,\"title\":{\"en\":\"Antenatal Care Part 2\"},\"description\":{\"en\":\"ANC HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"anc2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/73/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/76/\",\"id\":76,\"version\":20150221000028,\"title\":{\"en\":\"Communicable Diseases Part 1\"},\"description\":{\"en\":\"CD HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"cd1-all\",\"priority\":0,\"is_draft\":true,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/76/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/59/\",\"id\":59,\"version\":20150218143333,\"title\":{\"en\":\"Communicable Diseases Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"cd2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/59/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/60/\",\"id\":60,\"version\":20150218154124,\"title\":{\"en\":\"Communicable Diseases Part 3 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in Ethiopia\"},\"shortname\":\"cd3-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/60/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/78/\",\"id\":78,\"version\":20150221000542,\"title\":{\"en\":\"Communicable Diseases Part 3\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in all countries\"},\"shortname\":\"cd3-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/78/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/61/\",\"id\":61,\"version\":20150218161834,\"title\":{\"en\":\"Communicable Diseases Part 4 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in Ethiopia\"},\"shortname\":\"cd4-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/61/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/79/\",\"id\":79,\"version\":20190221001243,\"title\":{\"en\":\"Communicable Diseases Part 4\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in all countries\"},\"shortname\":\"cd4-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/79/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/57/\",\"id\":57,\"version\":20150218162046,\"title\":{\"en\":\"Family Planning - Ethiopia (Full)\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"fp-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/57/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/74/\",\"id\":74,\"version\":20150221001535,\"title\":{\"en\":\"Family Planning\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"fp-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/74/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/65/\",\"id\":65,\"version\":20150218163637,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/65/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/80/\",\"id\":80,\"version\":20150221124051,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heacm1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/80/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/66/\",\"id\":66,\"version\":20150218163819,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/66/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/81/\",\"id\":81,\"version\":20150221124256,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"heacm2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/81/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/69/\",\"id\":69,\"version\":20150218164216,\"title\":{\"en\":\"Health Management, Ethics and Research - Ethiopia (Full)\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"hmer-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/69/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/86/\",\"id\":86,\"version\":20150221124624,\"title\":{\"en\":\"Health Management, Ethics and Research\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"hmer-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/86/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/62/\",\"id\":62,\"version\":20150219124016,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heh1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/62/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/82/\",\"id\":82,\"version\":20150221124854,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heh1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/82/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/63/\",\"id\":63,\"version\":20150219125505,\"title\":{\"en\":\"Hygiene and Environmental Health Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heh2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/63/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":83,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/83/\",\"shortname\":\"heh2-all\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/83/download/\",\"username\":\"alex\",\"version\":20150221125250},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in Ethiopia\"},\"id\":42,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/42/\",\"shortname\":\"im-et\",\"title\":{\"en\":\"Immunization - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/42/download/\",\"username\":\"alex\",\"version\":20150219125724},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":50,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/50/\",\"shortname\":\"im-all\",\"title\":{\"en\":\"Immunization\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/50/download/\",\"username\":\"alex\",\"version\":20150221141604},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":45,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/45/\",\"shortname\":\"imnci1-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/45/download/\",\"username\":\"alex\",\"version\":20150219125811},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":47,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/47/\",\"shortname\":\"imnci1-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/47/download/\",\"username\":\"alex\",\"version\":20150221141640},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":44,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/44/\",\"shortname\":\"imnci2-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/44/download/\",\"username\":\"alex\",\"version\":20150219130627},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":49,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/49/\",\"shortname\":\"imnci2-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/49/download/\",\"username\":\"alex\",\"version\":20150221142445},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":38,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/38/\",\"shortname\":\"ldc-et\",\"title\":{\"en\":\"Labour and Delivery Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/38/download/\",\"username\":\"alex\",\"version\":20150219133612},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in all countries\"},\"id\":37,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/37/\",\"shortname\":\"ldc-all\",\"title\":{\"en\":\"Labour and Delivery Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/37/download/\",\"username\":\"alex\",\"version\":20150221151347},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":67,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/67/\",\"shortname\":\"ncd1-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/67/download/\",\"username\":\"alex\",\"version\":20150219134008},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":84,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/84/\",\"shortname\":\"ncd1-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/84/download/\",\"username\":\"alex\",\"version\":20150221151807},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":68,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/68/\",\"shortname\":\"ncd2-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/68/download/\",\"username\":\"alex\",\"version\":20150219134457},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":85,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/85/\",\"shortname\":\"ncd2-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/85/download/\",\"username\":\"alex\",\"version\":20150221152035},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in Ethiopia\"},\"id\":43,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/43/\",\"shortname\":\"nut-et\",\"title\":{\"en\":\"Nutrition - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/43/download/\",\"username\":\"alex\",\"version\":20150219140806},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":48,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/48/\",\"shortname\":\"nut-all\",\"title\":{\"en\":\"Nutrition\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/48/download/\",\"username\":\"alex\",\"version\":20150221152458},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":41,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/41/\",\"shortname\":\"pnc-et\",\"title\":{\"en\":\"Postnatal Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/41/download/\",\"username\":\"alex\",\"version\":20150219143153},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in all countries\"},\"id\":46,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/46/\",\"shortname\":\"pnc-all\",\"title\":{\"en\":\"Postnatal Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/46/download/\",\"username\":\"alex\",\"version\":20150221152551},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":122,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/122/\",\"shortname\":\"draft-test\",\"title\":{\"en\":\"Reference course 1 - reference\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/122/download/\",\"username\":\"alex\",\"version\":20210413132552}],\"meta\":{\"limit\":1000,\"next\":null,\"offset\":0,\"previous\":null,\"total_count\":37}}");

                    requestListener.apiRequestComplete(payload);
                }
            }
        }

        super.onPostExecute(result);
	}
	
	public void setAPIRequestListener(APIRequestListener srl) {
        synchronized (this) {
        	requestListener = srl;
        }
    }
}