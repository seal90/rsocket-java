package io.rsocket.transport.netty;

import io.rsocket.core.RSocketServer;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.test.PingHandler;
import io.rsocket.transport.netty.server.TcpServerTransport;

public final class UdsPongServer {
    private static final boolean IS_RESUME =
            Boolean.valueOf(System.getProperty("RSOCKET_TEST_RESUME", "false"));
    private static final String UDS_ADDRESS = System.getProperty("RSOCKET_UDS_ADDRESS", "/tmp/test.sock");

    public static void main(String... args) {
        System.out.println("Starting UDS ping-pong server");
        System.out.println("address: " + UDS_ADDRESS);
        System.out.println("resume enabled: " + IS_RESUME);

        RSocketServer server = RSocketServer.create(new PingHandler());
        if (IS_RESUME) {
            server.resume(new Resume());
        }
        server
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .bind(TcpServerTransport.create(UDS_ADDRESS))
                .block()
                .onClose()
                .block();
    }
}
