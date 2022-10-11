package androidTestFiles.org.digitalcampus.oppia.model;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

import androidTestFiles.utils.FileUtils;

@RunWith(AndroidJUnit4.class)
public class TagRepositoryTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String TAGS_ORIGINAL = "tags/tags_original.json";
    private static final String TAGS_NEW_DOWNLOADS_ALL_AVAILABLE = "tags/new_downloads_all_available.json";
    private static final String TAGS_NEW_DOWNLOADS_ONE_NON_AVAILABLE = "tags/new_downloads_one_non_available.json";

    private List<Tag> loadTagList(String json) throws Exception {
        return loadTagList(json, new ArrayList<>());
    }

    private List<Tag> loadTagList(String json, List<String> installedCoursesNames) throws Exception {

        String tagListJson = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(),
                json);

        TagRepository tr = new TagRepository();
        List<Tag> tagList = new ArrayList<>();

        JSONObject o = new JSONObject(tagListJson);
        tr.refreshTagList(tagList, o, installedCoursesNames);

        return tagList;
    }

    @Test
    public void loadTagsOriginalJson() throws Exception {

        // For retrocompatibility after "new download enable" feature added

        List<Tag> tagList = loadTagList(TAGS_ORIGINAL);

        assertEquals(9, tagList.size());

    }

    @Test
    public void loadTagsAllLive() throws Exception {

        List<Tag> tagList = loadTagList(TAGS_NEW_DOWNLOADS_ALL_AVAILABLE);

        assertEquals(2, tagList.size());

        assertEquals(2, tagList.get(0).getCount());
        assertEquals(2, tagList.get(0).getCountNewDownloadEnabled());
        assertEquals(2, tagList.get(0).getCountAvailable());

        assertEquals(3, tagList.get(1).getCount());
        assertEquals(2, tagList.get(1).getCountNewDownloadEnabled());
        assertEquals(2, tagList.get(1).getCountAvailable());

    }



    @Test
    public void loadTagsOneNonLive() throws Exception {

        List<Tag> tagList = loadTagList(TAGS_NEW_DOWNLOADS_ONE_NON_AVAILABLE);

        assertEquals(2, tagList.size());

    }

    @Test
    public void loadTagsOneNonLiveButCourseInstalled() throws Exception {

        List<String> installedCoursesNames = Arrays.asList("course-c");
        List<Tag> tagList = loadTagList(TAGS_NEW_DOWNLOADS_ONE_NON_AVAILABLE, installedCoursesNames);

        assertEquals(3, tagList.size());

        assertEquals(2, tagList.get(2).getCount());
        assertEquals(0, tagList.get(2).getCountNewDownloadEnabled());
        assertEquals(1, tagList.get(2).getCountAvailable());

    }
}
