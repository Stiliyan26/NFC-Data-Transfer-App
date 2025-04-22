package com.pmu.nfc_data_transfer_app.util;

import androidx.lifecycle.Observer;

public class Event<T> {

    private T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        if (content == null) {
            throw new IllegalArgumentException("null values in Event are not allowed.");
        }

        this.content = content;
    }

    /**
     * Returns the content and prevents its use again.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }

    /**
     * An Observer for Event, simplifying the pattern of checking if the Event content has
     * already been handled.
     */
    public static class EventObserver<T> implements Observer<Event<T>> {
        private final OnEventUnhandledContent<T> onEventUnhandledContent;

        public EventObserver(OnEventUnhandledContent<T> onEventUnhandledContent) {
            this.onEventUnhandledContent = onEventUnhandledContent;
        }

        @Override
        public void onChanged(Event<T> event) {
            if (event != null) {
                T content = event.getContentIfNotHandled();

                if (content != null) {
                    onEventUnhandledContent.onEventUnhandled(content);
                }
            }
        }

        public interface OnEventUnhandledContent<T> {
            void onEventUnhandled(T t);
        }
    }
}
