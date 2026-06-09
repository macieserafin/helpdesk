package macieserafin.pl.helpdesk.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TicketEventService {
    private static final long TIMEOUT_MILLIS = 30L * 60L * 1000L;

    private final Map<Long, List<SseEmitter>> emittersByTicketId = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long ticketId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MILLIS);
        emittersByTicketId.computeIfAbsent(ticketId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(ticketId, emitter));
        emitter.onTimeout(() -> removeEmitter(ticketId, emitter));
        emitter.onError(ignored -> removeEmitter(ticketId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("ticketId", ticketId, "timestamp", Instant.now().toString())));
        } catch (IOException exception) {
            removeEmitter(ticketId, emitter);
        }

        return emitter;
    }

    public void notifyTicketChanged(Long ticketId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendTicketChanged(ticketId);
                }
            });
            return;
        }

        sendTicketChanged(ticketId);
    }

    private void sendTicketChanged(Long ticketId) {
        List<SseEmitter> emitters = emittersByTicketId.get(ticketId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ticket-change")
                        .data(Map.of("ticketId", ticketId, "timestamp", Instant.now().toString())));
            } catch (IOException | IllegalStateException exception) {
                removeEmitter(ticketId, emitter);
            }
        }
    }

    private void removeEmitter(Long ticketId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByTicketId.get(ticketId);
        if (emitters == null) {
            return;
        }

        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByTicketId.remove(ticketId, emitters);
        }
    }
}
