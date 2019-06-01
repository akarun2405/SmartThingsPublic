/**
 *  Fibaro Heat Controller
 *
 *  Copyright 2017 Artur Draga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Heat Controller", namespace: "ClassicGOD", author: "Artur Draga") {
		capability "Thermostat"
		capability "Temperature Measurement"
		
		command "tempUp"
		command "tempDown"
		command "refreshBattery"
		command "refreshMode"
		command "resetNotification"
		
		fingerprint mfr: "010F", prod: "1301"
		fingerprint deviceId: "0x0806", inClusters:"0x5E,0x98,0x9F,0x55,0x56,0x6C,0x22,0x86,0x8E,0x31,0x40,0x43,0x53,0x59,0x5A,0x7A,0x60,0x71,0x72,0x75,0x80,0x70,0x81,0x73,0x85"
		fingerprint deviceId: "0x0806", inClusters:"0x5E,0x9F,0x55,0x56,0x6C,0x22,0x86,0x8E,0x31,0x40,0x43,0x53,0x59,0x5A,0x7A,0x60,0x71,0x72,0x75,0x80,0x70,0x81,0x73,0x85"
	}

	tiles (scale: 2) {
		multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
			tileAttribute("device.thermostatSetpoint", key: "PRIMARY_CONTROL") {
				attributeState("temp", label:'${currentValue}', unit:"dF", defaultState: true)
			}
			tileAttribute("device.control", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "tempUp")
				attributeState("VALUE_DOWN", action: "tempDown")
			}
			tileAttribute("device.multiStatus", key: "SECONDARY_CONTROL") {
				attributeState("multiStatus", label:'${currentValue}')
			}
			tileAttribute("device.thermostatMode", key: "OPERATING_STATE") {
				attributeState("off", backgroundColor:"#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_off.png")
				attributeState("heat", backgroundColor:"#e86d13")
				attributeState("auto", backgroundColor:"#00a0dc")
			}
		}
		
		standardTile("off", "device.thermostatMode", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Off', action:"off", backgroundColor:"#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_off.png"
			state "off", label:'Off', action:"refreshMode", backgroundColor:"#cccccc", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_off.png"
		}
		standardTile("auto", "device.thermostatMode", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Auto', action:"auto", backgroundColor:"#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_18.png"
			state "auto", label:'Auto', action:"refreshMode", backgroundColor:"#00a0dc", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_18.png"
		}
		standardTile("heat", "device.thermostatMode", decoration: "flat", width: 2, height: 2) {
			state "default", label:'Heat', action:"heat", backgroundColor:"#ffffff", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_max.png"
			state "heat", label:'Heat', action:"refreshMode", backgroundColor:"#e86d13", icon: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/fibaro-heat-controller.src/images/miami_max.png"
		}
		valueTile("notification", "device.notification", decoration: "flat", width: 4, height: 2) {
			state "notification", label: '${currentValue}', action:"resetNotification"
		}
		valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
			state "battery", label:'${currentValue}%\nbattery', action:"refreshBattery"
		}
	}

	preferences {
	
		input (
			title: "Fibaro Heat Controller manual",
			description: "Tap to view the manual.",
			image: "https://manuals.fibaro.com/wp-content/uploads/2017/10/hear_controller.png",
			url: "https://manuals.fibaro.com/content/manuals/en/FGT-001/FGT-001-EN-T-v1.0.pdf",
			type: "href",
			element: "href"
		)
		input (
			title: "2. Additional functions",
			description: "This parameter allows to enable different additional functions of the device.",
			type: "paragraph",
			element: "paragraph"
		)
		input ( name: "function1", title: "Open Window Detector", type: "boolean", required: false, defaultValue: 1)
		input ( name: "function2", title: "Fast Open Window Detector", type: "boolean", required: false, defaultValue: 0 )
		input ( name: "function3", title: "Increase receiver sensitivity (shortens battery life)", type: "boolean", required: false, defaultValue: 0 )
		input ( name: "function4", title: "LED indications when controlling remotely", type: "boolean", required: false, defaultValue: 0 )
		input ( name: "function5", title: "Protect from setting Full ON and Full OFF mode by turning the knob manually", type: "boolean", required: false, defaultValue: 0 )
		
		input ( name: "logging", title: "Logging", type: "boolean", required: false )
	}
}

def tempUp() {
	logging("Executing tempUp()","info")
	Integer currentTemp = device.currentValue("thermostatSetpoint")
	if (currentTemp == null) {
		sendEvent([name: "thermostatSetpoint", value: 10])
	} else if (currentTemp >= 30) {
		sendEvent([name: "thermostatSetpoint", value: 30])
	} else {
		sendEvent([name: "thermostatSetpoint", value: (currentTemp as Integer) + 1])
	}
	runIn(5,"setTemp")
}

def tempDown() {
	logging("Executing tempDown()","info")
	Integer currentTemp = device.currentValue("thermostatSetpoint")
	if (currentTemp == null) {
		sendEvent([name: "thermostatSetpoint", value: 30])
	} else if (currentTemp <= 10) {
		sendEvent([name: "thermostatSetpoint", value: 10])
	} else {
		sendEvent([name: "thermostatSetpoint", value: (currentTemp as Integer) - 1])
	}
	runIn(5,"setTemp")
}

def setTemp(Integer degrees = null) {
	logging("Executing setTemp($degrees)","info")
	def cmds = []
	if (degrees == null) { degrees = device.currentValue("thermostatSetpoint") as Integer }
	cmds << response(encap(zwave.thermostatSetpointV2.thermostatSetpointSet(precision: 1, reserved01: 0, scale: 0, scaledValue: degrees, setpointType: 1, size: 2)))
	sendHubCommand(cmds,1000)
}

def setHeatingSetpoint(degrees) {
	logging("Executing setHeatingSetpoint($degrees)","info")
	sendEvent([name: "thermostatSetpoint", value: degrees])
	sendEvent([name: "heatingSetpoint", value:degrees])
	setTemp(degrees)
}

def off() { setMode('off') }

def auto() { setMode('auto') }

def heat() { setMode('heat') }

def setMode(String mode) {
	logging("Executing setMode($mode)","info")
	def cmds = []
	def valList = [off: 0, auto: 1, heat: 31]
	sendEvent([name: "thermostatMode", value: mode])
	log.debug valList[mode]
	cmds << response(encap(zwave.thermostatModeV2.thermostatModeSet(mode: valList[mode]),1));
	cmds << response(encap(zwave.thermostatModeV2.thermostatModeGet(),1));
	sendHubCommand(cmds,3000)
}

def refreshMode() {
	logging("Executing refreshMode($mode)","info")
	def cmds = []
	sendEvent([name: "thermostatMode", value: null])
	cmds << response(encap(zwave.thermostatModeV2.thermostatModeGet(),1));
	sendHubCommand(cmds,1000)
}

def resetNotification() {
	logging("Executing resetNotification()","info")
	if ( device.currentValue("param3") in ["2","3"] ) {
		sendEvent(name: "notification", value: "Window open!" , displayed: true);
	} else {
		sendEvent(name: "notification", value: "" , displayed: true);
	}
}

def refreshBattery() {
	logging("Executing refreshBattery()","info")
	def cmds = []
	cmds << [zwave.batteryV1.batteryGet(), 1]
	cmds << [zwave.batteryV1.batteryGet(), 2]
	cmds << [zwave.sensorMultilevelV5.sensorMultilevelGet(), 2]
	encapSequence(cmds,3500)
}


//Configuration and synchronization
def updated() {
	logging("Executing updated()","info")
	if ( state.lastUpdated && (now() - state.lastUpdated) < 500 ) return
	def cmds = []
	logging("${device.displayName} - Executing updated()","info")
	runIn(10,"syncStart")
	state.lastUpdated = now()
	configure()
}

def configure() {
	logging("Executing configure()","info")
	def cmds = []
	def currentDate = new Date()
	cmds << response(encap(zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier: 1)))
	cmds << response(encap(zwave.clockV1.clockSet(hour: currentDate.format("H", location.timeZone) as Short, minute: currentDate.format("m", location.timeZone) as Short, weekday: currentDate.format("u", location.timeZone) as Short)))
	cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: 3)))
	
	sendHubCommand(cmds,3000)
}

def calcFnVal() {
	logging("Executing calcFnVal()","info")
	Integer value = 0
	(1..5).each {
		log.debug settings."function${it}"
		if (settings."function${it}" == "true") {
			value = value + 2**(it-1)
		}
	}
	return value
}

private syncStart() {
	boolean syncNeeded = false
	Integer settingValue = null
	parameterMap().each {
		if(settings."$it.key" != null || it.num == 2) {
			if ( it.num == 2 ) {
				settingValue = calcFnVal() as Integer
			} else {
				settingValue = settings."$it.key" as Integer
			}
			if (state."$it.key" == null) { state."$it.key" = [value: null, state: "synced"] } 
			if (state."$it.key".value != settingValue || state."$it.key".state != "synced" ) {
				state."$it.key".value = settingValue
				state."$it.key".state = "notSynced"
				syncNeeded = true
			}
		}
	}
	if ( syncNeeded ) { 
		logging("sync needed.", "info")
		syncNext()
	}
}

private syncNext() {
	logging("Executing syncNext()","info")
	def cmds = []
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.value != null && state."$param.key"?.state in ["notSynced","inProgress"] ) {
			multiStatusEvent("Sync in progress. (param: ${param.num})", true)
			state."$param.key"?.state = "inProgress"
			cmds << response(encap(zwave.configurationV2.configurationSet(configurationValue: intToParam(state."$param.key".value, param.size), parameterNumber: param.num, size: param.size)))
			cmds << response(encap(zwave.configurationV2.configurationGet(parameterNumber: param.num)))
			break
		} 
	}
	if (cmds) { 
		runIn(10, "syncCheck")
		sendHubCommand(cmds,3000)
	} else {
		runIn(1, "syncCheck")
	}
}

private syncCheck() {
	logging("Executing syncCheck()","info")
	def failed = []
	def incorrect = []
	def notSynced = []
	parameterMap().each {
		if (state."$it.key"?.state == "incorrect" ) {
			incorrect << it
		} else if ( state."$it.key"?.state == "failed" ) {
			failed << it
		} else if ( state."$it.key"?.state in ["inProgress","notSynced"] ) {
			notSynced << it
		}
	}
	
	if (failed) {
		multiStatusEvent("Sync failed! Verify parameter: ${failed[0].num}", true, true)
	} else if (incorrect) {
		multiStatusEvent("Sync mismatch! Verify parameter: ${incorrect[0].num}", true, true)
	} else if (notSynced) {
		multiStatusEvent("Sync incomplete! Open settings and tap Done to try again.", true, true)
	} else {
		if (device.currentValue("multiStatus")?.contains("Sync")) { multiStatusEvent("Sync OK.", true, true) }
	}
	runIn(5, "updateSensor")
}

private multiStatusEvent(String statusValue, boolean force = false, boolean display = false) {
	if (!device.currentValue("multiStatus")?.contains("Sync") || device.currentValue("multiStatus") == "Sync OK." || force) {
		sendEvent(name: "multiStatus", value: statusValue, descriptionText: statusValue, displayed: display)
	}
}

private updateSensor() {
	logging("Executing updateSensor()","info")
	if ( device.currentValue("param3") in ["1","3"] ) {
		def tempTemp = ( device.currentValue("temperature") == null ) ? "--" : "${device.currentValue("temperature")}°"
		def tempBatt = ( device.currentValue("sensorBattery") == null ) ? "--" : "${device.currentValue("sensorBattery")}%"
		multiStatusEvent( "$tempTemp | $tempBatt")
	} else {
		multiStatusEvent( "--" )
	}

}

//event handlers related to configuration and sync
def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, ep = null) {
	log.debug cmd
	if ( cmd.parameterNumber == 3 ) {
		sendEvent(name: "param3", value: cmd.scaledConfigurationValue, displayed: false);
		def currentNotification = device.currentValue("notification")
		currentNotification = ( currentNotification != null && currentNotification != "" ) ? currentNotification += "\n" : ""
		switch (cmd.scaledConfigurationValue) {
			case 0..1: sendEvent(name: "notification", value: removeItem(currentNotification, "Window Open!"), displayed: true); break;
			case 2..3: sendEvent(name: "notification", value: currentNotification + "Window Open!", displayed: true); break;
		}
	} else {
		def paramKey = parameterMap().find( {it.num == cmd.parameterNumber } ).key
		logging("Parameter ${paramKey} value is ${cmd.scaledConfigurationValue} expected " + state."$paramKey".value, "info")
		state."$paramKey".state = (state."$paramKey".value == cmd.scaledConfigurationValue) ? "synced" : "incorrect"
		syncNext()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.applicationstatusv1.ApplicationRejectedRequest cmd) {
	logging("rejected request!","warn")
	for ( param in parameterMap() ) {
		if ( state."$param.key"?.state == "inProgress" ) {
			state."$param.key"?.state = "failed"
			break
		} 
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelassociationv2.MultiChannelAssociationReport cmd) {
	def cmds = []
	if (cmd.groupingIdentifier == 1) {
		if (cmd.nodeId != [0, zwaveHubNodeId, 1]) {
			log.debug "${device.displayName} - incorrect MultiChannel Association for Group 1! nodeId: ${cmd.nodeId} will be changed to [0, ${zwaveHubNodeId}, 1]"
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier: 1)
			cmds << zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier: 1, nodeId: [0,zwaveHubNodeId,1])
		} else {
			logging("${device.displayName} - MultiChannel Association for Group 1 correct.","info")
		}
	}
	if (cmds) { [response(encapSequence(cmds, 1000))] }
}

//event handlers
def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	logging("${device.displayName} - BasicReport received, ignored.","info")
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatmodev2.ThermostatModeReport cmd, ep = null) {
	logging("${device.displayName} - ThermostatModeReport received, mode: ${cmd.mode}","info")
	switch (cmd.mode) {
		case 0: sendEvent([name: "thermostatMode", value: "off"]); break;
		case 1: sendEvent([name: "thermostatMode", value: "auto"]); break;
		case 31: sendEvent([name: "thermostatMode", value: "heat"]); break;
	}
}

def zwaveEvent(physicalgraph.zwave.commands.thermostatsetpointv2.ThermostatSetpointReport cmd, ep = null) {
	logging("${device.displayName} - ThermostatSetpointReport received, cmd: ${cmd}","info")
	def cmdScale = cmd.scale == 1 ? "F" : "C"
	sendEvent(name: "thermostatSetpoint", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledValue, cmdScale, cmd.precision).toFloat() as Integer, displayed: true)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd, ep = null) {
	logging("${device.displayName} - SensorMultilevelReport received, ep: $ep cmd: ${cmd}","info")
	if ( ep == 2 && cmd.sensorType == 1) {
		sendEvent(name: "temperature", unit: getTemperatureScale(), value: convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision), displayed: true)
		updateSensor()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd, ep = null) {
	logging("${device.displayName} - BatteryReport received, value: ${cmd.batteryLevel} - $ep", "info")
	if (ep == 1 || ep == null) {
		sendEvent(name: "battery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
	} else {
		sendEvent(name: "sensorBattery", value: cmd.batteryLevel.toString(), unit: "%", displayed: true)
		updateSensor()
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd, ep = null) {
	logging("${device.displayName} - NotificationReport received: ${cmd} - $ep", "info")
	def notificationMap = [ 
		10: 'Replace sensor battery soon.',
		11: 'Replace sensor battery now!',
		12: 'Battery is charging.',
		13: 'Battery is fully charged.',
		14: 'Charge battery soon.',
		15: 'Charge battery now!',
		2: 'External Sensor removed!',
		3: 'Motor Error!',
		4: 'Calibration error!' ]
	def currentNotification = device.currentValue("notification")
	currentNotification = ( currentNotification != null && currentNotification != "" ) ? currentNotification += "\n" : ""
	switch (cmd.notificationType) {
		case 8: switch (cmd.event) {
					case 0: sendEvent(name: "notification", value: removeItem(currentNotification, notificationMap[cmd.eventParameter[0] as Integer]) , displayed: true); break;
					default: sendEvent(name: "notification", value: currentNotification + notificationMap[cmd.event  as Integer], displayed: true); break;
				}; break;
		case 9: if (cmd.event == 3) { sendEvent(name: "notification", value: currentNotification + notificationMap[cmd.eventParameter[0] as Integer], displayed: true) }; break;
	}
}

private removeItem(String currentNotifications, String itemToRemove) {
	log.debug "removeItem!"
	def tempItems = currentNotifications.tokenize('\n')
	tempItems.remove(itemToRemove)
	return tempItems.join('\n')
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd, ep = null) {
	logging("${device.displayName} - MeterReport received, value: ${cmd.scaledMeterValue} scale: ${cmd.scale}","info")
	switch (cmd.scale) {
		case 0: sendEvent([name: "energy", value: cmd.scaledMeterValue, unit: "kWh"]); break;
		case 2: sendEvent([name: "power", value: cmd.scaledMeterValue, unit: "W"]); break;
	}
	multiStatusEvent("${device.currentValue("power")} W / ${device.currentValue("energy")} kWh")
}

/*
####################
## Z-Wave Toolkit ##
####################
*/
def parse(String description) {
	def result = []
	logging("${device.displayName} - Parsing: ${description}")
	if (description.startsWith("Err 106")) {
		result = createEvent(
			descriptionText: "Failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
			eventType: "ALERT",
			name: "secureInclusion",
			value: "failed",
			displayed: true,
		)
	} else if (description == "updated") {
		return null
	} else {
		def cmd = zwave.parse(description, cmdVersions()) 
		if (cmd) {
			logging("${device.displayName} - Parsed: ${cmd}")
			zwaveEvent(cmd)
		}
	}
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions()) 
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed SecurityMessageEncapsulation into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract Secure command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd) {
	def version = cmdVersions()[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed Crc16Encap into: ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract CRC16 command from $cmd"
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand(cmdVersions())
	if (encapsulatedCommand) {
		logging("${device.displayName} - Parsed MultiChannelCmdEncap ${encapsulatedCommand}")
		zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
	} else {
		log.warn "Unable to extract MultiChannel command from $cmd"
	}
}

private logging(text, type = "debug") {
	if (settings.logging == "true") {
		log."$type" text
	}
}

private secEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using Secure Encapsulation, command: $cmd","info")
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
	logging("${device.displayName} - encapsulating command using CRC16 Encapsulation, command: $cmd","info")
	zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format() 
}

private multiEncap(physicalgraph.zwave.Command cmd, Integer ep) {
	logging("${device.displayName} - encapsulating command using MultiChannel Encapsulation, ep: $ep command: $cmd","info")
	zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:ep).encapsulate(cmd)
}

private encap(physicalgraph.zwave.Command cmd, Integer ep) {
	encap(multiEncap(cmd, ep))
}

private encap(List encapList) {
	encap(encapList[0], encapList[1])
}

private encap(Map encapMap) {
	encap(encapMap.cmd, encapMap.ep)
}

private encap(physicalgraph.zwave.Command cmd) {
	if (zwaveInfo.zw.contains("s")) { 
		secEncap(cmd)
	} else if (zwaveInfo.cc.contains("56")){ 
		crcEncap(cmd)
	} else {
		logging("${device.displayName} - no encapsulation supported for command: $cmd","info")
		cmd.format()
	}
}

private encapSequence(cmds, Integer delay=250) {
	delayBetween(cmds.collect{ encap(it) }, delay)
}

private encapSequence(cmds, Integer delay, Integer ep) {
	delayBetween(cmds.collect{ encap(it, ep) }, delay)
}

private List intToParam(Long value, Integer size = 1) {
	def result = []
	size.times { 
		result = result.plus(0, (value & 0xFF) as Short)
		value = (value >> 8)
	}
	return result
}
/*
##########################
## Device Configuration ##
##########################
*/
private Map cmdVersions() {
	[0x5E: 2, 0x98: 1, 0x9F: 1, 0x55: 1, 0x56: 1, 0x6C: 1, 0x22: 1, 0x86: 1, 0x8E: 2, 0x31: 5, 0x40: 2, 0x43: 2, 0x53: 1, 0x59: 1, 0x5A: 1, 0x7A: 2, 0x60: 3, 0x71: 3, 0x72: 2, 0x75: 2, 0x80: 1, 0x70: 2, 0x81: 1, 0x73: 1, 0x85: 1] //Fibaro Heat Controller
}

private parameterMap() {[
	[key: "overrideDuration", num: 1, size: 4, type: "number", def: 240, min: 10, max: 10000 , title: "Override Schedule duration", descr: ""],
	[key: "additionalFunctions", num: 2, size: 4, type: "number", def: 1, min: 0, max: 32 , title: "Additional functions", descr: ""]
]}
