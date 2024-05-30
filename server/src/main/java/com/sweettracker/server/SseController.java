package com.sweettracker.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {

    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();


    // 응답 타입은 항상 MediaType.TEXT_EVENT_STREAM_VALUE
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(String id) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        emitter.onTimeout(() -> {
            log.info("server sent event timed out : id={}", id);
            emitter.complete();
        });

        // complete() 를 호출할 때 실행
        emitter.onCompletion(() -> {
            emitterMap.remove(id);
            log.info("disconnected id={}", id);
        });

        emitterMap.put(id, emitter);
        return ResponseEntity.ok(emitter);
    }

    @PostMapping(path = "/complete", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void complete(String id) {
        SseEmitter sseEmitter = emitterMap.get(id);
        sseEmitter.complete();
    }

    @PostMapping(path = "/send")
    public ResponseEntity<Void> send(@RequestBody EventPayload eventPayload) {
        if(emitterMap.isEmpty()) {
            log.info("streamer is null");
            return ResponseEntity.ok().build();
        }
        emitterMap.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("broadcast event")
                    .id("broadcast event 1")
                    .reconnectTime(RECONNECTION_TIMEOUT)
                    .data(eventPayload, MediaType.APPLICATION_JSON));
                log.info("message send - {}", eventPayload);
            } catch (IOException e) {
                log.error("fail to send emitter id={}, {}", id, e.getMessage());
            }
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public Set<String> getStreamUserList() {
        return emitterMap.keySet();
    }
}
