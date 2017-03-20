package ee.tartu.jpg.minuposka.ui.animation;

import android.view.View;
import android.widget.LinearLayout;

/**
 * A wrapper for expanding selected schedules in the timetables.
 */
public class ViewWeightAnimationWrapper {

    private View view;

    public ViewWeightAnimationWrapper(View view) {
        if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            this.view = view;
        } else {
            throw new IllegalArgumentException("The view should have LinearLayout as parent");
        }
    }

    @SuppressWarnings("unused")
    public void setWeight(float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.weight = weight;
        view.setLayoutParams(params);
    }

    public float getWeight() {
        return ((LinearLayout.LayoutParams) view.getLayoutParams()).weight;
    }
}