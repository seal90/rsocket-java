package io.rsocket.transport.netty;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.test.PerfTest;
import io.rsocket.test.PingClient;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.HdrHistogram.Recorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;

@PerfTest
public final class UdsPing {
    private static final int INTERACTIONS_COUNT = 1_000_000_000;
    private static final String UDS_ADDRESS = System.getProperty("RSOCKET_UDS_ADDRESS", "/tmp/test.sock");

    @BeforeEach
    void setUp() {
        System.out.println("Starting ping-pong test (UDS transport)");
        System.out.println("port: " + UDS_ADDRESS);
    }

    @Test
    void requestResponseTest() {
        PingClient pingClient = newPingClient();
        Recorder recorder = pingClient.startTracker(Duration.ofSeconds(1));

        pingClient
                .requestResponsePingPong(INTERACTIONS_COUNT, recorder)
                .doOnTerminate(() -> System.out.println("Sent " + INTERACTIONS_COUNT + " messages."))
                .blockLast();
    }

    @Test
    void requestStreamTest() {
        PingClient pingClient = newPingClient();
        Recorder recorder = pingClient.startTracker(Duration.ofSeconds(1));

        pingClient
                .requestStreamPingPong(INTERACTIONS_COUNT, recorder)
                .doOnTerminate(() -> System.out.println("Sent " + INTERACTIONS_COUNT + " messages."))
                .blockLast();
    }

    @Test
    void requestStreamResumableTest() {
        PingClient pingClient = newResumablePingClient();
        Recorder recorder = pingClient.startTracker(Duration.ofSeconds(1));

        pingClient
                .requestStreamPingPong(INTERACTIONS_COUNT, recorder)
                .doOnTerminate(() -> System.out.println("Sent " + INTERACTIONS_COUNT + " messages."))
                .blockLast();
    }

    private static PingClient newPingClient() {
        return newPingClient(false);
    }

    private static PingClient newResumablePingClient() {
        return newPingClient(true);
    }

    private static PingClient newPingClient(boolean isResumable) {
        RSocketConnector connector = RSocketConnector.create();
        if (isResumable) {
            connector.resume(new Resume());
        }
        Mono<RSocket> rSocket =
                connector
                        .payloadDecoder(PayloadDecoder.ZERO_COPY)
                        .keepAlive(Duration.ofMinutes(1), Duration.ofMinutes(30))
                        .connect(TcpClientTransport.create(UDS_ADDRESS));

        return new PingClient(rSocket);
    }
}
