package fr.xebia.usiquizz.core.game;


public abstract class GameCallback<T> {
    public abstract void callCompleted(T res);
}
