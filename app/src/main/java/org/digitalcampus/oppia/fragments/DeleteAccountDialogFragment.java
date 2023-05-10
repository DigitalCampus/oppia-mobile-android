package org.digitalcampus.oppia.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.DialogDeleteAccountBinding;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.DeleteAccountTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.TextUtilsJava;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

public class DeleteAccountDialogFragment extends DialogFragment implements APIRequestListener {

    private DialogDeleteAccountBinding binding;

    @Inject
    ApiEndpoint apiEndpoint;

    public interface DeleteAccountListener{
        void onDeleteAccountSuccess();
    }

    private DeleteAccountListener listener;

    public static DeleteAccountDialogFragment newInstance() {
        return new DeleteAccountDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DialogDeleteAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listener = (DeleteAccountListener) getActivity();
        getDialog().setTitle(R.string.privacy_delete_account_label);

        // Show soft keyboard automatically and request focus to field
        binding.inputLayoutPassword.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setCancelable(false);

        binding.btnDelete.setOnClickListener(v -> {
            String password = binding.inputLayoutPassword.getEditText().getText().toString();
            if (TextUtilsJava.isEmpty(password)){
                binding.inputLayoutPassword.setErrorEnabled(true);
                binding.inputLayoutPassword.setError(getText(R.string.field_required));
                return;
            }

            DeleteAccountTask task = new DeleteAccountTask(getContext(), apiEndpoint);
            task.setAPIRequestListener(this);
            task.execute(password);
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    @Override
    public void apiRequestComplete(BasicResult result) {
        if (result.isSuccess()){
            Toast.makeText(getActivity(), result.getResultMessage(), Toast.LENGTH_LONG).show();
            listener.onDeleteAccountSuccess();
            dismiss();
        }
        else{
            binding.inputLayoutPassword.setErrorEnabled(true);
            binding.inputLayoutPassword.setError(getText(R.string.error_register_password_no_match));
        }
    }

    @Override
    public void apiKeyInvalidated() {
        dismiss();
        ((AppActivity) getActivity()).apiKeyInvalidated();
    }
}