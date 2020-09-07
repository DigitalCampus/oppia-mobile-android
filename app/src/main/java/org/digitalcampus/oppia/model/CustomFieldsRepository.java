package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.util.List;

public class CustomFieldsRepository {

    public List<CustomField> getAll(Context ctx){
        return DbHelper.getInstance(ctx).getCustomFields();
    }

    public List<CustomField.RegisterFormStep> getRegisterSteps(Context ctx){
        String customFieldsData = StorageUtils.readFileFromAssets(ctx, CustomField.CUSTOMFIELDS_FILE);
        return CustomField.parseRegisterSteps(customFieldsData);
    }
}
