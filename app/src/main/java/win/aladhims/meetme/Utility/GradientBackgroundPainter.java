package win.aladhims.meetme.Utility;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;

import java.util.Random;

/**
 * Created by Aladhims on 06/05/2017.
 */

public class GradientBackgroundPainter {

    private static final int MIN = 4000;
    private static final int MAX = 5000;

    private final Random random;
    private final Handler handler;
    private final View target;
    private final int[] drawables;
    private final Context context;

    public GradientBackgroundPainter(@NonNull View target, int[] drawables) {
        this.target = target;
        this.drawables = drawables;
        random = new Random();
        handler = new Handler();
        context = target.getContext().getApplicationContext();
    }

    private void animate(final int firstDrawable, int secondDrawable, final int duration) {
        if (secondDrawable >= drawables.length) {
            secondDrawable = 0;
        }
        final Drawable first = ContextCompat.getDrawable(context, drawables[firstDrawable]);
        final Drawable second = ContextCompat.getDrawable(context, drawables[secondDrawable]);

        final TransitionDrawable transitionDrawable =
                new TransitionDrawable(new Drawable[] { first, second });

        target.setBackgroundDrawable(transitionDrawable);

        transitionDrawable.setCrossFadeEnabled(false);

        transitionDrawable.startTransition(duration);

        final int localSecondDrawable = secondDrawable;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                animate(localSecondDrawable, localSecondDrawable + 1, randInt(MIN, MAX));
            }
        }, duration);
    }

    public void start() {
        final int duration = randInt(MIN, MAX);
        animate(0, 1, duration);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private int randInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }
}
