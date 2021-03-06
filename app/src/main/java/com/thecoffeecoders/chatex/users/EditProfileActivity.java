package com.thecoffeecoders.chatex.users;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thecoffeecoders.chatex.MainActivity;
import com.thecoffeecoders.chatex.R;
import com.thecoffeecoders.chatex.auth.LoginActivity;
import com.thecoffeecoders.chatex.models.User;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class EditProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Bundle bundleExtras;

    final int RC_IMAGE_PICKER_PROFILE_PICTURE = 001;
    final int RC_IMAGE_PICKER_COVER_PICTURE = 002;

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    FirebaseStorage mStorage;

    //UI elements
    ImageView coverPictureImgView;
    CircularImageView profilePictureImgView;
    CircularImageView changeProfilePictureImgView;
    CircularImageView changeCoverPictureImgView;
    EditText nameEditText;
    EditText usernameEditText;
    EditText bioEditText;
    Spinner genderSpinner;
    //EditText emailEditText;
    EditText addressEditText;
    Button updateInfoBtn;

    ProgressBar mCoverPictureProgressBar;
    ProgressBar mProfilePictureProgressBar;
    User user;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Firebase Objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mStorage = FirebaseStorage.getInstance();

        user = new User();

        //Instantiate layout elements
        coverPictureImgView = findViewById(R.id.imgCoverPicture);
        profilePictureImgView = findViewById(R.id.imgProfilePicture);
        changeProfilePictureImgView = findViewById(R.id.imgChangeProfilePicture);
        changeCoverPictureImgView = findViewById(R.id.imgChangeCoverPicture);
        nameEditText = findViewById(R.id.et_name);
        usernameEditText = findViewById(R.id.et_username);
        bioEditText = findViewById(R.id.et_bio);
        mCoverPictureProgressBar = findViewById(R.id.edit_cover_picture_progress_bar);
        mProfilePictureProgressBar = findViewById(R.id.edit_profile_picture_progress_bar);

        //emailEditText = findViewById(R.id.et_email);
        addressEditText = findViewById(R.id.et_address);
        updateInfoBtn = findViewById(R.id.btnUpdateInfo);

        //setting up gender spinner
        genderSpinner = findViewById(R.id.spinnerGender);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        genderSpinner.setAdapter(adapter);
        genderSpinner.setOnItemSelectedListener(this);

        bundleExtras = getIntent().getExtras();
        new PopulateFormWithUserDataFromProvider().execute();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class PopulateFormWithPreExistingUserData extends  AsyncTask<Void, Void, User>{

        @Override
        protected User doInBackground(Void... voids) {


            return null;
        }
    }

    private class PopulateFormWithUserDataFromProvider extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            user.setId(mUser.getUid());

            if(bundleExtras != null){
                if(bundleExtras.getBoolean("isNew")){
                    if(bundleExtras.getString("provider").equals("firebase")){
                        if(mUser.getDisplayName() != null){
                            user.setDisplayName(mUser.getDisplayName());
                        }else if(mUser.getPhotoUrl() != null){
                            user.setProfilePicURI(mUser.getPhotoUrl().toString());
                        }
                        user.setEmail(mUser.getEmail());
                    }else if(bundleExtras.getString("provider").equals("google")|| bundleExtras.getString("provider").equals("facebook")){
                        for (UserInfo profile : mUser.getProviderData()) {
                            // Name, email address, and profile photo Url
                            String name = profile.getDisplayName();
                            user.setDisplayName(name);
                            String email = profile.getEmail();
                            user.setEmail(email);
                            Uri photoUrl;
                            if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                                String facebookUserId = profile.getUid();
                                photoUrl = Uri.parse("https://graph.facebook.com/" + facebookUserId + "/picture?height=500");
                                Log.d("bikalpa", "https://graph.facebook.com/" + facebookUserId + "/picture?height=500");
                            }else{
                                photoUrl = profile.getPhotoUrl();
                                photoUrl = Uri.parse(photoUrl.toString().replace("/s96-c/","/s500-c/"));
                            }
                            user.setProfilePicURI(photoUrl.toString());
                        }
                    }
                }else {//preexisting user
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/"+mUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                user = dataSnapshot.getValue(User.class);
                                Log.w("firebasedatabase", user.getUsername());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("firebasedatabase", "loadPost:onCancelled", databaseError.toException());
                        }
                    });
                }
            }

            //return user;
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPostExecute(param);

            Log.d("bikalpa", "user data downloaded");

            if(user.getProfilePicURI() != null){
                //Load Profile Picture
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_profile_picture_placeholder_female);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(user.getProfilePicURI())
                        .into(profilePictureImgView);
            }

            if(user.getCoverPictureURI() != null){
                //Load Profile Picture
                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_cover_photo_placeholder);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(user.getCoverPictureURI())
                        .into(coverPictureImgView);
            }

            if(user.getDisplayName() != null){
                nameEditText.setText(user.getDisplayName());
            }

            if(user.getUsername() != null){
                usernameEditText.setText(user.getUsername());
            }

            if(user.getBio() != null){
                bioEditText.setText(user.getBio());
            }

            if(user.getGender() != null){
                genderSpinner.setSelection(Arrays.asList(getResources().getStringArray(R.array.gender_array)).indexOf(user.getGender()));
            }

            if(user.getAddress() != null){
                addressEditText.setText(user.getAddress());
            }

            updateInfoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserInformation();
                }
            });

            usernameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final String username = s.toString();
                    DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("usernames/"+username);
                    usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && username!=user.getUsername() && !TextUtils.isEmpty(username)){
                                usernameEditText.setError("Username already taken");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }
    }

    private void updateUserInformation() {
        String displayName = nameEditText.getText().toString();
        if(TextUtils.isEmpty(displayName)){
            nameEditText.setError("Name cannot be empty");
            nameEditText.requestFocus();
            return;
        }

        String bio = bioEditText.getText().toString();
        if(TextUtils.isEmpty(bio)){
            bioEditText.setError("Bio cannot be empty");
            bioEditText.requestFocus();
            return;
        }

        String address = addressEditText.getText().toString();
        if(TextUtils.isEmpty(address)){
            addressEditText.setError("Address cannot be empty");
            addressEditText.requestFocus();
            return;
        }

        final String username = usernameEditText.getText().toString();
        if(TextUtils.isEmpty(username)){
            usernameEditText.setError("Username cannot be empty");
            usernameEditText.requestFocus();
            return;
        }

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("usernames/"+username);
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && username!=user.getUsername()) {
                        usernameEditText.setError("Username already exists");
                        usernameEditText.requestFocus();
                        return;
                    }else{
                        updateDatabaseWithNewUserInfo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void updateDatabaseWithNewUserInfo() {

        startProgressDialog("Updating information", "Please wait");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(mUser.getUid());

        User updatedUser = new User();
        updatedUser.setId(mUser.getUid());
        updatedUser.setCoverPictureURI(user.getCoverPictureURI());
        updatedUser.setProfilePicURI(user.getProfilePicURI());
        updatedUser.setDisplayName(nameEditText.getText().toString());
        updatedUser.setUsername(usernameEditText.getText().toString());
        updatedUser.setEmail(user.getEmail());
        updatedUser.setBio(bioEditText.getText().toString());
        updatedUser.setGender(genderSpinner.getSelectedItem().toString());
        updatedUser.setAddress(addressEditText.getText().toString());

        userRef.setValue(updatedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("usernames").child(usernameEditText.getText().toString());
                usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            usernameRef.setValue(mUser.getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        hideProgressDialog();

                                        //Take to MainActivity
                                        Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        finish();
                                        startActivity(mainIntent);
                                    }else{
                                        createAlertDialog("Error Writing to Database", "Please check your internet connection");
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideProgressDialog();
                createAlertDialog("Error Writing to Database", "Please check your internet connection");
            }
        });
    }


    public void promptCoverPictureChange(View view){
        // start picker to get image for cropping and then use the image in cropping activity
        Intent intent = CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(500, 300)
                .setAspectRatio(5,3)
                .setFixAspectRatio(true)
                .getIntent(this);
        startActivityForResult(intent, RC_IMAGE_PICKER_COVER_PICTURE);
    }

    public void promptProfilePictureChange(View view){
        // start picker to get image for cropping and then use the image in cropping activity
        Intent intent = CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(300, 300)
                .setFixAspectRatio(true)
                .getIntent(this);
        startActivityForResult(intent, RC_IMAGE_PICKER_PROFILE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_IMAGE_PICKER_PROFILE_PICTURE) {
            //the profile picture has been cropped

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfilePictureProgressBar.setVisibility(View.VISIBLE);
                updateInfoBtn.setEnabled(false);

                Uri profilePictureUri = result.getUri();
                File originalPictureFile = new File(profilePictureUri.getPath());
                File compressedPictureFile = originalPictureFile;
                try {
                     compressedPictureFile = new Compressor(this)
                            .setMaxWidth(300)
                            .setMaxHeight(300)
                            .setQuality(75)
                            .compressToFile(originalPictureFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_profile_picture_placeholder_female);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(profilePictureUri)
                        .into(profilePictureImgView);


                final StorageReference profilePictureStorageRef =
                        mStorage.getReference()
                        .child("user_profile_images")
                        .child(mUser.getUid())
                        .child("profile_picture.jpg");
                UploadTask uploadTask = profilePictureStorageRef.putFile(Uri.fromFile(compressedPictureFile));
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return profilePictureStorageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            user.setProfilePicURI(downloadUri.toString());
                            mProfilePictureProgressBar.setVisibility(View.GONE);
                            updateInfoBtn.setEnabled(true);
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == RC_IMAGE_PICKER_COVER_PICTURE) {
            //the cover picture has been cropped

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mCoverPictureProgressBar.setVisibility(View.VISIBLE);
                updateInfoBtn.setEnabled(false);

                Uri coverPictureUri = result.getUri();

                File originalPictureFile = new File(coverPictureUri.getPath());
                File compressedPictureFile = originalPictureFile;
                try {
                    compressedPictureFile = new Compressor(this)
                            .setMaxWidth(500)
                            .setMaxHeight(300)
                            .setQuality(75)
                            .compressToFile(originalPictureFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                RequestOptions requestOptions = new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_cover_photo_placeholder);
                Glide.with(EditProfileActivity.this)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(coverPictureUri)
                        .into(coverPictureImgView);

                final StorageReference coverPictureStorageRef =
                        mStorage.getReference()
                                .child("user_profile_images")
                                .child(mUser.getUid())
                                .child("cover_picture.jpg");
                UploadTask uploadTask = coverPictureStorageRef.putFile(Uri.fromFile(compressedPictureFile));

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return coverPictureStorageRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task){
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            user.setCoverPictureURI(downloadUri.toString());
                            mCoverPictureProgressBar.setVisibility(View.GONE);
                            updateInfoBtn.setEnabled(true);
                        } else {
                            // Handle failures
                            // ...
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startProgressDialog(String title, String message){
        mProgressDialog = new ProgressDialog(EditProfileActivity.this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void hideProgressDialog(){
        mProgressDialog.dismiss();
    }

    private void createAlertDialog(String title, String alertMessage){
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfileActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(alertMessage);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
