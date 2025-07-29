package com.example.artisan;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.io.IOException;
import java.util.Collections;

public class AccessTokenGenerator {

    public static String getAccessToken(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("serviceAccountKey.json");

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));

            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();

        } catch (IOException e) {
            Log.e("AccessTokenGenerator", "Error generating access token", e);
            return null;
        }
    }
}
