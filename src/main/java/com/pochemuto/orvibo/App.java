package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.OrviboApi;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.PowerCommand;
import com.pochemuto.orvibo.api.message.PowerResponse;
import com.pochemuto.orvibo.api.message.SubscribeCommand;
import com.pochemuto.orvibo.api.message.SubscribeResponse;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 12.11.2016
 */
@Slf4j
public class App {

    private final OrviboApi api;

    private final Map<Device, Instant> devices = new ConcurrentHashMap<>();

    private final Map<Device, CompletableFuture<Boolean>> switchFutures = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public App() {
        try {
            api = new OrviboApi();
            api.onDiscovery(this::onDiscovery);
            api.onSubscribe(this::onSubscribe);
            api.onPower(this::onPower);
            api.init();

            scheduler.scheduleWithFixedDelay(this::clean, 5, 5, TimeUnit.MINUTES);
            scheduler.scheduleWithFixedDelay(this::discover, 0, 1, TimeUnit.MINUTES);
        } catch (Exception ex) {
            log.error("Api initialization error");
            throw new RuntimeException(ex);
        }
    }

    private void onPower(PowerResponse powerResponse) {
        Device device = new Device(powerResponse.getMacAddress());
        device.setOn(powerResponse.isOn());
        deviceFound(device);
        CompletableFuture<Boolean> future = switchFutures.remove(device);
        if (future != null) {
            future.complete(device.isOn());
        }
    }

    private void onSubscribe(SubscribeResponse response) {
        Device device = new Device(response.getMacAddress());
        device.setOn(response.isOn());
        deviceFound(device);
    }

    private void onDiscovery(DiscoveryResponse response) {
        Device device = new Device(response.getMacAddress());
        device.setOn(response.isOn());
        api.send(new SubscribeCommand(device.getMacAddress()));
        deviceFound(device);
    }

    private void deviceFound(Device device) {
        if (device.getMacAddress().isEmpty()) {
            return;
        }
        log.debug("Device found: " + device);
        devices.remove(device);
        devices.put(device, Instant.now());
    }

    private void discover() {
        api.send(new DiscoveryCommand());
    }

    private void clean() {
        Instant dead = Instant.now().minus(5, ChronoUnit.MINUTES);
        Iterator<Map.Entry<Device, Instant>> iterator = devices.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Device, Instant> entry = iterator.next();
            if (entry.getValue().isBefore(dead)) {
                iterator.remove();
            }
        }
    }

    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>(this.devices.keySet());
        devices.sort(Comparator.comparing(Device::getMacAddress));
        return devices;
    }

    public CompletableFuture<Boolean> setPower(Device device, boolean isOn) {
        PowerCommand command = new PowerCommand(device.getMacAddress());
        command.setOn(isOn);
        return switchFutures.computeIfAbsent(device, d -> {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            api.send(command);
            return future;
        });
    }

    public static void main(String... args) throws InterruptedException {
        App app = new App();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                String command = scanner.nextLine();
                List<Device> devices = app.getDevices();
                String[] arguments = command.split("\\s+");
                switch (arguments[0]) {
                    case "exit":
                    case "quit":
                        app.shutdown();
                        System.out.println("exited by user request");
                        break;
                    case "list":
                        for (int i = 0; i < devices.size(); i++) {
                            Device device = devices.get(i);
                            System.out.println(i + ": " + device.getMacAddress() + ": " + stringState(device.isOn()));
                        }
                        break;
                    case "toggle":
                        int deviceIndex = Integer.parseInt(arguments[1]);
                        Device selectedDevice = devices.get(deviceIndex);
                        boolean powerOn = selectedDevice.isOn();
                        System.out.println("turning " + stringState(powerOn) + " device "
                                + selectedDevice.getMacAddress());
                        app.setPower(selectedDevice, !powerOn).thenAccept(state -> System.out.println("done"));
                        break;
                }
            } catch (NumberFormatException ex) {
                System.err.println("bad number format");
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.err.println("not enough arguments");
            } catch (IndexOutOfBoundsException ex) {
                System.err.println("not exists device id");
            }
        }
    }

    private static String stringState(boolean isOn) {
        return isOn ? "ON" : "OFF";
    }

    public void shutdown() throws InterruptedException {
        api.shutdown();
        scheduler.shutdown();
        scheduler.awaitTermination(3, TimeUnit.SECONDS);
    }
}
