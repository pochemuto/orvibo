package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.OrviboApi;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.PowerCommand;
import com.pochemuto.orvibo.api.message.SubscribeCommand;
import com.pochemuto.orvibo.api.message.SubscribeResponse;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
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

    private final ConcurrentHashMap<MacAddress, CompletableFuture<Device>> subscribeFutures = new ConcurrentHashMap<>();

    private static MacAddress mineMac() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
            return new MacAddress(networkInterface.getHardwareAddress());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String... args) throws Exception {
        Orvibo orvibo = new Orvibo();
        orvibo.init();
        MacAddress socket = MacAddress.fromString("ac cf 23 8d 9b 70");
        PowerCommand command = new PowerCommand(socket);
        while (true) {
            orvibo.subscribe(socket).thenAccept(
                    s -> {
                        command.setOn(!command.isOn());
                        orvibo.api.send(command);
                    }
            ).get();
            Thread.sleep(2000);
        }
    }

    public Orvibo() {
        api = new OrviboApi();
        api.onDiscovery(this::onDiscovery);
        api.onSubscribe(this::onSubscribe);
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

    private CompletableFuture<Device> subscribe(MacAddress mac) {
        return subscribeFutures.computeIfAbsent(mac, m -> {
            SubscribeCommand subscribeCommand = new SubscribeCommand(mac);
            CompletableFuture<Device> f = new CompletableFuture<>();
            api.send(subscribeCommand);
            return f;
        });
    }

    private <T> CompletableFuture<T> delayed(Supplier<T> resultSupplier, int delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executor.schedule(() -> future.complete(resultSupplier.get()), delay, TimeUnit.MILLISECONDS);
        return future;
    }

    private Set<Device> getKnownDevices() {
        return knownDevices;
    }

    private void onSubscribe(SubscribeResponse subscribeResponse) {
        MacAddress mac = subscribeResponse.getMacAddress();
        CompletableFuture<Device> future = subscribeFutures.remove(mac);
        if (future != null) {
            future.complete(new Device(mac));
        }
    }

    private void onDiscovery(DiscoveryResponse response) {
        if (response.getMacAddress().isEmpty()) {
            log.trace("empty device found");
            return;
        }
        Device device = new Device(response.getMacAddress());
        device.setOn(response.isOn());
        knownDevices.add(device);
    }

    public void init() throws Exception {
        api.init();
    }
}
