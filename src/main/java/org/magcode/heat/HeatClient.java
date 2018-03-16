package org.magcode.heat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.magcode.heat.mqtt.HeatSwitcher;
import org.magcode.heat.mqtt.MqttSubscriber;

public class HeatClient {
	private static Map<String, Room> rooms;
	private static String mqttServer;
	private static int interval = 60;
	private static int minimumCycle = 5;
	private static MqttClient mqttClient;
	private static Logger logger = LogManager.getLogger(HeatClient.class);

	public static void main(String[] args) throws Exception {
		logger.info("Started");
		rooms = new HashMap<String, Room>();
		readProps();

		// connect to MQTT broker
		startMQTTClient();

		// run scheduler
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable heatSwitcher = new HeatSwitcher(rooms, interval, mqttClient);
		logger.info("Scheduling for {} minutes", interval);
		ScheduledFuture<?> heatSwitcherFuture = executor.scheduleAtFixedRate(heatSwitcher, 0, interval,
				TimeUnit.MINUTES);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Logger logger2 = LogManager.getLogger("shutdown");
				try {
					mqttClient.disconnect();
					logger2.info("Disconnected from MQTT server");
					heatSwitcherFuture.cancel(true);
					((LifeCycle) LogManager.getContext()).stop();
				} catch (MqttException e) {
					logger2.error("Error during shutdown", e);
				}
			}
		});
	}

	private static void readProps() {
		Properties props = new Properties();
		InputStream input = null;

		try {
			File jarPath = new File(HeatClient.class.getProtectionDomain().getCodeSource().getLocation().getPath());
			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			String filePath = propertiesPath + "/heat.properties";
			logger.info("Loading properties from {}", filePath);

			input = new FileInputStream(filePath);
			props.load(input);

			interval = Integer.parseInt(props.getProperty("interval", "30"));
			minimumCycle = Integer.parseInt(props.getProperty("minimumCycle", "5"));
			mqttServer = props.getProperty("mqttServer", "tcp://localhost");
			Enumeration<?> e = props.propertyNames();

			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				for (int i = 1; i < 11; i++) {
					if (key.equals("heat" + i + ".name")) {
						Room one = new Room();
						one.setTopActTemp((props.getProperty("heat" + i + ".topActTemp")));
						one.setTopTargetTemp((props.getProperty("heat" + i + ".topTargetTemp")));
						one.setTopSwitch((props.getProperty("heat" + i + ".topSwitch")));
						one.setName(props.getProperty("heat" + i + ".name"));
						one.setInterval(interval);
						one.setMinimumCycle(minimumCycle);

						String propOffset = "heat" + i + ".timing.offset";
						if (props.containsKey(propOffset)) {
							one.setOffset(Integer.parseInt(propOffset));
						}
						String propFactor = "heat" + i + ".timing.factor";
						if (props.containsKey(propFactor)) {
							one.setFactor(Integer.parseInt(propFactor));
						}

						rooms.put(one.getName(), one);

					}
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Failed to close file", e);
				}
			}
		}
	}

	private static void startMQTTClient() throws MqttException {
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("Failed to get hostname", e);
		}
		mqttClient = new MqttClient(mqttServer, "client-for-heat-on-" + hostName);
		MqttConnectOptions connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		connOpt.setAutomaticReconnect(true);
		mqttClient.setCallback(new MqttSubscriber(rooms));
		mqttClient.connect(connOpt);
		for (Entry<String, Room> entry : rooms.entrySet()) {
			Room room = entry.getValue();
			mqttClient.subscribe(room.getTopActTemp());
			mqttClient.subscribe(room.getTopTargetTemp());
			logger.info("Handling room '{}' with topics {} and {}, switch: {}", room.getName(), room.getTopActTemp(),
					room.getTopTargetTemp(), room.getTopSwitch());
		}
		logger.info("Connected to MQTT broker.");
	}
}