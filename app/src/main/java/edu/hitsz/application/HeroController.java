package edu.hitsz.application;

import android.view.MotionEvent;
import android.view.View;
import edu.hitsz.aircraft.HeroAircraft;

public class HeroController implements View.OnTouchListener {
    private final HeroAircraft heroAircraft;
    private final int gameWidth;
    private final int gameHeight;

    public HeroController(HeroAircraft heroAircraft, int gameWidth, int gameHeight) {
        this.heroAircraft = heroAircraft;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
            return false;
        }

        int viewWidth = Math.max(1, view.getWidth());
        int viewHeight = Math.max(1, view.getHeight());

        int x = (int) (motionEvent.getX() * gameWidth / (float) viewWidth);
        int y = (int) (motionEvent.getY() * gameHeight / (float) viewHeight);

        x = Math.max(0, Math.min(gameWidth, x));
        y = Math.max(0, Math.min(gameHeight, y));

        heroAircraft.setLocation(x, y);
        return true;
    }
}
