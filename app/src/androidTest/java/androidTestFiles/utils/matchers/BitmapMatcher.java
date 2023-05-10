package androidTestFiles.utils.matchers;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class BitmapMatcher extends TypeSafeMatcher<View> {

    private final Bitmap expectedBitmap;

    public BitmapMatcher(Bitmap expectedBitmap) {
        super(View.class);
        this.expectedBitmap = expectedBitmap;
    }

    @Override
    protected boolean matchesSafely(View target) {

        if (!(target instanceof ImageView)){
            return false;
        }

        ImageView imageView = (ImageView) target;
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable){
            BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bmd.getBitmap();
            return bitmap.sameAs(expectedBitmap);
        }
        else{
            return false;
        }


    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with bitmap");
        description.appendValue(expectedBitmap);
    }
}
