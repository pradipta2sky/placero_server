package lm.pkp.com.landmap.google.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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

import org.json.JSONObject;

import lm.pkp.com.landmap.R;
import lm.pkp.com.landmap.R.id;
import lm.pkp.com.landmap.R.layout;
import lm.pkp.com.landmap.R.string;
import lm.pkp.com.landmap.SplashActivity;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
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
        this.setContentView(layout.activity_google_signin_main);

        ActionBar ab = getSupportActionBar();
        ab.hide();

        this.mStatusTextView = (TextView) this.findViewById(id.status);

        this.findViewById(id.sign_in_button).setOnClickListener(this);
        this.findViewById(id.sign_out_button).setOnClickListener(this);
        this.findViewById(id.disconnect_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        this.mGoogleApiClient = new Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton signInButton = (SignInButton) this.findViewById(id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(this.mGoogleApiClient);
        if (opr.isDone()) {
            Log.d(SignInActivity.TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            this.handleSignInResult(result);
        } else {
            this.showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    SignInActivity.this.hideProgressDialog();
                    SignInActivity.this.handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.hideProgressDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("Signin Result:[ ReqC - " + requestCode + ", ResC - " + resultCode + " ]");

        if (requestCode == SignInActivity.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            this.handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(SignInActivity.TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            this.signedUser = UserMappingUtil.convertGoogleAccountToLocalAccount(acct);

            UserDBHelper udh = new UserDBHelper(this.getApplicationContext());
            UserElement localUser = udh.getUserByEmail(this.signedUser.getEmail());
            if (localUser == null) {
                udh.insertUserLocally(this.signedUser);
            }
            UserContext.getInstance().setUserElement(this.signedUser);
            this.searchOnRemoteAndUpdate(this.signedUser);

            this.finish();

            Intent spashIntent = new Intent(this, SplashActivity.class);
            this.startActivity(spashIntent);
        } else {
            this.updateUI(false);
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
            UserDBHelper udh = new UserDBHelper(this.getApplicationContext());
            if (userDetails.trim().equalsIgnoreCase("[]")) {
                udh.insertUserToServer(this.signedUser);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(this.mGoogleApiClient);
        this.startActivityForResult(signInIntent, SignInActivity.RC_SIGN_IN);
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(this.mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        SignInActivity.this.updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(this.mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        SignInActivity.this.updateUI(false);
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(SignInActivity.TAG, "onConnectionFailed:" + connectionResult);
        TextView debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setVisibility(View.VISIBLE);
        debugText.setText("onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mProgressDialog != null) {
            this.mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(this);
            this.mProgressDialog.setMessage(this.getString(string.loading));
            this.mProgressDialog.setIndeterminate(true);
        }

        this.mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            this.findViewById(id.sign_in_button).setVisibility(View.GONE);
            this.findViewById(id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            this.mStatusTextView.setText(string.signed_out);

            this.findViewById(id.sign_in_button).setVisibility(View.VISIBLE);
            this.findViewById(id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case id.sign_in_button:
                this.signIn();
                break;
            case id.sign_out_button:
                this.signOut();
                break;
            case id.disconnect_button:
                this.revokeAccess();
                break;
        }
    }

}
