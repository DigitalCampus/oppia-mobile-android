package testFiles.features;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import testFiles.utils.BaseTest;
import testFiles.utils.UnitTestsFileUtils;

@RunWith(MockitoJUnitRunner.class)
public class ExternalResourceOpenerTest {

    private static final String PAGE_WITH_EXTERNAL_RESOURCES = BaseTest.PATH_PAGE_CONTENTS + "/page_with_pdf.html";
    private static final String PAGE_WITH_NO_EXTERNAL_RESOURCES = BaseTest.PATH_PAGE_CONTENTS + "/normal_page.html";

    @Test
    public void testPageWithNoExternalResourcesCorrect() throws Exception {
        String pageContents = UnitTestsFileUtils.readFileFromTestResources(PAGE_WITH_NO_EXTERNAL_RESOURCES);
        List<String> resources = ExternalResourceOpener.getResourcesFromContent(pageContents);
        assertThat(resources.size(), equalTo(0));
    }

    @Test
    public void testPageWithExternalResourceGetDetected() throws Exception {
        String pageContents = UnitTestsFileUtils.readFileFromTestResources(PAGE_WITH_EXTERNAL_RESOURCES);
        List<String> resources = ExternalResourceOpener.getResourcesFromContent(pageContents);
        assertThat(resources.size(), equalTo(1));
    }
}
