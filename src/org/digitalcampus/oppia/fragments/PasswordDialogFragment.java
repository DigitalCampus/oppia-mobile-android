package org.digitalcampus.oppia.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;

public class PasswordDialogFragment extends DialogFragment {

        public interface PasswordDialogListener{
            void onPasswordSuccess();
        }

        private PasswordDialogListener listener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_password, null))
                    .setPositiveButton(R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

    @Override
    public void onStart()
    {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    EditText passwordField = (EditText) d.findViewById(R.id.admin_password_field);
                    View errorMessage = d.findViewById(R.id.admin_password_error);
                    String password = passwordField.getText().toString();

                    if (password.equals("")) return;
                    Boolean passwordCorrect = true;
                    //TODO: check password...
                    if(passwordCorrect) {
                        d.dismiss();
                        if (listener != null) {
                            listener.onPasswordSuccess();
                        }
                    }
                    else{
                        errorMessage.setVisibility(View.VISIBLE);
                        SimpleAnimator.fade(errorMessage, SimpleAnimator.FADE_IN);
                        passwordField.setText("");
                    }
                }
            });
        }
    }

    public void setListener(PasswordDialogListener listener){
        this.listener = listener;
    }
}
