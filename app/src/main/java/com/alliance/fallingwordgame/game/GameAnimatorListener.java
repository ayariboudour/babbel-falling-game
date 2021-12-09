package com.alliance.fallingwordgame.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public class GameAnimatorListener extends AnimatorListenerAdapter {
    private GameEngine gameEngine;

    public GameAnimatorListener(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        super.onAnimationRepeat(animation);
        gameEngine.increaseNoAnswerCounter();
        gameEngine.setWord();
    }
}
