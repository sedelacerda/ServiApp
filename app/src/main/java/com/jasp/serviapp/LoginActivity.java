package com.jasp.serviapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    //region UI references.
    private TextInputLayout firstNameView;
    private TextInputLayout lastNameView;
    private TextInputLayout birthDateView;
    private TextInputLayout mobilePhoneView;
    private TextInputLayout workPhoneView;
    private TextInputLayout emailView;
    private TextInputLayout passwordView;
    private Spinner genderSpinner;

    private AutoCompleteTextView firstNameText;
    private AutoCompleteTextView lastNameText;
    private AutoCompleteTextView birthDateText;
    private AutoCompleteTextView mobilePhoneText;
    private AutoCompleteTextView workPhoneText;
    private AutoCompleteTextView emailText;
    private EditText passwordText;

    private Button signInButton;
    private Button signUpButton;
    private View separatorView;
    private LoginButton fbLogin;

    private View mProgressView;
    private View mLoginFormView;
    //endregion

    /* The callback manager for Facebook */
    private CallbackManager mFacebookCallbackManager;
    /* Used to track user logging in/out off Facebook */
    private AccessTokenTracker mFacebookAccessTokenTracker;

    boolean signInMode = false;

    public static User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        setContentView(R.layout.layout_login);

        //region Seteamos los componentes del incio de sesion con usuario y contraseña

        //region Referenciamos los objetos de la UI
        firstNameView = (TextInputLayout) findViewById(R.id.first_name_view);
        lastNameView = (TextInputLayout) findViewById(R.id.last_name_view);
        birthDateView = (TextInputLayout) findViewById(R.id.birth_date_view);
        mobilePhoneView = (TextInputLayout) findViewById(R.id.mobile_phone_view);
        workPhoneView = (TextInputLayout) findViewById(R.id.work_phone_view);
        emailView = (TextInputLayout) findViewById(R.id.email_view);
        passwordView = (TextInputLayout) findViewById(R.id.password_view);

        firstNameText = (AutoCompleteTextView) findViewById(R.id.first_name);
        lastNameText = (AutoCompleteTextView) findViewById(R.id.last_name);
        birthDateText = (AutoCompleteTextView) findViewById(R.id.birth_date);
        mobilePhoneText = (AutoCompleteTextView) findViewById(R.id.mobile_phone);
        workPhoneText = (AutoCompleteTextView) findViewById(R.id.work_phone);
        emailText = (AutoCompleteTextView) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);

        signInButton = (Button) findViewById(R.id.email_sign_in_button);
        signUpButton = (Button) findViewById(R.id.email_sign_up_button);
        separatorView = findViewById(R.id.login_separator);
        fbLogin = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //iniciamos el spinner
        ArrayList<String> genders = new ArrayList<String>();
        genders.add(getString(R.string.prompt_gender_male));
        genders.add(getString(R.string.prompt_gender_female));
        genderSpinner = (Spinner) findViewById(R.id.gender_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, genders);
        genderSpinner.setAdapter(adapter);

        //endregion

        populateAutoComplete();

        birthDateText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    DateDialog dialog = new DateDialog(v);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    dialog.show(ft, "Date Picker");
                }
            }
        });

        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(signInMode){
                    setSignUpLayout();
                }
                else{
                    registerUser();
                }
            }
        });

        //endregion

        //region Seteamos el boton para login con facebook
        mFacebookCallbackManager = CallbackManager.Factory.create();
        fbLogin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));

        fbLogin.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                AccessToken fbToken = loginResult.getAccessToken();

                AuthCredential credential = FacebookAuthProvider.getCredential(fbToken.getToken());
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //Auth failed
                        }
                    }
                });

                //region Extraemos datos del usuario de Facebook e iniciamos sesion si se puede
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                String id = null;
                                String firstName = null;
                                String lastName = null;
                                String email = null;
                                String gender = null;
                                String birthday = null;

                                //region Recibimos los datos de Facebook
                                try {
                                    id = object.getString("id");
                                    firstName = object.getString("first_name");
                                    lastName = object.getString("last_name");
                                    email = object.getString("email");
                                    gender = object.getString("gender");
                                    birthday = object.getString("birthday"); // 01/31/1980 format
                                    //Cambiamos el formato de la fecha
                                    if(0 < birthday.length()){
                                        try{
                                            String[] comp = birthday.split("/");
                                            birthday = comp[1] + "/" + comp[0] + "/" + comp[2];
                                        }catch (IndexOutOfBoundsException e){ }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                final String mid = id;
                                final String mfirstName = firstName;
                                final String mlastName = lastName;
                                final String memail = email;
                                final String mgender = gender;
                                final String mbirthday = birthday;

                                final boolean[] usersIsEmpty = new boolean[]{true};
                                //endregion

                                InitActivity.myFirebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren())
                                            usersIsEmpty[0] = false;

                                        if (usersIsEmpty[0]) {
                                            System.out.println("Inside if.");
                                            //region Si users no tiene elementos entonces rellenamos el formulario de registro y enviamos a la pantalla de login
                                            firstNameText.setText(mfirstName);
                                            lastNameText.setText(mlastName);
                                            birthDateText.setText(mbirthday);
                                            emailText.setText(memail);
                                            ArrayAdapter<String> adapter = (ArrayAdapter) genderSpinner.getAdapter();
                                            if (mgender.equalsIgnoreCase("male"))
                                                genderSpinner.setSelection(adapter.getPosition(getString(R.string.prompt_gender_male)));
                                            else
                                                genderSpinner.setSelection(adapter.getPosition(getString(R.string.prompt_gender_female)));
                                            LoginManager.getInstance().logOut();
                                            setSignUpLayout();
                                            Toast.makeText(LoginActivity.this, "No se pudo iniciar sesion. " +
                                                    "No existe ningún usuario.", Toast.LENGTH_LONG).show();
                                            //endregion
                                        } else {
                                            System.out.println("Inside else.");
                                            InitActivity.myFirebaseRef.child("users").addChildEventListener(new ChildEventListener() {
                                                @Override
                                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                    //region Si el FacebookID ya esta registrado, entonces iniciamos sesion
                                                    if (dataSnapshot.child("facebookID").exists() && mid != null) {
                                                        if (dataSnapshot.child("facebookID").getValue().toString().equals(mid)) {
                                                            user = dataSnapshot.getValue(User.class);
                                                            onFacebookAccessTokenChange(loginResult.getAccessToken());
                                                            goToNavigationActivity();
                                                        }
                                                    }
                                                    //endregion

                                                    //region Si el FacebookID no esta registrado, entonces se debe iniciar sesion con telefono/contraseña
                                                    else {
                                                        //Rellenamos el formulario de registro y enviamos a la pantalla de login
                                                        firstNameText.setText(mfirstName);
                                                        lastNameText.setText(mlastName);
                                                        birthDateText.setText(mbirthday);
                                                        emailText.setText(memail);
                                                        ArrayAdapter<String> adapter = (ArrayAdapter) genderSpinner.getAdapter();
                                                        if (mgender.equalsIgnoreCase("male"))
                                                            genderSpinner.setSelection(adapter.getPosition(getString(R.string.prompt_gender_male)));
                                                        else
                                                            genderSpinner.setSelection(adapter.getPosition(getString(R.string.prompt_gender_female)));
                                                        LoginManager.getInstance().logOut();
                                                        setSignUpLayout();
                                                        Toast.makeText(LoginActivity.this, "No se pudo iniciar sesion. " +
                                                                "Su cuenta de Facebook no esta registrada", Toast.LENGTH_LONG).show();
                                                    }
                                                    //endregion

                                                }

                                                @Override
                                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                                }

                                                @Override
                                                public void onChildRemoved(DataSnapshot dataSnapshot) {
                                                }

                                                @Override
                                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }

                                        InitActivity.myFirebaseRef.removeEventListener(this);
                                        System.out.println("Listener removed.");
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
                //endregion
            }

            @Override
            public void onCancel() {
                Log.v("LoginActivity", "cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.v("LoginActivity", error.getCause().toString());
            }
        });


        //endregion

        final LinearLayout layoutView = (LinearLayout) findViewById(R.id.login_main_layout);

        layoutView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Layout has happened here.
                        setSignInLayout();
                        // Don't forget to remove your listener when you are done with it.
                        layoutView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
        );

        /** Iniciamos el autenticador de Firebase */
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        setSignInLayout();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private void onFacebookAccessTokenChange(AccessToken token){

    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(emailText, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid mobilePhone, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mobilePhoneText.setError(null);
        passwordText.setError(null);

        // Store values at the time of the login attempt.
        String mobilePhone = mobilePhoneText.getText().toString();
        String password = passwordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid mobile phone
        if (TextUtils.isEmpty(mobilePhone)) {
            mobilePhoneText.setError(getString(R.string.error_field_required));
            focusView = mobilePhoneText;
            cancel = true;
        } else if (!isMobilePhoneValid(mobilePhone)) {
            mobilePhoneText.setError(getString(R.string.error_invalid_mobile_phone));
            focusView = emailText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(mobilePhone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isMobilePhoneValid(String mobilePhone) {
        //TODO: Replace this with your own logic
        if(mobilePhone.length() == 12 && mobilePhone.substring(0, 4).equalsIgnoreCase("+569"))
            return true;
        else
            return false;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        emailText.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMobilePhone;
        private final String mLoginEmail;
        private final String mPassword;

        UserLoginTask(String mobilePhone, String password) {
            mMobilePhone = mobilePhone;
            mLoginEmail = mobilePhone + "@serviapp.cl";
            mPassword = password;

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            final boolean[] output = new boolean[]{false};
            user.setMobilePhone(mMobilePhone);

            mAuth.signInWithEmailAndPassword(mLoginEmail, mPassword)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else {
                                InitActivity.myFirebaseRef.child("users").child(user.getMobilePhone()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        user.setFirstName(dataSnapshot.child("firstName").getValue().toString());
                                        user.setLastName(dataSnapshot.child("lastName").getValue().toString());
                                        user.setBirthDate(dataSnapshot.child("birthDate").getValue().toString());
                                        if(dataSnapshot.child("email").exists())
                                            user.setEmail(dataSnapshot.child("email").getValue().toString());
                                        if(dataSnapshot.child("workPhone").exists())
                                            user.setWorkPhone(dataSnapshot.child("workPhone").getValue().toString());

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                output[0] = true;
                            }
                        }
                    });

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return output[0];
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                goToNavigationActivity();
            } else {
                passwordText.setError(getString(R.string.error_incorrect_password));
                passwordText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    //region Estos 2 metodos son para modificar y animar la UI

    /*El metodo setSignUpLayout traslada los objetos de la UI para que queden
    solo los elementos necesarios para registrar un usuario
     */
    private void setSignUpLayout(){
        if(signInMode){
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;

            float translationPhoneY = 0;
            float translationY = 0;
            float translationButtonsY = signInButton.getY() - signUpButton.getY();
            float translationX = 0;
            float translationButtonsX = screenWidth;

            long durationY = 500;
            long durationX = 500;

            firstNameView.animate().translationX(translationX).setDuration(durationX);
            lastNameView.animate().translationX(translationX).setDuration(durationX);
            birthDateView.animate().translationX(translationX).setDuration(durationX);
            genderSpinner.animate().translationX(translationX).setDuration(durationX);
            mobilePhoneView.animate().translationY(translationPhoneY).setDuration(durationY);
            workPhoneView.animate().translationX(translationX).setDuration(durationX);
            emailView.animate().translationX(translationX).setDuration(durationX);
            passwordView.animate().translationY(translationY).setDuration(durationY);

            signInButton.animate().translationX(-translationButtonsX).setDuration(durationX);
            signUpButton.animate().translationY(translationButtonsY).setDuration(durationY);
            separatorView.animate().translationX(-translationButtonsX).setDuration(durationX);
            fbLogin.animate().translationX(-translationButtonsX).setDuration(durationX);

            signInMode = false;
            this.setTitle(R.string.title_activity_signup);
        }
    }

    /*El metodo setSignInLayout traslada los objetos de la UI para que queden
    solo los elementos necesarios para que un usuario pueda iniciar sesión
     */
    private void setSignInLayout(){
        if(!signInMode && firstNameView.getY() != mobilePhoneView.getY()){
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;

            float translationPhoneY = firstNameView.getY() - mobilePhoneView.getY();
            float translationY = lastNameView.getY() - passwordView.getY();
            float translationX = screenWidth;
            long durationY = 500;
            long durationX = 500;

            firstNameView.animate().translationX(translationX).setDuration(durationX);
            lastNameView.animate().translationX(translationX).setDuration(durationX);
            birthDateView.animate().translationX(translationX).setDuration(durationX);
            genderSpinner.animate().translationX(translationX).setDuration(durationX);
            mobilePhoneView.animate().translationY(translationPhoneY).setDuration(durationY);
            workPhoneView.animate().translationX(translationX).setDuration(durationX);
            emailView.animate().translationX(translationX).setDuration(durationX);
            passwordView.animate().translationY(translationY).setDuration(durationY);

            signInButton.animate().translationX(0).translationY(translationY).setDuration(durationX);
            signUpButton.animate().translationY(translationY).setDuration(durationY);
            separatorView.animate().translationX(0).translationY(translationY).setDuration(durationY);
            fbLogin.animate().translationX(0).translationY(translationY).setDuration(durationY);

            signInMode = true;
            this.setTitle(R.string.title_activity_login);
        }
    }
    //endregion

    @Override
    public void onBackPressed() {
        if(!signInMode)
            setSignInLayout();
    }

    private void goToNavigationActivity(){
        startActivity(new Intent(LoginActivity.this, NavigationActivity.class));
    }

    private void loginUser(String firstName, String lastName){

    }

    private void registerUser(){

        final String firstName = firstNameText.getText().toString();
        final String lastName = lastNameText.getText().toString();
        final String birthDate = birthDateText.getText().toString();
        final String mobilePhone = mobilePhoneText.getText().toString();
        final String workPhone = workPhoneText.getText().toString();
        final String email = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        final String loginEmail = mobilePhone + "@serviapp.cl";

        mAuth.createUserWithEmailAndPassword(loginEmail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        System.out.println("Successfully created user account with uid: " + task.getResult().getUser().getUid());

                        User newUser = new User();
                        if(0 < firstName.length())
                            newUser.setFirstName(firstName);
                        if(0 < lastName.length())
                            newUser.setLastName(lastName);
                        if(0 < birthDate.length())
                            newUser.setBirthDate(birthDate);
                        if(0 < mobilePhone.length())
                            newUser.setMobilePhone(mobilePhone);
                        if(0 < workPhone.length())
                            newUser.setWorkPhone(workPhone);
                        if(0 < email.length())
                            newUser.setEmail(email);
                        if(0 < password.length())
                            newUser.setPassword(password);

                        InitActivity.myFirebaseRef.child("users").child(newUser.getMobilePhone()).setValue(newUser);
                        Toast.makeText(LoginActivity.this, "Usuario creado!", Toast.LENGTH_SHORT).show();
                        setSignInLayout();
                    }
                });
    }

}

