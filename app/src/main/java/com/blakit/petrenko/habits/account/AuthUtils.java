package com.blakit.petrenko.habits.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.blakit.petrenko.habits.HabitApplication;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;

/**
 * Created by user_And on 07.10.2015.
 */
public class AuthUtils {
    public static final String PREFS_NAME = HabitApplication.getInstance().getPackageName()+".account";
    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String PREF_TOKEN = "accessToken";

    public static final int REQUEST_CODE_AUTH = 42;

    public static void refreshAuthToken(final Activity context, String accName){
        final SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        final String accessToken = settings.getString(PREF_TOKEN, "");
        final String accountName = settings.getString(PREF_ACCOUNT_NAME, accName != null ? accName : "");

        final GoogleAccountManager manager = new GoogleAccountManager(context);
        final Account account = manager.getAccountByName(accountName);
        final Intent callback = new Intent(context, context.getClass());

        if (account == null){
            manager.getAccountManager()
                    .addAccount(GoogleAccountManager.ACCOUNT_TYPE, null, null, null, context, null, null);
        }else{
            final AccountManagerCallback<Bundle> cb = new AccountManagerCallback<Bundle>() {
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        final Bundle result = future.getResult();
                        final String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                        final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                        final Intent authIntent = result.getParcelable(AccountManager.KEY_INTENT);

                        if (accountName != null && authToken != null) {
                            final SharedPreferences.Editor editor = settings.edit();
                            editor.putString(PREF_TOKEN, authToken);
                            editor.commit();
                            callback.putExtra("token", authToken);
                            callback.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(callback);
                        } else if (authIntent != null) {
                            context.startActivityForResult(authIntent, REQUEST_CODE_AUTH);
                        } else {
                            Log.e(PREFS_NAME, "AccountManager was unable to obtain an authToken.");
                        }
                    } catch (Exception e) {
                        Log.e(PREFS_NAME, "Auth Error", e);
                    }
                }
            };
            manager.invalidateAuthToken(accessToken);
           manager.getAccountManager().getAuthToken(account, GoogleAccountManager.ACCOUNT_TYPE, true, cb, null);
        }

    }
}
