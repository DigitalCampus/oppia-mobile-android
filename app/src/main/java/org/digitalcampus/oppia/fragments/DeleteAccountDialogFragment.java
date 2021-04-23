package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.DeleteAccountTask;
import org.digitalcampus.oppia.task.Payload;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteAccountDialogFragment extends DialogFragment implements APIRequestListener {

    public interface DeleteAccountListener{
        void onDeleteAccountSuccess();
    }


    private TextInputLayout passwordInput;
    private DeleteAccountListener listener;

    public static DeleteAccountDialogFragment newInstance() {
        return new DeleteAccountDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_delete_account, container);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listener = (DeleteAccountListener) getActivity();
        getDialog().setTitle(R.string.privacy_delete_account_label);

        passwordInput = view.findViewById(R.id.input_layout_password);
        // Show soft keyboard automatically and request focus to field
        passwordInput.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setCancelable(false);

        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        btnDelete.setOnClickListener(v -> {
            String password = passwordInput.getEditText().getText().toString();
            if (TextUtils.isEmpty(password)){
                passwordInput.setErrorEnabled(true);
                passwordInput.setError(getText(R.string.field_required));
                return;
            }

            DeleteAccountTask task = new DeleteAccountTask(getContext());
            task.setAPIRequestListener(this);
            task.execute(password);
        });
        btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void apiRequestComplete(Payload response) {
        if (response.isResult()){
            Toast.makeText(getActivity(), response.getResultResponse(), Toast.LENGTH_LONG).show();
            listener.onDeleteAccountSuccess();
            dismiss();
        }
        else{
            passwordInput.setErrorEnabled(true);
            passwordInput.setError(getText(R.string.error_register_password_no_match));
        }
    }

    @Override
    public void apiKeyInvalidated() {
        dismiss();
        ((AppActivity) getActivity()).apiKeyInvalidated();
    }
}