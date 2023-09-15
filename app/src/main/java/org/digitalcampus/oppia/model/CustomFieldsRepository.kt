package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.CustomField.RegisterFormStep
import org.digitalcampus.oppia.utils.storage.StorageUtils

class CustomFieldsRepository {
    fun getAll(ctx: Context?): List<CustomField> {
        return DbHelper.getInstance(ctx).customFields
    }

    fun getRegisterSteps(ctx: Context): List<RegisterFormStep> {
        val customFieldsData = StorageUtils.readFileFromAssets(ctx, CustomField.CUSTOMFIELDS_FILE)
        return CustomField.parseRegisterSteps(customFieldsData)
    }

    fun getRequiredFields(ctx: Context): List<String> {
        val customFieldsData = StorageUtils.readFileFromAssets(ctx, CustomField.CUSTOMFIELDS_FILE)
        return CustomField.getRequiredFields(customFieldsData)
    }
}