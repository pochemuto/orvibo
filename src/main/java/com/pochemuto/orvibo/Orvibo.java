package com.pochemuto.orvibo;

import com.pochemuto.orvibo.api.OrviboApi;
import com.pochemuto.orvibo.api.message.DiscoveryCommand;
import com.pochemuto.orvibo.api.message.DiscoveryResponse;
import com.pochemuto.orvibo.api.message.MacAddress;
import com.pochemuto.orvibo.api.message.PowerCommand;
import com.pochemuto.orvibo.api.message.PowerResponse;
import com.pochemuto.orvibo.api.message.SubscribeCommand;

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
 * Orvibo Devices API
 * @author Alexander Kramarev (pochemuto@gmail.com)
 * @date 12.11.2016
 */
@Slf4j
public class Orvibo {

    private final OrviboApi api;

    private final Map<Device, Instant> devices = new ConcurrentHashMap<>();

    private final Map<MacAddress, CompletableFuture<Boolean>> switchFutures = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Orvibo() {
        try {
            api = new OrviboApi();
            api.onDiscovery(this::onDiscovery);
            api.onPower(this::onPower);
            api.init();

            scheduler.scheduleWithFixedDelay(this::clean, 5, 5, TimeUnit.MINUTES);
            scheduler.scheduleWithFixedDelay(this::discover, 0, 30, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.error("Api initialization error");
            throw new RuntimeException(ex);
        }
    }

    private void onPower(PowerResponse powerResponse) {
        Device device = new Device(powerResponse.getMacAddress());
        device.setOn(powerResponse.isOn());
        deviceFound(device, "power");
        CompletableFuture<Boolean> future = switchFutures.remove(device.getMacAddress());
        if (future != null) {
            future.complete(device.isOn());
        }
    }

    private void onDiscovery(DiscoveryResponse response) {
        Device device = new Device(response.getMacAddress());
        device.setOn(response.isOn());
        api.send(new SubscribeCommand(device.getMacAddress()));
        deviceFound(device, "discovery");
    }

    private void deviceFound(Device device, String source) {
        if (device.getMacAddress().isEmpty()) {
            return;
        }
        log.debug("Device found: " + device + " via " + source);
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

    /**
     * Get all found devices.
     * It doesn't send discovery command, so result wight be empty if no one socket responded yet.
     * @return list of known devices
     */
    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>(this.devices.keySet());
        devices.sort(Comparator.comparing(Device::getMacAddress));
        return devices;
    }

    /**
     * Set power state for specific device
     * @param mac device mac address
     * @param isOn power state
     * @return future
     */
    public CompletableFuture<Boolean> setPower(MacAddress mac, boolean isOn) {
        PowerCommand command = new PowerCommand(mac);
        command.setOn(isOn);
        log.info("Setting power for " + mac + " to " + stringState(isOn));
        return switchFutures.computeIfAbsent(mac, d -> {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            api.send(command);
            scheduler.schedule(() -> {
                if (future.isDone()) {
                    future.cancel(false);
                }
                switchFutures.remove(d);
            }, 5, TimeUnit.SECONDS);
            return future;
        });
    }

    /**
     * Set power state for specific device
     * @param id device index in device list, see {@link #getDevices()}
     * @param isOn power state
     * @return future
     */
    public CompletableFuture<Boolean> setPower(int id, boolean isOn) {
        Device device = getDevices().get(id);
        return setPower(device.getMacAddress(), isOn);
    }

    /**
     * Toggle power state for specific device
     * @param mac device mac address
     * @return future
     */
    public CompletableFuture<Boolean> toggle(MacAddress mac) {
        return devices.keySet().stream()
                .filter(d -> d.getMacAddress().equals(mac))
                .findAny()
                .map(d -> setPower(d.getMacAddress(), !d.isOn()))
                .orElse(CompletableFuture.completedFuture(false));
    }

    /**
     * Toggle power state for specific device
     * @param id device index in device list, see {@link #getDevices()}
     * @return future
     */
    public CompletableFuture<Boolean> toggle(int id) {
        Device device = getDevices().get(id);
        return toggle(device.getMacAddress());
    }

    /**
     * Interactive terminal. Following commands are supported:
     * - exit or quit
     * - list
     * - toggle device_id
     * @param args arguments
     * @throws InterruptedException on interrupt blocking request
     */
    public static void main(String... args) throws InterruptedException {
        Orvibo orvibo = new Orvibo();
        Scanner scanner = new Scanner(System.in);
        loop:
        while (true) {
            try {
                System.out.print("> ");
                String command = scanner.nextLine();
                List<Device> devices = orvibo.getDevices();
                String[] arguments = command.split("\\s+");
                switch (arguments[0]) {
                    case "exit":
                    case "quit":
                        orvibo.shutdown();
                        System.out.println("exited by user request");
                        break loop;
                    case "list":
                        for (int i = 0; i < devices.size(); i++) {
                            Device device = devices.get(i);
                            System.out.println(i + ": " + device.getMacAddress() + ": " + stringState(device.isOn()));
                        }
                        break;
                    case "toggle":
                        int deviceIndex = Integer.parseInt(arguments[1]);
                        orvibo.toggle(deviceIndex).thenAccept(state -> System.out.println("done"));
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

    /**
     * Shutdown api.
     * @throws InterruptedException on interrupt
     */
    public void shutdown() throws InterruptedException {
        api.shutdown();
        scheduler.shutdown();
        scheduler.awaitTermination(3, TimeUnit.SECONDS);
    }
}
