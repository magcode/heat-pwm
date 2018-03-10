package org.magcode.heat.mqtt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.magcode.heat.Room;

public class HeatReleaser implements Runnable {
	private MqttClient mqttClient;
	private Room room;
	private static Logger logger = LogManager.getLogger(HeatReleaser.class);

	public HeatReleaser(MqttClient mqttClient, Room room) {
		this.mqttClient = mqttClient;
		this.room = room;
	}

	public void run() {
		MqttMessage message = new MqttMessage();
		message.setPayload("0".getBytes());
		try {
			logger.info("Switch OFF for room {}", room.getName());
			this.mqttClient.publish(this.room.getTopSwitch(), message);
		} catch (MqttException e) {
			logger.error("Error during switch OFF", e);
		}
	}
}