package com.thecoffeecoders.chatex;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thecoffeecoders.chatex.auth.LoginActivity;
import com.thecoffeecoders.chatex.fragments.ChatFragment;
import com.thecoffeecoders.chatex.fragments.FindFriendsFragment;
import com.thecoffeecoders.chatex.fragments.FriendsFragment;
import com.thecoffeecoders.chatex.fragments.GroupsFragment;
import com.thecoffeecoders.chatex.fragments.RequestsFragment;
import com.thecoffeecoders.chatex.models.User;
import com.thecoffeecoders.chatex.users.EditProfileActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ChatFragment.OnFragmentInteractionListener,
        FriendsFragment.OnFragmentInteractionListener,
        GroupsFragment.OnFragmentInteractionListener,
        RequestsFragment.OnFragmentInteractionListener,
        FindFriendsFragment.OnFragmentInteractionListener{

    //Firebase Objects
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

//    private Uri profilePictureURI;
//    private Uri coverPictureURI;
//    private String displayName;
//    private String username;
    private User localUser;

    //Layout elements
    private TextView usernameTextView;
    private TextView displayNameTextView;
    private ImageView profilePictureImageView;
    private ImageView coverPictureImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //Fireabase objects
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        //localUser = new User();

        //check whether the user is signed in or not
        checkUserSignedIn();

        //create a Auth State Listener
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    promptForLogin();
                }
            }
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Layout Elements instantiation
        usernameTextView = navigationView.getHeaderView(0).findViewById(R.id.main_usernameTextView);
        displayNameTextView = navigationView.getHeaderView(0).findViewById(R.id.main_displayNameTextview);
        coverPictureImageView = navigationView.getHeaderView(0).findViewById(R.id.main_coverPictureImageView);
        profilePictureImageView = navigationView.getHeaderView(0).findViewById(R.id.main_profilePictureImgView);


        if(mUser != null){
            new DownloadUserDataAndUpdateUITask().execute();
            //updateHeaderWithInfo();
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.navdrawer_screen_area, new ChatFragment());
        fragmentTransaction.commit();
    }

    private void checkUserSignedIn() {
        if(mUser != null){
            return;
        } else{
            promptForLogin();
        }
    }

    private void promptForLogin() {
        //TODO take the user to Login Page
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(loginIntent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            signOutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;

        if (id == R.id.nav_chats) {
            //ChatFragment
            fragment = new ChatFragment();
        } else if (id == R.id.nav_friends) {
            //FriendsFragment
            fragment = new FriendsFragment();
        } else if (id == R.id.nav_group) {
            //GroupFragment
            fragment = new GroupsFragment();
        } else if (id == R.id.nav_requests) {
            //RequestsFragment
            fragment = new RequestsFragment();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_find_friends) {
            fragment = new FindFriendsFragment();
        }

        if(fragment != null){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.navdrawer_screen_area, fragment);
            fragmentTransaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

//    public void updateHeaderWithInfo(){
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid());
//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                localUser = dataSnapshot.getValue(User.class);
//
//                if(localUser.getProfilePicURI() != null){
//                    RequestOptions requestOptions = new RequestOptions()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .placeholder(R.drawable.img_profile_picture_placeholder_female);
//                    Glide.with(MainActivity.this)
//                            .applyDefaultRequestOptions(requestOptions)
//                            .load(localUser.getProfilePicURI())
//                            .into(profilePictureImageView);
//                }
//
//                if(localUser.getCoverPictureURI() != null){
//                    RequestOptions requestOptions = new RequestOptions()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .placeholder(R.drawable.img_cover_photo_placeholder);
//                    Glide.with(MainActivity.this)
//                            .applyDefaultRequestOptions(requestOptions)
//                            .load(localUser.getCoverPictureURI())
//                            .into(coverPictureImageView);
//                }
//                if(localUser.getDisplayName() != null){
//                    displayNameTextView.setText(localUser.getDisplayName());
//                }
//                if(localUser.getUsername() != null){
//                    usernameTextView.setText(localUser.getUsername());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("firebasedatabase", databaseError.getMessage());
//            }
//        });
//    }

    private class DownloadUserDataAndUpdateUITask extends AsyncTask<Void, Void, Void>{

        MainActivity mainActivity;

        public void setContext(MainActivity mainActivity){
            this.mainActivity = mainActivity;
        }
        @Override
        protected Void doInBackground(Void... voids) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.getUid());
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        localUser = dataSnapshot.getValue(User.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(localUser.getProfilePicURI() != null){
                                    RequestOptions requestOptions = new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .placeholder(R.drawable.img_profile_picture_placeholder_female);
                                    Glide.with(MainActivity.this)
                                            .applyDefaultRequestOptions(requestOptions)
                                            .load(localUser.getProfilePicURI())
                                            .into(profilePictureImageView);
                                }

                                if(localUser.getCoverPictureURI() != null){
                                    RequestOptions requestOptions = new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                                            .placeholder(R.drawable.img_cover_photo_placeholder);
                                    Glide.with(MainActivity.this)
                                            .applyDefaultRequestOptions(requestOptions)
                                            .load(localUser.getCoverPictureURI())
                                            .into(coverPictureImageView);
                                }
                                if(localUser.getDisplayName() != null){
                                    displayNameTextView.setText(localUser.getDisplayName());
                                }
                                if(localUser.getUsername() != null){
                                    usernameTextView.setText("@" + localUser.getUsername());
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("firebasedatabase", databaseError.getMessage());
                    }
                });
                return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);
        }
    }
}
