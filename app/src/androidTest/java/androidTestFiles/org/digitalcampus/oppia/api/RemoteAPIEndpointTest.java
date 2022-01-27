package androidTestFiles.org.digitalcampus.oppia.api;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RemoteAPIEndpointTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void isServerVersionCompatibleTest(){

        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("b0.12.7"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("b0.12.5"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("0.12.5"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("12.5"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible(""));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("invalid"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible(null));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("-0.12.5"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("-"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("0.12.7b"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("0.a.7"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("A.12"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("0.12.5.0"));
        assertEquals(false, RemoteApiEndpoint.isServerVersionCompatible("v"));

        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.12.6.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.12.6"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.12.7.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.12.7"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("1.0.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.13.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("0.12.7-testsomething"));

        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.12.6.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.12.6"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.12.7.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.12.7"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v1.0.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.13.0"));
        assertEquals(true, RemoteApiEndpoint.isServerVersionCompatible("v0.12.7-testsomething"));
    }
}
