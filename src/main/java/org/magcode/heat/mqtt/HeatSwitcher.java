package org.magcode.heat.mqtt;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.magcode.heat.Room;

public class HeatSwitcher implements Runnable {
	private MqttClient mqttClient;
	private Map<String, Room> rooms;
	private int interval;
	private final ScheduledExecutorService scheduler;
	private static Logger logger = LogManager.getLogger(HeatSwitcher.class);

	public HeatSwitcher(Map<String, Room> rooms, int interval, MqttClient mqttClient) {
		this.rooms = rooms;
		this.mqttClient = mqttClient;
		this.scheduler = Executors.newScheduledThreadPool(rooms.size());
		this.interval = interval;
	}

	@Override
	public void run() {
		for (Entry<String, Room> entry : rooms.entrySet()) {
			Room room = entry.getValue();
			Float actTemp = room.getActTemp();
			Float targetTemp = room.getTargetTemp();
			float diff = targetTemp - actTemp;
			int time = getTimeForDiff(diff);
			logger.debug("Cyclic check: room {} actual temp {}, target temp {}, timeForDiff {}", room.getName(),
					room.getActTemp(), room.getTargetTemp(), time);

			if (time > 0) {
				// enable relay
				MqttMessage message = new MqttMessage();
				message.setPayload("1".getBytes());
				try {
					this.mqttClient.publish(room.getTopSwitch(), message);
				} catch (MqttException e) {
					logger.error("Error during switch ON", e);
				}
				if (time < interval) {
					// schedule OFF Switch in xx minutes when we do not need full time heating
					@SuppressWarnings("unused")
					ScheduledFuture<?> countdown = scheduler.schedule(new HeatReleaser(mqttClient, room), time,
							TimeUnit.MINUTES);
				}
			} else {
				// disable relay
				MqttMessage message = new MqttMessage();
				message.setPayload("0".getBytes());
				try {
					this.mqttClient.publish(room.getTopSwitch(), message);
				} catch (MqttException e) {
					logger.error("Error during switch OFF", e);
				}
			}
		}
	}

	private int getTimeForDiff(Float diff) {
		if (diff >= 2) {
			return interval;
		}
		if (diff >= 1) {
			return interval / 2;
		}
		if (diff >= 0.5) {
			return interval / 3;
		}
		if (diff >= 0.1) {
			return interval / 4;
		}
		return 0;
	}
}