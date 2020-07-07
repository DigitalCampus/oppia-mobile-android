/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;

public class PasswordDialogFragment extends DialogFragment {

    private AdminSecurityManager.AuthListener listener;

    /**      
     * @deprecated
     */
    @Override
    @Deprecated
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle);
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_password, null))
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    /**      
     * @deprecated
     */
    @Override
    @Deprecated
    public void onStart() {
        super.onStart();
        final AlertDialog d = (AlertDialog)getDialog();
        if(d != null) {
            Button positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                EditText passwordField = d.findViewById(R.id.admin_password_field);
                View errorMessage = d.findViewById(R.id.admin_password_error);
                String password = passwordField.getText().toString();

                //If the user leave the input blank, we don't perform any action
                if (password.equals("")) return;

                boolean passCorrect = AdminSecurityManager.with(getActivity()).checkAdminPassword(password);
                if(passCorrect) {
                    d.dismiss();
                    if (listener != null) {
                        listener.onPermissionGranted();
                    }
                }
                else{
                    errorMessage.setVisibility(View.VISIBLE);
                    SimpleAnimator.fade(errorMessage, SimpleAnimator.FADE_IN);
                    passwordField.setText("");
                }
            });
        }
    }

    public void setListener(AdminSecurityManager.AuthListener listener){
        this.listener = listener;
    }
}
