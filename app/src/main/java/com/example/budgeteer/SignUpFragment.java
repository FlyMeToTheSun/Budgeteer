package com.example.budgeteer;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

public class SignUpFragment extends Fragment {

    private static final int SELECT_PICTURE = 1;
    private TextInputLayout layoutFirstName, layoutLastName, layoutEmail,layoutPassword;
    private TextInputEditText inputFirstName, inputLastName, inputEmail,inputPassword;

    private ImageButton profile_image_button;

    private String selectedImagePath;
    private Button btnSignup;

    private InputValidation inputValidation;
    private DatabaseHelper databaseHelper;
    private User user;

    private byte[] logoImage;

    private Bitmap selectedImageBitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment, container, false);

        layoutFirstName = v.findViewById(R.id.layout_signup_firstname);
        layoutLastName = v.findViewById(R.id.layout_signup_lastname);
        layoutEmail = v.findViewById(R.id.layout_login_email);
        layoutPassword = v.findViewById(R.id.layout_login_password);
        inputEmail = v.findViewById(R.id.login_email);
        inputPassword = v.findViewById(R.id.login_password);
        inputFirstName = v.findViewById(R.id.signup_firstname);
        inputLastName = v.findViewById(R.id.signup_lastname);
        btnSignup = v.findViewById(R.id.next);
        profile_image_button = v.findViewById(R.id.profile_image_button);

        selectedImageBitmap =  BitmapFactory.decodeResource(getContext().getResources(), R.drawable.getstarted2_profilee);


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postDataToSQLite();
                profile_image_button.setImageResource(R.drawable.getstarted2_profilee);
            }
        });

        profile_image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        SELECT_PICTURE);
            }
        });


        databaseHelper = new DatabaseHelper(getActivity());
        user = new User();

        return v;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                profile_image_button.setVisibility(View.VISIBLE);
                profile_image_button.setImageBitmap(selectedImageBitmap);


//
//                selectedImagePath = selectedImageUri.toString();
//
//                profile_image_button.setImageURI(selectedImageUri);
            }
        }
    }



    private void postDataToSQLite() {

        String firstname = inputFirstName.getText().toString();
        String lastname = inputLastName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Password field is required!");
            inputPassword.requestFocus();
        } else {
            clearError(4);
        }

        if (!isEmailValid(email)) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Invalid email address!");
            inputEmail.requestFocus();
        } else {
            clearError(3);
        }

        if (lastname.isEmpty()) {
            layoutLastName.setErrorEnabled(true);
            layoutLastName.setError("Last name is required!");
            inputLastName.requestFocus();
        }
        else {
            clearError(2);
        }

        if (firstname.isEmpty()) {
            layoutFirstName.setErrorEnabled(true);
            layoutFirstName.setError("First name is required!");
            inputFirstName.requestFocus();
        }
        else {
            clearError(1);
        }


        if (withoutErrors()) {

            try{
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                logoImage = stream.toByteArray();

                if (!databaseHelper.checkUser(email.trim())) {
                    user.setFirstName(firstname.trim());
                    user.setLastName(firstname.trim());
                    user.setEmail(email.trim());
                    user.setPassword(password.trim());
                    user.setImage(logoImage);
                    Log.d("mesa", "postDataToSQLite: " + logoImage);
                    databaseHelper.addUser(user);

                    emptyInputEditText();
                    showSuccessDialog();

                } else {

                    Toast.makeText(getActivity(), "Account Already Registered to Another User", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // Snack Bar to show error message that record already exists
                Toast.makeText(getActivity(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }

        }
    }
    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        inputEmail.setText(null);
        inputPassword.setText(null);
        inputFirstName.setText(null);
        inputLastName.setText(null);
    }

    private void clearError(int field) {
        if (field == 1) {
            layoutFirstName.setError(null);
            layoutFirstName.setErrorEnabled(false);
        } else if (field == 2) {
            layoutLastName.setError(null);
            layoutLastName.setErrorEnabled(false);
        } else if (field == 3) {
            layoutEmail.setError(null);
            layoutEmail.setErrorEnabled(false);
        } else {
            layoutPassword.setError(null);
            layoutPassword.setErrorEnabled(false);
        }
    }

    private boolean withoutErrors() {
        if (!layoutEmail.isErrorEnabled() && !layoutEmail.isErrorEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }

    private void showSuccessDialog(){
        //Toast.makeText(getActivity(), "Account Created", Toast.LENGTH_SHORT).show();

        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.prompt_account_created, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

}