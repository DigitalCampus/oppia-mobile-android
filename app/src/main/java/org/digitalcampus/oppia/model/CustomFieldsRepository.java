package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;

import java.util.List;

public class CustomFieldsRepository {

    public List<CustomField> getAll(Context ctx){
        return DbHelper.getInstance(ctx).getCustomFields();
    }
}
