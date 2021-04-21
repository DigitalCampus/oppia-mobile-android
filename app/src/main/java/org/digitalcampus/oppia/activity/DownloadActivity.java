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

package org.digitalcampus.oppia.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.adapter.DownloadCoursesAdapter;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.MultiChoiceHelper;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class DownloadActivity extends AppActivity implements APIRequestListener, CourseInstallerListener {

    public static final String EXTRA_TAG = "extra_tag";
    public static final String EXTRA_COURSE = "extra_course";
    public static final String EXTRA_MODE = "extra_mode";

    public static final int MODE_TAG_COURSES = 0;
    public static final int MODE_COURSE_TO_UPDATE = 1;
    public static final int MODE_NEW_COURSES = 2;

    // TODO oppia-577 remove
    static String coursename = "anc2-all";
    public static final String MOCK_COURSES_RESPONSE = "{\"courses\":[{\"resource_uri\":\"/api/v2/course/73/\",\"id\":73,\"version\":20220220235615,\"title\":{\"en\":\"Antenatal Care Part 2\"},\"description\":{\"en\":\"ANC HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"" + coursename + "\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/73/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/77/\",\"id\":77,\"version\":20190221000211,\"title\":{\"en\":\"Communicable Diseases Part 2\"},\"description\":{\"en\":\"CD HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"cd2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/77/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/60/\",\"id\":60,\"version\":20150218154124,\"title\":{\"en\":\"Communicable Diseases Part 3 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in Ethiopia\"},\"shortname\":\"cd3-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/60/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/78/\",\"id\":78,\"version\":20150221000542,\"title\":{\"en\":\"Communicable Diseases Part 3\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in all countries\"},\"shortname\":\"cd3-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/78/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/61/\",\"id\":61,\"version\":20210218161834,\"title\":{\"en\":\"Communicable Diseases Part 4 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in Ethiopia\"},\"shortname\":\"cd4-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/61/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/79/\",\"id\":79,\"version\":20220221001243,\"title\":{\"en\":\"Communicable Diseases Part 4\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in all countries\"},\"shortname\":\"cd4-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/79/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/57/\",\"id\":57,\"version\":20150218162046,\"title\":{\"en\":\"Family Planning - Ethiopia (Full)\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"fp-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/57/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/74/\",\"id\":74,\"version\":20150221001535,\"title\":{\"en\":\"Family Planning\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"fp-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/74/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/65/\",\"id\":65,\"version\":20150218163637,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/65/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/80/\",\"id\":80,\"version\":20150221124051,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heacm1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/80/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/66/\",\"id\":66,\"version\":20150218163819,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/66/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/81/\",\"id\":81,\"version\":20150221124256,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"heacm2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/81/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/69/\",\"id\":69,\"version\":20150218164216,\"title\":{\"en\":\"Health Management, Ethics and Research - Ethiopia (Full)\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"hmer-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/69/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/86/\",\"id\":86,\"version\":20150221124624,\"title\":{\"en\":\"Health Management, Ethics and Research\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"hmer-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/86/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/62/\",\"id\":62,\"version\":20150219124016,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heh1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/62/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":82,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/82/\",\"shortname\":\"heh1-all\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/82/download/\",\"username\":\"alex\",\"version\":20150221124854},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":63,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/63/\",\"shortname\":\"heh2-et\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/63/download/\",\"username\":\"alex\",\"version\":20150219125505},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":83,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/83/\",\"shortname\":\"heh2-all\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/83/download/\",\"username\":\"alex\",\"version\":20150221125250},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in Ethiopia\"},\"id\":42,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/42/\",\"shortname\":\"im-et\",\"title\":{\"en\":\"Immunization - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/42/download/\",\"username\":\"alex\",\"version\":20150219125724},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":50,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/50/\",\"shortname\":\"im-all\",\"title\":{\"en\":\"Immunization\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/50/download/\",\"username\":\"alex\",\"version\":20150221141604},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":45,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/45/\",\"shortname\":\"imnci1-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/45/download/\",\"username\":\"alex\",\"version\":20150219125811},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":47,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/47/\",\"shortname\":\"imnci1-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/47/download/\",\"username\":\"alex\",\"version\":20150221141640},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":44,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/44/\",\"shortname\":\"imnci2-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/44/download/\",\"username\":\"alex\",\"version\":20150219130627},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":49,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/49/\",\"shortname\":\"imnci2-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/49/download/\",\"username\":\"alex\",\"version\":20150221142445},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":38,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/38/\",\"shortname\":\"ldc-et\",\"title\":{\"en\":\"Labour and Delivery Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/38/download/\",\"username\":\"alex\",\"version\":20150219133612},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in all countries\"},\"id\":37,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/37/\",\"shortname\":\"ldc-all\",\"title\":{\"en\":\"Labour and Delivery Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/37/download/\",\"username\":\"alex\",\"version\":20150221151347},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":67,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/67/\",\"shortname\":\"ncd1-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/67/download/\",\"username\":\"alex\",\"version\":20150219134008},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":84,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/84/\",\"shortname\":\"ncd1-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/84/download/\",\"username\":\"alex\",\"version\":20150221151807},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":68,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/68/\",\"shortname\":\"ncd2-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/68/download/\",\"username\":\"alex\",\"version\":20150219134457},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":85,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/85/\",\"shortname\":\"ncd2-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/85/download/\",\"username\":\"alex\",\"version\":20150221152035},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in Ethiopia\"},\"id\":43,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/43/\",\"shortname\":\"nut-et\",\"title\":{\"en\":\"Nutrition - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/43/download/\",\"username\":\"alex\",\"version\":20150219140806},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":48,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/48/\",\"shortname\":\"nut-all\",\"title\":{\"en\":\"Nutrition\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/48/download/\",\"username\":\"alex\",\"version\":20150221152458},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":41,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/41/\",\"shortname\":\"pnc-et\",\"title\":{\"en\":\"Postnatal Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/41/download/\",\"username\":\"alex\",\"version\":20150219143153},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in all countries\"},\"id\":46,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/46/\",\"shortname\":\"pnc-all\",\"title\":{\"en\":\"Postnatal Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/46/download/\",\"username\":\"alex\",\"version\":20150221152551},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":123,\"is_draft\":true,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/123/\",\"shortname\":\"draft-test-draft\",\"title\":{\"en\":\"Reference course 1 - draft\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/123/download/\",\"username\":\"alex\",\"version\":20210413131911},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":122,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/122/\",\"shortname\":\"draft-test\",\"title\":{\"en\":\"Reference course 1 - reference\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/122/download/\",\"username\":\"alex\",\"version\":20210413132552}],\"meta\":{\"limit\":1000,\"next\":null,\"offset\":0,\"previous\":null,\"total_count\":38}}";
//    public static final String MOCK_COURSES_RESPONSE = "{\"courses\":[{\"resource_uri\":\"/api/v2/course/73/\",\"id\":73,\"version\":20190220235615,\"title\":{\"en\":\"Antenatal Care Part 2\"},\"description\":{\"en\":\"ANC HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"anc2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/73/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/77/\",\"id\":77,\"version\":20190221000211,\"title\":{\"en\":\"Communicable Diseases Part 2\"},\"description\":{\"en\":\"CD HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"cd2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/77/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/60/\",\"id\":60,\"version\":20150218154124,\"title\":{\"en\":\"Communicable Diseases Part 3 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in Ethiopia\"},\"shortname\":\"cd3-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/60/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/78/\",\"id\":78,\"version\":20150221000542,\"title\":{\"en\":\"Communicable Diseases Part 3\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in all countries\"},\"shortname\":\"cd3-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/78/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/57/\",\"id\":57,\"version\":20150218162046,\"title\":{\"en\":\"Family Planning - Ethiopia (Full)\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"fp-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/57/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/74/\",\"id\":74,\"version\":20150221001535,\"title\":{\"en\":\"Family Planning\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"fp-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/74/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/65/\",\"id\":65,\"version\":20150218163637,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/65/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/80/\",\"id\":80,\"version\":20150221124051,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heacm1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/80/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/66/\",\"id\":66,\"version\":20150218163819,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/66/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/81/\",\"id\":81,\"version\":20150221124256,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"heacm2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/81/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/69/\",\"id\":69,\"version\":20150218164216,\"title\":{\"en\":\"Health Management, Ethics and Research - Ethiopia (Full)\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"hmer-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/69/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/86/\",\"id\":86,\"version\":20150221124624,\"title\":{\"en\":\"Health Management, Ethics and Research\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"hmer-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/86/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/62/\",\"id\":62,\"version\":20150219124016,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heh1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/62/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/82/\",\"id\":82,\"version\":20150221124854,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heh1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/82/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/63/\",\"id\":63,\"version\":20150219125505,\"title\":{\"en\":\"Hygiene and Environmental Health Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heh2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/63/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":83,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/83/\",\"shortname\":\"heh2-all\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/83/download/\",\"username\":\"alex\",\"version\":20150221125250},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in Ethiopia\"},\"id\":42,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/42/\",\"shortname\":\"im-et\",\"title\":{\"en\":\"Immunization - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/42/download/\",\"username\":\"alex\",\"version\":20150219125724},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":50,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/50/\",\"shortname\":\"im-all\",\"title\":{\"en\":\"Immunization\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/50/download/\",\"username\":\"alex\",\"version\":20150221141604},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":45,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/45/\",\"shortname\":\"imnci1-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/45/download/\",\"username\":\"alex\",\"version\":20150219125811},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":47,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/47/\",\"shortname\":\"imnci1-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/47/download/\",\"username\":\"alex\",\"version\":20150221141640},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":44,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/44/\",\"shortname\":\"imnci2-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/44/download/\",\"username\":\"alex\",\"version\":20150219130627},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":49,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/49/\",\"shortname\":\"imnci2-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/49/download/\",\"username\":\"alex\",\"version\":20150221142445},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":38,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/38/\",\"shortname\":\"ldc-et\",\"title\":{\"en\":\"Labour and Delivery Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/38/download/\",\"username\":\"alex\",\"version\":20150219133612},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in all countries\"},\"id\":37,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/37/\",\"shortname\":\"ldc-all\",\"title\":{\"en\":\"Labour and Delivery Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/37/download/\",\"username\":\"alex\",\"version\":20150221151347},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":67,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/67/\",\"shortname\":\"ncd1-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/67/download/\",\"username\":\"alex\",\"version\":20150219134008},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":84,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/84/\",\"shortname\":\"ncd1-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/84/download/\",\"username\":\"alex\",\"version\":20150221151807},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":68,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/68/\",\"shortname\":\"ncd2-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/68/download/\",\"username\":\"alex\",\"version\":20150219134457},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":85,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/85/\",\"shortname\":\"ncd2-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/85/download/\",\"username\":\"alex\",\"version\":20150221152035},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in Ethiopia\"},\"id\":43,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/43/\",\"shortname\":\"nut-et\",\"title\":{\"en\":\"Nutrition - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/43/download/\",\"username\":\"alex\",\"version\":20150219140806},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":48,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/48/\",\"shortname\":\"nut-all\",\"title\":{\"en\":\"Nutrition\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/48/download/\",\"username\":\"alex\",\"version\":20150221152458},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":41,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/41/\",\"shortname\":\"pnc-et\",\"title\":{\"en\":\"Postnatal Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/41/download/\",\"username\":\"alex\",\"version\":20150219143153},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in all countries\"},\"id\":46,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/46/\",\"shortname\":\"pnc-all\",\"title\":{\"en\":\"Postnatal Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/46/download/\",\"username\":\"alex\",\"version\":20150221152551},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":123,\"is_draft\":true,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/123/\",\"shortname\":\"draft-test-draft\",\"title\":{\"en\":\"Reference course 1 - draft\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/123/download/\",\"username\":\"alex\",\"version\":20210413131911},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":122,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/122/\",\"shortname\":\"draft-test\",\"title\":{\"en\":\"Reference course 1 - reference\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/122/download/\",\"username\":\"alex\",\"version\":20210413132552}],\"meta\":{\"limit\":1000,\"next\":null,\"offset\":0,\"previous\":null,\"total_count\":38}}";
    // ----

    private JSONObject json;
    private String url;
    private ArrayList<CourseInstallViewAdapter> courses;
    private ArrayList<CourseInstallViewAdapter> selected;

    private Button downloadButton;

    private InstallerBroadcastReceiver receiver;

    @Inject
    CourseInstallRepository courseInstallRepository;
    @Inject
    CourseInstallerServiceDelegate courseInstallerServiceDelegate;
    @Inject
    CoursesRepository coursesRepository;
    private DownloadCoursesAdapter coursesAdapter;
    private MultiChoiceHelper multiChoiceHelper;
    private Course courseToUpdate;
    private int mode;

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getAppComponent().inject(this);

        downloadButton = findViewById(R.id.btn_download_courses);

        Bundle bundle = this.getIntent().getExtras();
        if (!bundle.containsKey(EXTRA_MODE)) {
            throw new IllegalArgumentException("Mode parameter not found" + EXTRA_MODE);
        }

        mode = bundle.getInt(EXTRA_MODE);

        setUpRecyclerView();

        setUpScreen(mode, bundle);
    }

    private void setUpScreen(int mode, Bundle bundle) {

        switch (mode) {
            case MODE_TAG_COURSES:
                if (!bundle.containsKey(EXTRA_TAG)) {
                    throw new IllegalArgumentException("Tag parameter not found");
                }

                Tag tag = (Tag) bundle.getSerializable(EXTRA_TAG);
                this.url = Paths.SERVER_TAG_PATH + tag.getId() + File.separator;
                TextView tagTitle = findViewById(R.id.category_title);
                tagTitle.setVisibility(View.VISIBLE);
                tagTitle.setText(tag.getName());
                break;

            case MODE_COURSE_TO_UPDATE:

                if (!bundle.containsKey(EXTRA_COURSE)) {
                    throw new IllegalArgumentException("Course parameter not found");
                }

                setTitle(R.string.course_updates);
                findViewById(R.id.action_bar_subtitle).setVisibility(View.GONE);

                courseToUpdate = (Course) bundle.getSerializable(EXTRA_COURSE);
                this.url = Paths.SERVER_COURSES_PATH;
                break;

            case MODE_NEW_COURSES:

                setTitle(R.string.new_courses);
                findViewById(R.id.action_bar_subtitle).setVisibility(View.GONE);

                this.url = Paths.SERVER_COURSES_PATH;

                break;
        }
    }

    private void setUpRecyclerView() {

        courses = new ArrayList<>();
        selected = new ArrayList<>();
        coursesAdapter = new DownloadCoursesAdapter(this, courses);
        multiChoiceHelper = new MultiChoiceHelper(this, coursesAdapter);
        multiChoiceHelper.setMultiChoiceModeListener(new MultiChoiceHelper.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(androidx.appcompat.view.ActionMode mode, int position, long id, boolean checked) {
                Log.v(TAG, "Count: " + multiChoiceHelper.getCheckedItemCount());
                CourseInstallViewAdapter course = courses.get(position);
                if (checked) {
                    if (!course.isToInstall()) {
                        multiChoiceHelper.setItemChecked(position, false, true);
                        return;
                    }
                    selected.add(course);
                } else {
                    selected.remove(course);
                }

                int count = selected.size();
                mode.setSubtitle(count == 1 ? count + " item selected" : count + " items selected");
            }

            @Override
            public boolean onCreateActionMode(final androidx.appcompat.view.ActionMode mode, Menu menu) {

                onPrepareOptionsMenu(menu);
                downloadButton.setOnClickListener(view -> {
                    for (CourseInstallViewAdapter course : selected) {
                        downloadCourse(course);
                    }
                    mode.finish();
                });
                mode.setTitle(R.string.title_download_activity);
                coursesAdapter.setEnterOnMultiChoiceMode(true);
                coursesAdapter.notifyDataSetChanged();
                showDownloadButton(true);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_select_all:
                        selectAllInstallableCourses();
                        return true;
                    case R.id.menu_unselect_all:
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode) {
                selected.clear();
                showDownloadButton(false);
                multiChoiceHelper.clearChoices();
                coursesAdapter.setEnterOnMultiChoiceMode(false);
                coursesAdapter.notifyDataSetChanged();

            }
        });

        coursesAdapter.setOnItemClickListener((view, position) -> {
            Log.d("course-download", "Clicked " + position);
            CourseInstallViewAdapter course = courses.get(position);
            // When installing, don't do anything on click
            if (course.isInstalling()) return;
            if (course.isDownloading()) {
                cancelCourseTask(course);
            } else if (course.isToInstall()) {
                downloadCourse(course);
            }

        });

        coursesAdapter.setMultiChoiceHelper(multiChoiceHelper);
        RecyclerView recyclerCourses = findViewById(R.id.recycler_tags);
        if (recyclerCourses != null) {
            recyclerCourses.setAdapter(coursesAdapter);
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseInstallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);

        if (json == null) {
            // The JSON download task has not started or been completed yet
            getCourseList();
        } else if ((courses != null) && !courses.isEmpty()) {
            // We already have loaded JSON and courses (coming from orientationchange)
            coursesAdapter.notifyDataSetChanged();
        } else {
            // The JSON is downloaded but course list is not
            refreshCourseList();
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        hideProgressDialog();
        unregisterReceiver(receiver);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mode = savedInstanceState.getInt("mode");

        try {
            this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<CourseInstallViewAdapter> savedCourses = (ArrayList<CourseInstallViewAdapter>) savedInstanceState.getSerializable("courses");
            if (savedCourses != null) this.courses.addAll(savedCourses);
        } catch (Exception e) {
            // error in the json so just get the list again
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("mode", mode);

        if (json != null) {
            // Only save the instance if the request has been proccessed already
            savedInstanceState.putString("json", json.toString());
            savedInstanceState.putSerializable("courses", courses);
        }
    }

    private void getCourseList() {
        showProgressDialog(getString(R.string.loading));
        courseInstallRepository.getCourseList(this, url);
    }

    private void showDownloadButton(boolean show) {
        downloadButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void downloadCourse(CourseInstallViewAdapter course) {
        if (course.isToInstall() && !course.isInProgress()) {
            Intent serviceIntent = new Intent(DownloadActivity.this, CourseInstallerService.class);
            courseInstallerServiceDelegate.installCourse(DownloadActivity.this, serviceIntent, course);
            resetCourseProgress(course, true, false);
        }
    }

    private void cancelCourseTask(CourseInstallViewAdapter course) {
        Intent serviceIntent = new Intent(DownloadActivity.this, CourseInstallerService.class);
        courseInstallerServiceDelegate.cancelCourseInstall(DownloadActivity.this, serviceIntent, course);
        resetCourseProgress(course, false, false);
    }

    public void refreshCourseList() {
        // process the response and display on screen in listview
        // Create an array of courses, that will be put to our ListActivity
        try {
            String storage = Storage.getStorageLocationRoot(this);
            courses.clear();

            // TODO 'refreshCourseList' method should be refactorized
            courseInstallRepository.refreshCourseList(this, courses, json, storage, mode == MODE_COURSE_TO_UPDATE);
            if (mode == MODE_COURSE_TO_UPDATE) {
                filterOnlyInstalledCourses();
            } else if (mode == MODE_NEW_COURSES) {
                filterNewCoursesNotSeen();
            }
            coursesAdapter.notifyDataSetChanged();
            findViewById(R.id.empty_state).setVisibility((courses.isEmpty()) ? View.VISIBLE : View.GONE);

        } catch (Exception e) {
            Mint.logException(e);
            Log.d(TAG, "Error processing response: ", e);
            UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
        }
    }

    private void filterNewCoursesNotSeen() {

        final long lastNewCourseTimestamp = getPrefs().getLong(PrefsActivity.PREF_LAST_NEW_COURSE_TIMESTAMP, 0);
        long newlastNewCourseTimestamp = lastNewCourseTimestamp;

        List<Course> installedCourses = coursesRepository.getCourses(this);

        Iterator<CourseInstallViewAdapter> iter = courses.iterator();
        while (iter.hasNext()) {
            CourseInstallViewAdapter courseAdapter = iter.next();

            boolean newCourseSeen = courseAdapter.getVersionId() < lastNewCourseTimestamp;
            if (isInstalled(courseAdapter, installedCourses) || newCourseSeen) {
                iter.remove();
            }

            newlastNewCourseTimestamp = (long) Math.max(lastNewCourseTimestamp, courseAdapter.getVersionId());

        }


        getPrefs().edit().putLong(PrefsActivity.PREF_LAST_NEW_COURSE_TIMESTAMP, newlastNewCourseTimestamp).commit();
    }


    private void filterOnlyInstalledCourses() {
        List<Course> installedCourses = coursesRepository.getCourses(this);

        Iterator<CourseInstallViewAdapter> iter = courses.iterator();
        while (iter.hasNext()) {
            CourseInstallViewAdapter courseAdapter = iter.next();
            if (!isInstalled(courseAdapter, installedCourses)) {
                iter.remove();
            }
        }

    }

    private boolean isInstalled(CourseInstallViewAdapter courseAdapter, List<Course> installedCourses) {
        for (Course course : installedCourses) {
            if (TextUtils.equals(course.getShortname(), courseAdapter.getShortname())) {
                return true;
            }
        }
        return false;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.missing_media_sortby, menu);
        MenuItem sortBy = menu.findItem(R.id.menu_sort_by);
        if (sortBy != null) {
            sortBy.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_select_all:
                selectAllInstallableCourses();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void selectAllInstallableCourses() {
        for (int i = 0; i < coursesAdapter.getItemCount(); i++) {
            CourseInstallViewAdapter course = courses.get(i);
            if (course.isToInstall() && !multiChoiceHelper.isItemChecked(i)) {
                multiChoiceHelper.setItemChecked(i, true, true);
            }
        }
    }

    public void apiRequestComplete(Payload response) {
        hideProgressDialog();

        Callable<Boolean> finishActivity = () -> {
            DownloadActivity.this.finish();
            return true;
        };

        if (response.isResult()) {
            try {

                // TODO oppia-577 remove
                if (mode != MODE_TAG_COURSES) {
                    response.setResultResponse(MOCK_COURSES_RESPONSE);
                }

                json = new JSONObject(response.getResultResponse());
                refreshCourseList();

                if (courseToUpdate != null) {
                    findCourseAndDownload(courseToUpdate);
                }

            } catch (JSONException e) {
                Mint.logException(e);
                Log.d(TAG, "Error connecting to server: ", e);
                UIUtils.showAlert(this, R.string.loading, R.string.error_connection, finishActivity);
            }
        } else {
            String errorMsg = response.getResultResponse();
            UIUtils.showAlert(this, R.string.error, errorMsg, finishActivity);
        }
    }

    private void findCourseAndDownload(Course courseToUpdate) {
        for (CourseInstallViewAdapter course : courses) {
            if (TextUtils.equals(course.getShortname(), courseToUpdate.getShortname())) {
                downloadCourse(course);
            }
        }
    }

    //@Override
    public void onDownloadProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            course.setDownloading(true);
            course.setInstalling(false);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallProgress(String fileUrl, int progress) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            course.setDownloading(false);
            course.setInstalling(true);
            course.setProgress(progress);
            coursesAdapter.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallFailed(String fileUrl, String message) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            resetCourseProgress(course, false, false);
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseInstallViewAdapter course = findCourse(fileUrl);
        if (course != null) {
            Toast.makeText(this, this.getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            course.setToUpdate(false);
            resetCourseProgress(course, false, false);
        }
    }

    private CourseInstallViewAdapter findCourse(String fileUrl) {
        if (!courses.isEmpty()) {
            for (CourseInstallViewAdapter course : courses) {
                if (course.getDownloadUrl().equals(fileUrl)) {
                    return course;
                }
            }
        }
        return null;
    }

    protected void resetCourseProgress(CourseInstallViewAdapter courseSelected,
                                       boolean downloading, boolean installing) {

        courseSelected.setDownloading(downloading);
        courseSelected.setInstalling(installing);
        courseSelected.setProgress(0);
        coursesAdapter.notifyDataSetChanged();
    }


}
