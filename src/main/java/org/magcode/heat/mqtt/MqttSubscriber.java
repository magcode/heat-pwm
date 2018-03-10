package org.magcode.heat.mqtt;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.magcode.heat.Room;

public class MqttSubscriber implements MqttCallback {
	private Map<String, Room> rooms;
	private static Logger logger = LogManager.getLogger(MqttSubscriber.class);

	public MqttSubscriber(Map<String, Room> rooms) {
		this.rooms = rooms;
	}

	@Override
	public void connectionLost(Throwable cause) {

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {

		for (Entry<String, Room> entry : rooms.entrySet()) {
			Room room = entry.getValue();
			if (topic.equals(room.getTopActTemp())) {
				Float actTemp = Float.parseFloat(message.toString());
				room.setActTemp(actTemp);
				logger.trace("room {} has now actual temp {}", room.getName(), room.getActTemp());
			}
			if (topic.equals(room.getTopTargetTemp())) {
				Float targetTemp = Float.parseFloat(message.toString());
				room.setTargetTemp(targetTemp);
				logger.trace("room {} has now target temp {}", room.getName(), room.getTargetTemp());
			}
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}