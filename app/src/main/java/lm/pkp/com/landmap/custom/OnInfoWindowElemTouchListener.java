package lm.pkp.com.landmap.custom;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.google.android.gms.maps.model.Marker;

public abstract class OnInfoWindowElemTouchListener implements OnTouchListener {

    private final View view;
    private final Handler handler = new Handler();

    private Marker marker;
    private boolean pressed = false;

    public OnInfoWindowElemTouchListener(View view) {
        this.view = view;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean onTouch(View vv, MotionEvent event) {
        if (0 <= event.getX() && event.getX() <= view.getWidth() && 0 <= event.getY() && event.getY() <= view.getHeight()) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startPress();
                    break;

                case MotionEvent.ACTION_UP:
                    handler.postDelayed(runnable, 150);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    endPress();
                    break;
                default:
                    break;
            }
        } else {
            endPress();
        }
        return false;
    }

    private void startPress() {
        if (!pressed) {
            pressed = true;
            handler.removeCallbacks(runnable);
            if (marker != null)
                marker.showInfoWindow();
        }
    }


    private boolean endPress() {
        if (pressed) {
            this.pressed = false;
            handler.removeCallbacks(runnable);
            if (marker != null)
                marker.showInfoWindow();
            return true;
        } else
            return false;
    }



    private final Runnable runnable = new Runnable() {
        public void run() {
            if (endPress()) {
                onClickConfirmed(view, marker);
            }
        }
    };


    /**
     * This is called after a successful click
     */
    protected abstract void onClickConfirmed(View v, Marker marker);


}