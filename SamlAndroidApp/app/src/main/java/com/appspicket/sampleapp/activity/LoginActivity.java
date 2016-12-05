package com.appspicket.sampleapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appspicket.i2fa.I2FAHandler;
import com.appspicket.i2fa.I2FA;
import com.appspicket.i2fa.I2faResponse;
import com.appspicket.i2fa.utils.Constants;
import com.appspicket.i2fa.utils.SecureKeyStore;
import com.appspicket.sampleapp.R;

import java.io.IOException;


/**
 * A login screen that offers login via username/password .
 */
public class LoginActivity extends FragmentActivity {

    //I2fa Url
    private static final String I2FA_URL = "https://mobile.appspicket.com/module.php/extendtwofactorauthentication/ipragsaml.php";

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mUserNameView;
    private EditText mMobileNumberView;
    private EditText mOTPView;
    private Button mEmailSignUpButton;
    private View mProgressView;
    private View mLoginFormView;

    private String deviceId;
    Integer userId;
    I2FA service;
    private Boolean signupWAD = false;
    private Boolean isLogin = true;
    private Boolean otpStep = false;
    private Dialog msgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mUserNameView = (EditText) findViewById(R.id.username);
        mMobileNumberView = (EditText) findViewById(R.id.mobile);
        mOTPView = (EditText) findViewById(R.id.otp);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                return false;
            }
        });
        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        final Button mEmailSignupAdButton = (Button) findViewById(R.id.ad_sign_up_button);
        //Initialize the I2FA service class
        service = new I2FA(LoginActivity.this, I2FA_URL);

        mPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mEmailSignInButton.performClick();
                    return true;
                }
                return false;
            }
        });

        //already existing user login
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isLogin = true;
                mEmailSignInButton.setBackgroundColor(getResources().getColor(R.color.lightorange));
                mEmailSignUpButton.setBackgroundColor(getResources().getColor(R.color.darkorange));
                signupWAD = false;
                otpStep = false;
                if (mEmailView.getVisibility() == View.VISIBLE && mMobileNumberView.getVisibility() == View.VISIBLE) {
                    mEmailView.setVisibility(View.GONE);
                    mMobileNumberView.setVisibility(View.GONE);
                    mPasswordView.setText("");
                    mUserNameView.setText("");
                    mEmailView.setText("");
                    mMobileNumberView.setText("");
                }
            }
        });

        //new user sign up
        mEmailSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                isLogin = false;
                mEmailSignUpButton.setBackgroundColor(getResources().getColor(R.color.lightorange));
                mEmailSignInButton.setBackgroundColor(getResources().getColor(R.color.darkorange));
                signupWAD = false;
                if (mEmailView.getVisibility() == View.GONE && mMobileNumberView.getVisibility() == View.GONE) {
                    mEmailView.setVisibility(View.VISIBLE);
                    mMobileNumberView.setVisibility(View.VISIBLE);
                }
            }
        });
        //signup with AD
        mEmailSignupAdButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(isLogin);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin(boolean isLogin) {


        boolean cancel = false;
        View focusView = null;

        if (msgDialog != null && msgDialog.isShowing() && mOTPView != null) {
            String otp = mOTPView.getText().toString();
            mOTPView.setError(null);
            // Check for a valid email address.
            if (TextUtils.isEmpty(otp)) {
                mOTPView.setError(getString(R.string.error_field_required));
                focusView = mOTPView;
                cancel = true;
            }
        } else if (mLoginFormView.getVisibility() == View.VISIBLE) {
            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();
            String username = mUserNameView.getText().toString();
            String mobile = mMobileNumberView.getText().toString();

            // Reset errors.
            if (mEmailView.getVisibility() == View.VISIBLE) {
                mEmailView.setError(null);
                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    focusView = mEmailView;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    mEmailView.setError(getString(R.string.error_invalid_email));
                    focusView = mEmailView;
                    cancel = true;
                }

            }
            mPasswordView.setError(null);
            mUserNameView.setError(null);


            // Check for a valid password, if the user entered one.
            if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
            if (TextUtils.isEmpty(username)) {
                mUserNameView.setError(getString(R.string.error_field_required));
                focusView = mUserNameView;
                cancel = true;
            } else if (!isUserNameValid(username)) {
                mEmailView.setError(getString(R.string.error_invalid_username));
                focusView = mEmailView;
                cancel = true;
            }
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            initializeProcess(isLogin);
        }
    }

    /**
     * Filters which I2FA service you are going to call
     */
    public void initializeProcess(boolean isLogin) {

        if (!isLogin) {
            if (!otpStep)
                callSignUpMethod(mEmailView.getText().toString(), mMobileNumberView.getText().toString(), mUserNameView.getText().toString(), mPasswordView.getText().toString(), deviceId);
            else {
                msgDialog.cancel();// to hide the OTP dialog
                showProgress(true);
                callOTPSubmitMethod(mUserNameView.getText().toString(), mPasswordView.getText().toString(), mOTPView.getText().toString(), userId, deviceId);

            }
        } else {
            callLoginMethod(mUserNameView.getText().toString(), mPasswordView.getText().toString(), deviceId);
        }

    }

    /**
     * Method to call I2FA signup service
     *
     * @param email
     * @param password
     * @param deviceId
     * @param mobileNumber
     */
    private void callSignUpMethod(final String email, final String mobileNumber, final String userName, final String password, final String deviceId) {
        try {
            service.registerUser(email, mobileNumber, userName, password, deviceId, new I2FAHandler() {
                //Method to handle success response
                @Override
                public void onSuccess(I2faResponse result) {
                    if (result != null) {
                        try {
                            showProgress(false);
                            otpStep = true;
                            userId = result.getUser().getUserId();
                            showAlertBox();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Method to handle failure response
                @Override
                public void onFailure(int statusCode, Exception e) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // To show a dialog to enter OTP
    private void showAlertBox() {
        msgDialog = new Dialog(this);
        msgDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        msgDialog.setContentView(R.layout.alert_otp_submit);
        msgDialog.setCancelable(false);
        mOTPView = (EditText) msgDialog.findViewById(R.id.otp);
        TextView alertCancel = (TextView) msgDialog.findViewById(R.id.cancel);
        alertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpStep = false;
                msgDialog.cancel();
            }
        });
        TextView alertOk = (TextView) msgDialog.findViewById(R.id.ok);
        alertOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin(false);
            }
        });
        msgDialog.show();


    }

    /**
     * Method to submit otp for final I2FA signup
     *
     * @param userName
     * @param password
     * @param otp
     * @param userId
     * @param deviceId
     */
    private void callOTPSubmitMethod(final String userName, final String password, final String otp, final Integer userId, final String deviceId) {
        try {
            service.submitOTP(userName, otp, userId, new I2FAHandler() {
                //Method to handle success response
                @Override
                public void onSuccess(I2faResponse result) {
                    if (result != null) {
                        try {
                            Toast.makeText(LoginActivity.this,
                                    result.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                            callLoginMethod(userName, password, result.getDeviceId());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Method to handle failure response
                @Override
                public void onFailure(int statusCode, Exception e) {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to call I2FA login service
     *
     * @param userName
     * @param password
     * @param deviceId
     */
    private void callLoginMethod(String userName, String password, String deviceId) {
        try {
            service.loginUser(userName, password, deviceId, new I2FAHandler() {
                //Method to handle success response
                @Override
                public void onSuccess(I2faResponse result) {
                    if (result != null) {
                        try {
                            showProgress(false);
                            Intent intent = new Intent(LoginActivity.this, ServiceActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Method to handle failure response
                @Override
                public void onFailure(int statusCode, Exception e) {
                    showProgress(false);
                    if(e.getMessage().contains("User not verified"))
                        Toast.makeText(LoginActivity.this,e.getMessage()+".Please signup!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Method to check entered email is valid or not
    private boolean isEmailValid(String email) {

        return (email.length() > 3 && email.contains("@"));
    }

    //Method to check entered email is valid or not
    private boolean isUserNameValid(String username) {

        return username.length() > 3;
    }

    //Method to check entered password is valid or not
    private boolean isPasswordValid(String password) {

        return password.length() > 3;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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


}



