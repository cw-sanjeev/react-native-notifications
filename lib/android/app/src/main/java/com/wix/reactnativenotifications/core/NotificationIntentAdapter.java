package com.wix.reactnativenotifications.core;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.wix.reactnativenotifications.Defs;
import com.wix.reactnativenotifications.core.notification.PushNotificationProps;

public class NotificationIntentAdapter {
    private static final String PUSH_NOTIFICATION_EXTRA_NAME = "pushNotification";

    private Application application;

    @SuppressLint("UnspecifiedImmutableFlag")
    public static PendingIntent createPendingNotificationIntent(Context appContext, PushNotificationProps notification) {
        if (cannotHandleTrampolineActivity(appContext)) {
            Intent intent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
            intent.putExtra(PUSH_NOTIFICATION_EXTRA_NAME, notification.asBundle());
            return PendingIntent.getActivity(appContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            Intent intent = new Intent(appContext, ProxyService.class);
            intent.putExtra(PUSH_NOTIFICATION_EXTRA_NAME, notification.asBundle());
            return PendingIntent.getService(appContext, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);
        }
    }

    public static Intent createAppLaunchIntent(Context appContext, AppLaunchHelper appLaunchHelper, PushNotificationProps notification) {
        Intent intent;
        if (NotificationIntentAdapter.cannotHandleTrampolineActivity(appContext)) {
            intent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
            intent.putExtra(PUSH_NOTIFICATION_EXTRA_NAME, notification.asBundle());
            intent.putExtra(Defs.IS_INTENT_HANDLED, false);
        } else {
            intent = appLaunchHelper.getLaunchIntent(appContext);
            intent.putExtra(PUSH_NOTIFICATION_EXTRA_NAME, notification.asBundle());
            intent.putExtra(Defs.IS_INTENT_HANDLED, false);
        }
        return intent;
    }

    public static boolean cannotHandleTrampolineActivity(Context appContext) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && appContext.getApplicationInfo().targetSdkVersion >= 31;
    }

    public static boolean cannotHandleTrampolineActivity() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public static Bundle extractPendingNotificationDataFromIntent(Intent intent) {
        return intent.getBundleExtra(PUSH_NOTIFICATION_EXTRA_NAME);
    }

    public static boolean canHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle notificationData = cannotHandleTrampolineActivity() ? extractPendingNotificationDataFromIntent(intent) : intent.getExtras();
            return notificationData != null &&
                    (intent.hasExtra(PUSH_NOTIFICATION_EXTRA_NAME) ||
                            notificationData.getString("google.message_id", null) != null) && !intent.hasExtra("isIntentHandled");
        }

        return false;
    }
}
