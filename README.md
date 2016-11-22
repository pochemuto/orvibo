Orvibo Java API
===============

Allow control your sockets from java code, terminal, or via http.

Using in code
-------------

Add Sonatype public repository:

```groovy
repositories {
    mavenCentral()
    maven {
        url "https://oss.sonatype.org/content/groups/public/"
    }
}
```

and dependency: 

```groovy
compile group: 'com.pochemuto', name: 'orvibo', version: '0.9.0'
```

Basic example:

```java
Orvibo orvibo = new Orvibo();
Thread.sleep(250); // wait for discovery responses

List<Device> devices = orvibo.getDevices();
for (Device device : devices) {
    System.out.println(device.getMacAddress() + " : " + (device.isOn() ? "ON" : "OFF"));
}

if (!devices.isEmpty()) {
    Device mySocket = devices.get(0);
    orvibo.toggle(mySocket.getMacAddress());
    Thread.sleep(5000); // just for example
    orvibo.setPower(mySocket.getMacAddress(), false);
}
```

Basic terminal control
----------------------
```
java -cp orvibo-1.0-SNAPSHOT-all.jar com.pochemuto.orvibo.Orvibo
> list
0: ac cf 23 8d 9b 70: OFF
> toggle 0
done
> exit
```

Http interface
--------------

For using http interface run:
```bash
$ java -jar orvibo-1.0-SNAPSHOT-all.jar > /dev/null &
$ curl http://localhost:4352/list
0 accf238d9b70 OFF
$ curl http://localhost:4352/on/0
$ curl http://localhost:4352/state/0 -s | grep 1 -q && echo ON || echo OFF
ON
$ curl http://localhost:4352/toggle/0
$ curl http://localhost:4352/state/0 -s | grep 1 -q && echo ON || echo OFF
OFF
```
