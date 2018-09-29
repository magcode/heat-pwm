package org.magcode.heat.mqtt;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.magcode.heat.Room;

public class MqttSubscriber implements MqttCallbackExtended {
	private Map<String, Room> rooms;
	private static Logger logger = LogManager.getLogger(MqttSubscriber.class);

	public MqttSubscriber(Map<String, Room> rooms) {
		this.rooms = rooms;
	}

	@Override
	public void connectionLost(Throwable cause) {
		logger.error("MQTT connection lost", cause);
	}

	// @Override
	public void connectComplete(boolean reconnect, java.lang.String serverURI) {
		logger.error("MQTT connection complete. Reconnect: {}", reconnect);
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
			if (StringUtils.isNotBlank(room.getTopEnabled()) && topic.equals(room.getTopEnabled())) {
				String mess = message.toString();
				if (mess.equalsIgnoreCase("ON") || mess.equals("1")) {
					room.setHeatingEnabled(true);
				} else {
					room.setHeatingEnabled(false);
				}
				logger.info("room {} heating is now enabled={}", room.getName(), room.isHeatingEnabled());
			}
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {

	}
}