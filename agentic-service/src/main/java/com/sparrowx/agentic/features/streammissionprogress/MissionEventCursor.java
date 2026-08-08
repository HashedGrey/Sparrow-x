package com.sparrowx.agentic.features.streammissionprogress;

import com.sparrowx.agentic.mission.model.MissionProgressEvent;
import com.sparrowx.agentic.mission.model.MissionStatus;
import com.sparrowx.agentic.runtime.store.RuntimeEventStore;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MissionEventCursor implements AutoCloseable {

    private final Deque<MissionProgressEventView> replayEvents;
    private final RuntimeEventStore.EventSubscription subscription;
    private final AtomicBoolean closed;

    private volatile String resumeToken;
    private volatile boolean terminal;

    MissionEventCursor(
            Collection<MissionProgressEvent> replayEvents,
            RuntimeEventStore.EventSubscription subscription,
            String initialResumeToken
    ) {
        this.replayEvents = new ArrayDeque<>();
        this.subscription = subscription;
        this.closed = new AtomicBoolean(false);
        this.resumeToken = normalize(initialResumeToken);

        if (replayEvents != null) {
            for (MissionProgressEvent event : replayEvents) {
                MissionProgressEventView view =
                        MissionProgressEventView.from(event);
                this.replayEvents.addLast(view);

                if (!view.resumeToken().isBlank()) {
                    this.resumeToken = view.resumeToken();
                }

                this.terminal = this.terminal || isTerminal(view.status());
            }
        }
    }

    public Optional<MissionProgressEventView> next(Duration waitTimeout) {
        Objects.requireNonNull(waitTimeout, "waitTimeout");

        if (waitTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "Wait timeout must not be negative."
            );
        }

        MissionProgressEventView replay = replayEvents.pollFirst();
        if (replay != null) {
            updatePosition(replay);

            if (isTerminal(replay.status())) {
                closeSubscription();
            }

            return Optional.of(replay);
        }

        if (closed.get() || terminal || subscription == null) {
            return Optional.empty();
        }

        Optional<MissionProgressEvent> event =
                subscription.next(waitTimeout);

        if (event.isEmpty()) {
            return Optional.empty();
        }

        MissionProgressEventView view =
                MissionProgressEventView.from(event.get());
        updatePosition(view);

        if (isTerminal(view.status())) {
            terminal = true;
            closeSubscription();
        }

        return Optional.of(view);
    }

    public String resumeToken() {
        String subscriptionToken = subscription == null
                ? ""
                : normalize(subscription.resumeToken());

        return subscriptionToken.isBlank()
                ? resumeToken
                : subscriptionToken;
    }

    public boolean terminal() {
        return terminal;
    }

    public boolean closed() {
        return closed.get();
    }

    @Override
    public void close() {
        replayEvents.clear();
        closeSubscription();
    }

    private void updatePosition(MissionProgressEventView event) {
        if (!event.resumeToken().isBlank()) {
            resumeToken = event.resumeToken();
        }

        terminal = terminal || isTerminal(event.status());
    }

    private void closeSubscription() {
        if (closed.compareAndSet(false, true) && subscription != null) {
            subscription.close();
        }
    }

    static boolean isTerminal(MissionStatus status) {
        return status == MissionStatus.COMPLETED
                || status == MissionStatus.FAILED_TERMINAL
                || status == MissionStatus.CANCELLED;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}