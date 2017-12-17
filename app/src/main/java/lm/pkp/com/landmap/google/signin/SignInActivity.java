package lm.pkp.com.landmap.google.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.R.string;
import lm.pkp.com.landmap.SplashActivity;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.tags.TagElement;
import lm.pkp.com.landmap.user.UserContext;
import lm.pkp.com.landmap.user.UserDBHelper;
import lm.pkp.com.landmap.user.UserElement;
import lm.pkp.com.landmap.user.UserInfoSearchAsyncTask;
import lm.pkp.com.landmap.util.UserMappingUtil;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class SignInActivity extends AppCompatActivity implements
        OnConnectionFailedListener,
        OnClickListener, AsyncTaskCallback {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;
    private UserElement signedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            String cause = extras.getString("cause");
            if(cause != null && cause.equalsIgnoreCase("crash")){
                // TODO something for crash recovery
            }
        }

        setContentView(layout.activity_google_signin_main);
        getSupportActionBar().hide();

        mStatusTextView = (TextView) findViewById(id.status);

        findViewById(id.sign_in_button).setOnClickListener(this);
        findViewById(id.sign_out_button).setOnClickListener(this);
        findViewById(id.disconnect_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) findViewById(id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("Signin Result:[ ReqC - " + requestCode + ", ResC - " + resultCode + " ]");

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            signedUser = UserMappingUtil.convertGoogleAccountToLocalAccount(acct);

            UserDBHelper udh = new UserDBHelper(getApplicationContext());
            UserElement localUser = udh.getUserByEmail(signedUser.getEmail());
            if (localUser == null) {
                udh.insertUserLocally(signedUser);
            }
            UserContext.getInstance().setUserElement(signedUser);
            searchOnRemoteAndUpdate(signedUser);

            mGoogleApiClient.disconnect();
        } else {
            updateUI(false);
        }
    }

    private void searchOnRemoteAndUpdate(UserElement ue) {
        UserInfoSearchAsyncTask searchUserTask = new UserInfoSearchAsyncTask();
        JSONObject params = new JSONObject();
        try {
            params.put("ss", ue.getEmail());
            params.put("sf", "email");
            searchUserTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        searchUserTask.setCompletionCallback(this);
    }

    @Override
    public void taskCompleted(Object result) {
        try {
            String userDetails = result.toString();
            UserDBHelper udh = new UserDBHelper(getApplicationContext());
            Intent spashIntent = new Intent(this, SplashActivity.class);
            if (userDetails.trim().equalsIgnoreCase("[]")) {
                udh.insertUserToServer(signedUser);
                spashIntent.putExtra("user_exists", "false");
            }else {
                // Parse user details
                JSONArray userArr = new JSONArray(userDetails);
                JSONObject userObj = (JSONObject) userArr.get(0);
                String tagsStr = userObj.getString("tags");
                String[] tagsArr = tagsStr.split(",");

                String tagTypes = userObj.getString("tag_types");
                String[] tagTypesArr = tagTypes.split(",");

                UserElement userElement = UserContext.getInstance().getUserElement();
                for (int i = 0; i < tagsArr.length; i++) {
                    TagElement tagElement = new TagElement(tagsArr[i], tagTypesArr[i], "user");
                    userElement.getPreferences().getTags().add(tagElement);
                }
                spashIntent.putExtra("user_exists", "true");
            }
            startActivity(spashIntent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            findViewById(id.sign_in_button).setVisibility(View.GONE);
            findViewById(id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(string.signed_out);

            findViewById(id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.sign_in_button:
                signIn();
                break;
            case id.sign_out_button:
                signOut();
                break;
            case id.disconnect_button:
                revokeAccess();
                break;
        }
    }
}
