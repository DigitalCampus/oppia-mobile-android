package org.digitalcampus.oppia.utils.ui.fields;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.badoualy.stepperindicator.StepperIndicator;

import org.digitalcampus.oppia.model.CustomField;

import java.util.List;

public class SteppedFormUIManager {

    private StepperIndicator stepIndicator;
    private CustomFieldsUIManager fieldsManager;
    private List<CustomField.RegisterFormStep> steps;

    private CustomField.RegisterFormStep currentStep;
    private int currentStepNum;
    private int numSteps;

    private TextView stepDescription;
    private ViewGroup customFieldsContainer;
    private ViewGroup steppedFieldsContainer;

    public SteppedFormUIManager(StepperIndicator indicator, List<CustomField.RegisterFormStep> steps, CustomFieldsUIManager fieldsManager){
        this.stepIndicator = indicator;
        this.fieldsManager = fieldsManager;
        this.steps = steps;

        numSteps = steps.get(steps.size() - 1).getOrder();
    }

    public void initialize(ViewGroup customFieldsContainer, ViewGroup steppedFieldsContainer, TextView stepDescriptionTextView){
        currentStepNum = 0;
        stepIndicator.setStepCount(numSteps);
        stepDescription = stepDescriptionTextView;
        this.customFieldsContainer = customFieldsContainer;
        this.steppedFieldsContainer = steppedFieldsContainer;
        customFieldsContainer.setVisibility(View.GONE);
        loadStep(true);
    }

    private CustomField.RegisterFormStep getStepWithOrder(int order){
        for (CustomField.RegisterFormStep step : steps){
            if (step.getOrder() == order){
                if (TextUtils.isEmpty(step.getConditionalByField())){
                    return step;
                }

                if (fieldsManager.isConditionMet(step.getConditionalByField(), step.getConditionalByValue())){
                    return step;
                }
            }
        }

        // We didn't find a match for the current step number...
        return null;
    }

    public boolean validate(){
        boolean valid = true;
        for (String field : currentStep.getFields()){
            valid = fieldsManager.getInputByKey(field).validate() && valid;
        }
        return valid;

    }

    public boolean nextStep(){
        if (validate()){
            currentStepNum++;
            loadStep(true);
            return true;
        }
        return false;
    }


    private void loadStep(boolean searchForward){

        CustomField.RegisterFormStep step = null;
        while ((step == null) && (currentStepNum <= numSteps)){
            step = getStepWithOrder(currentStepNum);
            if (step == null){
                currentStepNum += searchForward ? 1 : -1;
            }
        }
        currentStep = step;
        if (step == null){
            // Something went wrong, we don't have more steps...
            return;
        }
        fieldsManager.moveAllFieldsTo(customFieldsContainer);
        for (String field : currentStep.getFields()){
            fieldsManager.setVisibleInView(field, steppedFieldsContainer);
        }
        updateDescription();
        stepIndicator.setCurrentStep(Math.max(currentStepNum - 1, 0));
    }

    public boolean prevStep() {
        currentStepNum--;
        loadStep(false);
        return (currentStepNum <= 1);
    }

    private void updateDescription(){

        if (TextUtils.isEmpty(currentStep.getHelperText())) {
            stepDescription.setVisibility(View.GONE);
        }
        else {
            stepDescription.setVisibility(View.VISIBLE);
            stepDescription.setText(currentStep.getHelperText());
        }
    }
    public boolean isLastStep(){
        return currentStepNum == numSteps;
    }

}
