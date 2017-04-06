package com.gmail.dleemcewen.tandemfieri.EventListeners;

import com.gmail.dleemcewen.tandemfieri.Interfaces.ISubscriber;

import java.util.EventListener;
import java.util.List;

/**
 * SubscriberBuilderCompleteListener defines the interface for the event that
 * fires when subscriber builds are completed
 */

public interface SubscriberBuilderCompleteListener<T extends ISubscriber> extends EventListener {
    void onBuildComplete(List<ISubscriber> subscribers);
}
