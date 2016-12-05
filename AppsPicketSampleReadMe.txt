 RUNNING THE APPSPICKET SAMPLE APP
 ---------------------------------  

This sample demonstrates how to use AppsPicket library to authenticate user with Integrated Two Factor Authentication(I2FA) technology. 

Follow the below steps to run this AppsPicketSample app:

  1.Download the project: 
     a.Click on “clone or download” button .
     b.Click on “Download ZIP”.
     c.Unzip the downloaded project.
 
  2.Import the AppsPicketSample project into your Android Studio
     a.From the Welcome screen, click on "Import project".
     b.Browse to the project directory and press OK.

Now the  AppsPicketSample app is ready to run. Click on Run button and you will find the sample running on your emulator or device.

INTEGRATION OF APPSPICKET ANDROID SDK IN APP
--------------------------------------------

Introduction
============

In this guide, we will help you integrate the user authentication of your Android application with our award winning cloud based Integrated Two Factor Authentication(I2FA) technology. To reduce the work required to do the integration, we have developed the AppsPicket Android SDK that will be used.

On the diagram below you can see a full picture of how these things interact with each other.

<<<Diagram to show user <-> app <-> sdk <-> i2fa flow <-> company user database>>>

Pre-requisites
==============

To get started, you need

1. Get your unique I2FA URL from AppsPicket team that integrates with your user database or directory. We have connectors for most authentication setups.
2. Working Android development environment with Android SDK
3. Gradle enabled Android project of the application you wish to integrate

Get your unique I2FA URL
------------------------
AppsPicket team will configure a connector on our cloud based I2FA side that integrates with your user database or directory. We have connectors for most authentication setups. Please email team@appspicket.com for more details.

Once the connector is configured, the unique URL for your application will be generated and provided to you.

Installation
------------

Assuming you have a Gradle enabled Android project of your application, let us get the SDK in.

1. Add the following dependency in the module-level build.gradle file
   compile(group: 'com.appspicket.module', name: 'appspicket', version: '1.0.2', ext: 'aar'){transitive true}

2. Add following lines in the project-level build.gradle file
   allprojects {
        repositories {
            maven { url "https://jitpack.io" }
            jcenter()
            maven {
              url "http://artifactory.ipragmatech.com:8081/artifactory/libs-release-local"
            }
        }
    }
3. Finally build your Gradle

Use Cases
=========

Now we are ready to write the integration code. There are only two use cases,

1. When user logs in for the first time
2. When user logs in any subsequent time

When user logs in first time
----------------------------

When the user logs in first time, the SDK sends the username or mobile number and password and deviceId to our I2FA cloud, registers the user and also checks the authentication against your user directory. This is performed using the following code.

Step 1. Create I2FA service instance. For this your activity context object needs to be passed.
// ctx is your activity context object
//url is the the unique I2FA URL
I2FA service = new I2FA(ctx,url);

Step 2. Now we are ready call the service to register. This will generate the one-time secret for the user which is sent to the I2FA cloud along with the username and password. Since the service is called asynchronously, a response handler is needed to handle success and failures results i.e. I2FAHandler and it needs to be passed with the call.

service.registerUser(
  "<<username>>", // String containing username
  "<<password>>", // String containing password
  "<<mobilenumber>>", // String containing mobile number
  "<<deviceId>>" // String containing device id
  new I2FAHandler() {
    public void onSuccess(I2faResponse result) {
    // it handles the success result
    }

    public void onFailure(int statusCode, Exception e) {
    // It handles the failure result.
    // 10, AuthenticationException - thrown when User is not authenticated
    // 15, InvalidEmailException - thrown when user failed email check
    // 25, InvalidMobileException - throws when user failed mobile check
    // 35, EmailMobileRequiredException - One of email or mobile input is required
    // 45, DuplicateUserException - User already exists with a different email/mobile number
    // 51, EmptyUsernameException - Username is empty
    // 52, EmptyPasswordException - Password is empty
    // 55, EmptyEmailException - Email is empty
    // 65, DuplicateEmailException - User-email already exists
    // 75, UnknownException 
    }
  });

Step 3. Now user will receive an One Time Password(OTP) by email or on phone and user-id in response. This OTP is needed for final verification of the user. So this OTP is passed to the server using the following service:

service.submitOTP(
 "<<userName>>", //String containing user-name
 "<<otp>>",      //String containing otp received via email/mobile
 "<<userId>>",   //Integer containing user-id
 new I2FAHandler() {
    public void onSuccess(I2faResponse result) {
    // it handles the success result
    }

    public void onFailure(int statusCode, Exception e) {
    // It handles the failure result.
        // 51, EmptyUsernameException - thrown when Username is empty
	// 53, EmptyOtpException -  thrown when OTP is empty
        // 54, EmptyUserIdException - thrown when UserId is empty
	// 85, IncorrectOtpException -  thrown when Incorrect OTP
    }
  });

Step 4.Now when user signup is completed with success response you will need to call the login service:

 service.loginUser(
  "<<username>>", // String containing username
  "<<password>>", // String containing password
  "<<deviceId>>" // String containing device id received after signup i.e on otp service response
  new I2FAHandler() {
    public void onSuccess(I2faResponse result) {
    // it handles the success result
    }

    public void onFailure(int statusCode, Exception e) {
    // It handles the failure result.
    // 20, InvalidKeyException - thrown when no valid registration data found on the user device
    // 75, UnknownException - when user is not authenticated.
    // 51, EmptyUsernameException -  thrown when Username is empty
    }
  });



When user logs in subsequent time
----------------------------------

After the first time, the password is never sent again. The I2FA cloud performs the authentication based on secrets and tokens that are generated on the first time login. The SDK performs this transparently for you and you only have to call:

Step 1. Create I2FA service instance. For this your activity context object needs to be passed.
// ctx is your activity context object
//url is the the unique I2FA URL
I2FA service = new I2FA(ctx,url);

Step 2. Now we are ready call the service to login. This will generate the one-time secret for the user which is sent to the I2FA cloud. Since the service is called asynchronously, a response handler is needed to handle success and failures results i.e. I2FAHandler and it needs to be passed with the call.

service.loginUser(
  "<<username>>", // String containing username
  "<<password>>", // String containing password
  "<<deviceId>>" // String containing device id
  new I2FAHandler() {
    public void onSuccess(I2faResponse result) {
    // it handles the success result
    }

    public void onFailure(int statusCode, Exception e) {
    // It handles the failure result.
    // 20, InvalidKeyException - thrown when no valid registration data found on the user device
    // 75, UnknownException - when user is not authenticated.
    // 51, EmptyUsernameException -  thrown when Username is empty
    }
  });

For more details about AppsPicket Library visit our site :  http://web.appspicket.com/
