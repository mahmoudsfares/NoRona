package com.example.no_rona;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class ToastMatcher extends TypeSafeMatcher<Root> {

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }

    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        // if the type is toast
        if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
            // 1- because toasts belong to both windowToken and appToken
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            // 2- return true if it matches that condition
            if (windowToken == appToken) {
                return true;
            }
        }
        return false;
    }
}
