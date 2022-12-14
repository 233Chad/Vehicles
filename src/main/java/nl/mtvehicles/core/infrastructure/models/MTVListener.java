package nl.mtvehicles.core.infrastructure.models;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;

/**
 * Abstract class for the plugin's listeners
 */
public abstract class MTVListener implements Listener {

    /**
     * The event the listener is listening to
     */
    protected Event event;
    /**
     * Player of this event
     */
    protected @Nullable Player player;

    private MTVEvent api;

    protected MTVListener(){
        this.api = null;
    }

    protected MTVListener(MTVEvent api){
        this.api = api;
    }

    /**
     * Check whether the event has been cancelled via MTVehicles API.
     * @return True if event is cancelled
     */
    protected boolean isCancelled(){
        if (event == null) throw new NullPointerException("Cannot check if event is cancelled if event is null.");

        return getAPI().isCancelled();
    }

    protected void setAPI(MTVEvent event){
        this.api = event;
    }

    protected MTVEvent getAPI(){
        if (api == null) throw new NullPointerException("Event API not specified for this listener.");

        return api;
    }

    protected void callAPI(){
        if (api == null) throw new NullPointerException("Event API not specified for this listener.");

        api.setPlayer(player);
        api.call();
    }

    /**
     * Call the event with a custom player
     * @param player Custom player (may be null if no player is specified)
     *
     * @throws NullPointerException If API is not specified for the listener (see {@link #setAPI(MTVEvent)})
     */
    protected void callAPI(@Nullable Player player) throws NullPointerException {
        if (api == null) throw new NullPointerException("Event API not specified for this listener.");

        api.setPlayer(player);
        api.call();
    }
}
