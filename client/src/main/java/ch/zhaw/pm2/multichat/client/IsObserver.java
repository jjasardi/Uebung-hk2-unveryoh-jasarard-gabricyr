package ch.zhaw.pm2.multichat.client;

/**
 * basic interface for beeing an observer
 * @author jasarard
 *
 */
public interface IsObserver {
    /**
     * method is always called when an observed object changes
     */
    void update();
}
