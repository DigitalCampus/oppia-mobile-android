package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UserModelTest {


    @Test
    public void getAndSetTest(){
        User u = new User();
        u.setUserId(123);
        assertEquals(123, u.getUserId());

        CustomValue cv = new CustomValue("myCV");
        u.putCustomField("cv", cv);
        assertEquals(cv, u.getCustomField("cv"));
        assertEquals(null, u.getCustomField("noval"));

    }

    @Test
    public void getPasswordEncryptedTest(){

        User u = new User();
        u.setPasswordEncrypted(null);
        u.setPassword("123456");
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92", u.getPasswordEncrypted());

        u.setPasswordEncrypted("encrypted");
        assertEquals("encrypted", u.getPasswordEncrypted());
    }

    @Test
    public void getPasswordHashedTest(){

        User u = new User();
        u.setPassword("123456");
        assertEquals("sha1$$7c4a8d09ca3762af61e59520943dc26494f8941b", u.getPasswordHashed());

        u.setPassword(null);
        assertEquals("", u.getPasswordHashed());

    }
}
