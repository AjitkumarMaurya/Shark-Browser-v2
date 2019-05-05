package ajit.browser.lightning.content.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class SquaredLayout extends FrameLayout {

    public SquaredLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int squareMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);

        super.onMeasure(squareMeasureSpec, squareMeasureSpec);
    }
}
