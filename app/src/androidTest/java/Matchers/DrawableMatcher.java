package Matchers;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class DrawableMatcher extends TypeSafeMatcher<View> {

    private final int expectedId;
    private String resourceName;

    public DrawableMatcher(int resourceId) {
        super(View.class);
        this.expectedId = resourceId;
    }

    @Override
    protected boolean matchesSafely(View target) {
        if (!(target instanceof ImageView)){
            return false;
        }
        ImageView imageView = (ImageView) target;
        if (expectedId < 0){
            return imageView.getDrawable() == null;
        }
        Resources resources = target.getContext().getResources();
        Drawable expectedDrawable = resources.getDrawable(expectedId);
        resourceName = resources.getResourceEntryName(expectedId);
        if (expectedDrawable == null) {
            return false;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable){
            BitmapDrawable bmd = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bmd.getBitmap();
            BitmapDrawable expected = (BitmapDrawable) expectedDrawable;
            Bitmap otherBitmap = expected.getBitmap();
            return bitmap.sameAs(otherBitmap);
        }
        else{
            //If it's a vector drawable, we need to manually have set a tag with the resource ID
            int resourceID = (int) imageView.getTag();
            return resourceID == expectedId;
        }


    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with drawable from resource id: ");
        description.appendValue(expectedId);
        if (resourceName != null) {
            description.appendText("[");
            description.appendText(resourceName);
            description.appendText("]");
        }
    }
}
