package fctreddit.impl.server;

import java.io.IOException;

public class DiscoveryHolder {
    public static final Discovery INSTANCE;

    static {
        try {
            INSTANCE = new Discovery(Discovery.DISCOVERY_ADDR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        INSTANCE.start();
    }
}
