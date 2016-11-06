package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.OrviboApi;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.MacAddress;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 04.11.2016
 */
@Slf4j
public class Orvibo {
    private final OrviboApi api;

    private static final int DISCOVERY_TIMEOUT = 300;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

    private final Set<Device> knownDevices = ConcurrentHashMap.newKeySet();

    public static void main(String... args) throws Exception {
        Orvibo orvibo = new Orvibo();
        orvibo.init();
        orvibo.discovery(MacAddress.fromString("accf238d9b70"))
                .thenAccept(System.out::println);
    }

    public Orvibo() {
        api = new OrviboApi();
        api.onDiscovery(this::onDiscovery);
    }

    public CompletableFuture<Set<Device>> discovery() {
        return discovery(null);
    }

    public CompletableFuture<Set<Device>> discovery(MacAddress mac) {
        knownDevices.clear();
        DiscoveryCommand discoveryCommand = new DiscoveryCommand();
        discoveryCommand.setMacAddress(mac);
        api.send(discoveryCommand);
        return delayed(this::getKnownDevices, DISCOVERY_TIMEOUT);
    }

    private <T> CompletableFuture<T> delayed(Supplier<T> resultSupplier, int delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.schedule(() -> future.complete(resultSupplier.get()), delay, TimeUnit.MILLISECONDS);
        return future;
    }

    private Set<Device> getKnownDevices() {
        return knownDevices;
    }

    private void onDiscovery(DiscoveryResponse response) {
        if (response.getMac().isEmpty()) {
            log.trace("empty device found");
            return;
        }
        Device device = new Device(response.getMac());
        device.setOn(response.isOn());
        knownDevices.add(device);
    }

    public void init() throws Exception {
        api.init();
    }
}
