package com.example.no_rona;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.test.espresso.matcher.BoundedMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Matchers to assert the contents of TextView error texts.
 */
public final class ErrorTextMatchers {

    private ErrorTextMatchers() {
        // do not instantiate
    }

    /**
     * Returns a matcher that matches {@link TextView}s based on text value.
     *
     * @param text {@link String} with text to match
     */
    @NonNull
    public static Matcher<View> withErrorText(final String text) {
        return withErrorText(Matchers.is(text));
    }

    /**
     * Returns a matcher that matches {@link TextView}s based on text property value.
     *
     * @param stringMatcher {@link Matcher} of {@link String} with text to match
     */
    @NonNull
    public static Matcher<View> withErrorText(final Matcher<String> stringMatcher) {

        return new BoundedMatcher<View, TextView>(TextView.class) {

            @Override
            public void describeTo(final Description description) {
                description.appendText("with error text: ");
                stringMatcher.describeTo(description);
            }

            // logic of how we are matching is returned in this method
            @Override
            public boolean matchesSafely(final TextView textView) {
                return stringMatcher.matches(textView.getError().toString());
            }
        };
    }

    /**
     * Returns a matcher that matches a descendant of {@link TextView} that is displaying the error
     * string associated with the given resource id.
     *
     * @param resourceId the string resource the text view is expected to hold.
     */
    @NonNull
    public static Matcher<View> withErrorText(@StringRes final int resourceId) {

        return new BoundedMatcher<View, TextView>(TextView.class) {
            private String resourceName = null;
            private String expectedText = null;

            @Override
            public void describeTo(final Description description) {
                description.appendText("with error text from resource id: ");
                description.appendValue(resourceId);
                if (null != resourceName) {
                    description.appendText("[");
                    description.appendText(resourceName);
                    description.appendText("]");
                }
                if (null != expectedText) {
                    description.appendText(" value: ");
                    description.appendText(expectedText);
                }
            }

            @Override
            public boolean matchesSafely(final TextView textView) {
                if (null == expectedText) {
                    try {
                        expectedText = textView.getResources().getString(resourceId);
                        resourceName = textView.getResources().getResourceEntryName(resourceId);
                    } catch (Resources.NotFoundException ignored) {
                        // view could be from a context unaware of the resource id
                    }
                }
                return null != expectedText && expectedText.equals(textView.getError());
            }
        };
    }
}
