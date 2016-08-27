/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro Smoke Sensor
 *  File Name:			fibaro-smoke-sensor.groovy
 *	Initial Release:	2016-08-23
 *	@author:			CSC
 *  Email:				chancsc@me.com
 *  @version:			1.0
 *
 *  Copyright 2016 Soon Chye
 *
 *  Software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *  either express or implied. See the License for the specific language governing permissions
 *  and limitations under the License.
 *
 */
 
 /**
 * Sets up metadata, simulator info and tile definition. The tamper tile is setup, but 
 * not displayed to the user. We do this so we can receive events and display on device 
 * activity. If the user wants to display the tamper tile, adjust the tile display lines
 * with the following:
 *		main(["water", "temperature", "tamper"])
 *		details(["water", "temperature", "battery", "tamper"])
 *
 * @param none
 *
 * @return none
 */
metadata {
	definition (name: "Fibaro Smoke Sensor (SC)", namespace: "smartthings", author: "SmartThings") {
		capability "Smoke Detector"
		capability "Temperature Measurement"
		capability "Configuration"
		capability "Battery"
        capability "Sensor"
        
        attribute "tamper", "enum", ["detected", "clear"]
        attribute "heatAlarm", "enum", ["overheat detected", "clear", "rapid temperature rise", "underheat detected"]

/*
        command		"resetParams2StDefaults"
        command		"listCurrentParams"
        command		"updateZwaveParam"
        command		"test"
*/
        
        fingerprint deviceId: "0x1000", inClusters: "0x9C, 0x31, 0x86, 0x72, 0x70, 0x85, 0x8E, 0x8B, 0x56, 0x84, 0x80", outClusters: ""
        fingerprint mfr:"010F", prod:"0C00", model:"1000"
	}
    simulator {
        //battery
        for (int i in [0, 5, 10, 15, 50, 99, 100]) {
            status "battery ${i}%":  new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
                    new physicalgraph.zwave.Zwave().batteryV1.batteryReport(batteryLevel: i)
            ).incomingMessage()
        }
        status "battery 100%": "command: 8003, payload: 64"
        status "battery 5%": "command: 8003, payload: 05"
        //smoke
        status "smoke detected": "command: 7105, payload: 01 01"
        status "smoke clear": "command: 7105, payload: 01 00"
        status "smoke tested": "command: 7105, payload: 01 03"
        //temperature
        for (int i = 0; i <= 100; i += 20) {
            status "temperature ${i}F": new physicalgraph.zwave.Zwave().securityV1.securityMessageEncapsulation().encapsulate(
                    new physicalgraph.zwave.Zwave().sensorMultilevelV5.sensorMultilevelReport(scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1)
            ).incomingMessage()
        }
    }
    preferences {
        input description: "After successful installation, please click B-button at the Fibaro Smoke Sensor to update device status and configuration",
                title: "Instructions", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input description: "Enter the menu by press and hold B-button for 3 seconds. Once indicator glows WHITE, release the B-button. Visual indicator will start changing colours in sequence. Press B-button briefly when visual indicator glows GREEN",
                title: "To check smoke detection state", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input description: "Please consult Fibaro Smoke Sensor operating manual for advanced setting options. You can skip this configuration to use default settings",
                title: "Advanced Configuration", displayDuringSetup: true, type: "paragraph", element: "paragraph"
        input "smokeSensorSensitivity", "enum", title: "Smoke Sensor Sensitivity", options: ["High","Medium","Low"], defaultValue: "${smokeSensorSensitivity}", displayDuringSetup: true
        input "zwaveNotificationStatus", "enum", title: "Notifications Status", options: ["disabled","casing opened","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${zwaveNotificationStatus}", displayDuringSetup: true
        input "visualIndicatorNotificationStatus", "enum", title: "Visual Indicator Notifications Status",
                options: ["disabled","casing opened","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${visualIndicatorNotificationStatus}", displayDuringSetup: true
        input "soundNotificationStatus", "enum", title: "Sound Notifications Status",
                options: ["disabled","casing opened","exceeding temperature threshold", "lack of Z-Wave range", "all notifications"],
                defaultValue: "${soundNotificationStatus}", displayDuringSetup: true
        input "temperatureReportInterval", "enum", title: "Temperature Report Interval",
                options: ["reports inactive", "5 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${temperatureReportInterval}", displayDuringSetup: true
        input "temperatureReportHysteresis", "number", title: "Temperature Report Hysteresis", description: "Available settings: 1-100 C", range: "1..100", displayDuringSetup: true
        input "temperatureThreshold", "number", title: "Overheat Temperature Threshold", description: "Available settings: 0 or 2-100 C", range: "0..100", displayDuringSetup: true
        input "excessTemperatureSignalingInterval", "enum", title: "Excess Temperature Signaling Interval",
                options: ["5 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${excessTemperatureSignalingInterval}", displayDuringSetup: true
        input "lackOfZwaveRangeIndicationInterval", "enum", title: "Lack of Z-Wave Range Indication Interval",
                options: ["5 minutes", "15 minutes", "30 minutes", "1 hour", "6 hours", "12 hours", "18 hours", "24 hours"], defaultValue: "${lackOfZwaveRangeIndicationInterval}", displayDuringSetup: true
    }

    tiles (scale: 2){
        multiAttributeTile(name:"smoke", type: "lighting", width: 6, height: 4){
            tileAttribute ("device.smoke", key: "PRIMARY_CONTROL") {
                attributeState("clear", label:"CLEAR", icon:"st.alarm.smoke.clear", backgroundColor:"#ffffff")
                attributeState("detected", label:"SMOKE", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13")
                attributeState("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#e86d13")
                attributeState("replacement required", label:"REPLACE", icon:"st.alarm.smoke.test", backgroundColor:"#FFFF66")
                attributeState("unknown", label:"UNKNOWN", icon:"st.alarm.smoke.test", backgroundColor:"#ffffff")
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
                attributeState "battery", label:'Battery: ${currentValue}%', unit:"%"
            }
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit:"%"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "temperature", label:'${currentValue}°', unit:"C"
        }
        valueTile("heatAlarm", "device.heatAlarm", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "clear", label:'TEMPERATURE OK', backgroundColor:"#ffffff"
            state "overheat detected", label:'OVERHEAT DETECTED', backgroundColor:"#ffffff"
            state "rapid temperature rise", label:'RAPID TEMP RISE', backgroundColor:"#ffffff"
            state "underheat detected", label:'UNDERHEAT DETECTED', backgroundColor:"#ffffff"
        }
        valueTile("tamper", "device.tamper", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "clear", label:'NO TAMPER', backgroundColor:"#ffffff"
            state "detected", label:'TAMPER DETECTED', backgroundColor:"#ffffff"
        }

        main "smoke"
        details(["smoke","temperature"])
    }
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    setConfigured("false") //wait until the next time device wakeup to send configure command
}

// Parse incoming device messages to generate events
def parse(String description)
{
	def result = []
	
    if (description == "updated") {
    	if (!state.MSR) {
			result << response(zwave.wakeUpV1.wakeUpIntervalSet(seconds: 60*60, nodeid:zwaveHubNodeId))
			result << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
		} 
    } else {
        def cmd = zwave.parse(description, [0x9C: 1, 0x31: 1, 0x86: 2, 0x72: 1, 0x70: 1, 0x85: 2, 0x8E: 2, 0x8B: 1, 0x56: 1, 0x84: 1, 0x80: 1, 0x20: 1])
		if (cmd) {
			result += zwaveEvent(cmd) //createEvent(zwaveEvent(cmd))   
		}
    }
    
    result << response(zwave.batteryV1.batteryGet().format())
    
    if ( result[0] != null ) {
		log.debug "Parse returned ${result}"
		result
    }
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 2, 0x31: 2]) // can specify command class versions here like in zwave.parse
	log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd) {
    log.info "Executing zwaveEvent 84 (WakeUpV1): 07 (WakeUpNotification) with cmd: $cmd"
    log.info "checking this MSR : ${getDataValue("MSR")} before sending configuration to device"
    def result = [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
    def cmds = []
    /* check MSR = "manufacturerId-productTypeId" to make sure configuration commands are sent to the right model */
    if (!isConfigured() && getDataValue("MSR")?.startsWith("010F-0C00")) {
        result << response(configure()) // configure a newly joined device or joined device with preference update
    } else {
        //Only ask for battery if we haven't had a BatteryReport in a while
        if (!state.lastbatt || (new Date().time) - state.lastbatt > 24*60*60*1000) {
            log.debug("Device has been configured sending >> batteryGet()")
            cmds << zwave.securityV1.securityMessageEncapsulation().encapsulate(zwave.batteryV1.batteryGet()).format()
            cmds << "delay 1200"
        }
        log.debug("Device has been configured sending >> wakeUpNoMoreInformation()")
        cmds << zwave.wakeUpV1.wakeUpNoMoreInformation().format()
        result << response(cmds) //tell device back to sleep
    }
    result
}

/*  -- not reporting temperature, replace it with sensormultilevelv5
def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv2.SensorMultilevelReport cmd)
{
	def map = [:]
    
	switch (cmd.sensorType) {
		case 1:
			// temperature
			def cmdScale = cmd.scale == 1 ? "F" : "C"
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
			map.unit = getTemperatureScale()
			map.name = "temperature"
			break;
		case 0:
			// here's our tamper alarm = acceleration
			map.value = cmd.sensorState == 255 ? "active" : "inactive"
			map.name = "acceleration"
			break;
	}
	createEvent(map)
}
*/

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
    log.info "Executing zwaveEvent 31 (SensorMultilevelV5): 05 (SensorMultilevelReport) with cmd: $cmd"
    def map = [:]
    switch (cmd.sensorType) {
        case 1:
            map.name = "temperature"
            def cmdScale = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmdScale, cmd.precision)
            map.unit = getTemperatureScale()
            break
        default:
            map.descriptionText = cmd.toString()
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel > 0 ? cmd.batteryLevel.toString() : 1
	map.unit = "%"
	map.displayed = false
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv1.SensorBinaryReport cmd) {
	def map = [:]
	map.value = cmd.sensorValue ? "active" : "inactive"
	map.name = "acceleration"
    
	if (map.value == "active") {
		map.descriptionText = "$device.displayName detected vibration"
	}
	else {
		map.descriptionText = "$device.displayName vibration has stopped"
	}
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	log.debug "${device.displayName} parameter '${cmd.parameterNumber}' with a byte size of '${cmd.size}' is set to '${cmd.configurationValue}'"
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    log.debug "BasicSet with CMD = ${cmd}"
    
    if (!isConfigured()) {
    	def result = []
    	def map = [:]
    
    	map.name = "water"
		map.value = cmd.value ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
    
    	// If we are getting a BasicSet, and isConfigured == false, then we are likely NOT properly configured.
    	result += lateConfigure(true)
    
    	result << createEvent(map)
        
        result
    }
}

def smokeAlarmEvent(value) {
    log.debug "smokeAlarmEvent(value): $value"
    def map = [name: "smoke"]
    if (value == 1 || value == 2) {
        map.value = "detected"
        map.descriptionText = "$device.displayName detected smoke"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName is clear (no smoke)"
    } else if (value == 3) {
        map.value = "tested"
        map.descriptionText = "$device.displayName smoke alarm test"
    } else if (value == 4) {
        map.value = "replacement required"
        map.descriptionText = "$device.displayName replacement required"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
    log.info "Executing zwaveEvent 71 (NotificationV3): 05 (NotificationReport) with cmd: $cmd"
    def result = []
    if (cmd.notificationType == 7) {
        switch (cmd.event) {
            case 0:
                result << createEvent(name: "tamper", value: "clear", displayed: false)
                break
            case 3:
                result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName casing was opened")
                break
        }
    } else if (cmd.notificationType == 1) { //Smoke Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Smoke Alarm (V2)"
        result << smokeAlarmEvent(cmd.event)
    }  else if (cmd.notificationType == 4) { // Heat Alarm (V2)
        log.debug "notificationv3.NotificationReport: for Heat Alarm (V2)"
        result << heatAlarmEvent(cmd.event)
    } else {
        log.warn "Need to handle this cmd.notificationType: ${cmd.notificationType}"
        result << createEvent(descriptionText: cmd.toString(), isStateChange: false)
    }
    result
}

def heatAlarmEvent(value) {
    log.debug "heatAlarmEvent(value): $value"
    def map = [name: "heatAlarm"]
    if (value == 1 || value == 2) {
        map.value = "overheat detected"
        map.descriptionText = "$device.displayName overheat detected"
    } else if (value == 0) {
        map.value = "clear"
        map.descriptionText = "$device.displayName heat alarm cleared (no overheat)"
    } else if (value == 3 || value == 4) {
        map.value = "rapid temperature rise"
        map.descriptionText = "$device.displayName rapid temperature rise"
    } else if (value == 5 || value == 6) {
        map.value = "underheat detected"
        map.descriptionText = "$device.displayName underheat detected"
    } else {
        map.value = "unknown"
        map.descriptionText = "$device.displayName unknown event"
    }
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	def map = [:]
    
	if (cmd.sensorType == 0x05) {
		map.name = "water"
		map.value = cmd.sensorState ? "wet" : "dry"
		map.descriptionText = "${device.displayName} is ${map.value}"
        
        log.debug "CMD = SensorAlarmReport: ${cmd}"
    	setConfigured("true")
    } else if ( cmd.sensorType == 0) {
    	map.name = "tamper"
        map.isStateChange = true
        map.value = cmd.sensorState ? "tampered" : "secure"
        map.descriptionText = "${device.displayName} has been tampered with"
        runIn(30, "resetTamper") //device does not send alarm cancelation
        
    } else if ( cmd.sensorType == 1) {
    	map.name = "tamper"
        map.value = cmd.sensorState ? "tampered" : "secure"
        map.descriptionText = "${device.displayName} has been tampered with"
        runIn(30, "resetTamper") //device does not send alarm cancelation
        
	} else {
		map.descriptionText = "${device.displayName}: ${cmd}"
	}
	createEvent(map)
}

def resetTamper() {
	def map = [:]
    map.name = "tamper"
    map.value = "secure"
    map.descriptionText = "$device.displayName is secure"
    sendEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.debug "Catchall reached for cmd: ${cmd.toString()}}"
	[:]
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    log.info "Executing zwaveEvent 72 (ManufacturerSpecificV2) : 05 (ManufacturerSpecificReport) with cmd: $cmd"
    log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
    def result = []
    def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
    updateDataValue("MSR", msr)
    log.debug "After device is securely joined, send commands to update tiles"
    result << zwave.batteryV1.batteryGet()
    result << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)
    result << zwave.wakeUpV1.wakeUpNoMoreInformation()
    [[descriptionText:"${device.displayName} MSR report"], response(commands(result, 5000))]
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
    def result = []
    if (cmd.nodeId.any { it == zwaveHubNodeId }) {
        result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
    } else if (cmd.groupingIdentifier == 1) {
        result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
        result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
    }
    result
}

def setConfigured(configure) {
	//device.updateDataValue("configured", "true")
    updateDataValue("configured", configure)
}

def isConfigured() {
	//Boolean configured = device.getDataValue(["configured"]) as Boolean
    //return configured
    getDataValue("configured") == "true"
}

def configure() {
    log.info "${device.displayName} is configuring its settings"
    def request = []

    //1. configure wakeup interval : available: 0, 4200s-65535s, device default 21600s(6hr)
    request += zwave.wakeUpV1.wakeUpIntervalSet(seconds:6*3600, nodeid:zwaveHubNodeId)

    //2. Smoke Sensitivity 3 levels: 1-HIGH , 2-MEDIUM (default), 3-LOW
    if (smokeSensorSensitivity && smokeSensorSensitivity != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1,
                                                          scaledConfigurationValue:
                                                          smokeSensorSensitivity == "High" ? 1 :
                                                          smokeSensorSensitivity == "Medium" ? 2 :
                                                          smokeSensorSensitivity == "Low" ? 3 : 2)
    }
    //3. Z-Wave notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable
    if (zwaveNotificationStatus && zwaveNotificationStatus != "null"){
        request += zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: notificationOptionValueMap[zwaveNotificationStatus] ?: 0)
    }
    //4. Visual indicator notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable, 4-lack of range notification
    if (visualIndicatorNotificationStatus && visualIndicatorNotificationStatus != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: notificationOptionValueMap[visualIndicatorNotificationStatus] ?: 0)
    }
    //5. Sound notification status: 0-all disabled (default), 1-casing open enabled, 2-exceeding temp enable, 4-lack of range notification
    if (soundNotificationStatus && soundNotificationStatus != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 4, size: 1, scaledConfigurationValue: notificationOptionValueMap[soundNotificationStatus] ?: 0)
    }
    //6. Temperature report interval: 0-report inactive, 1-8640 (multiply by 10 secs) [10s-24hr], default 180 (30 minutes)
    if (temperatureReportInterval && temperatureReportInterval != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 20, size: 2, scaledConfigurationValue: timeOptionValueMap[temperatureReportInterval] ?: 180)
    } else { //send SmartThings default configuration
        request += zwave.configurationV1.configurationSet(parameterNumber: 20, size: 2, scaledConfigurationValue: 180)
    }
    //7. Temperature report hysteresis: 1-100 (in 0.1C step) [0.1C - 10C], default 10 (1 C)
    if (temperatureReportHysteresis && temperatureReportHysteresis != null) {
        request += zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: temperatureReportHysteresis < 1 ? 1 : temperatureReportHysteresis > 100 ? 100 : temperatureReportHysteresis)
    }
    //8. Temperature threshold: 1-100 (C), default 55 (C)
    if (temperatureThreshold && temperatureThreshold != null) {
        request += zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: temperatureThreshold < 1 ? 1 : temperatureThreshold > 100 ? 100 : temperatureThreshold)
    }
    //9. Excess temperature signaling interval: 1-8640 (multiply by 10 secs) [10s-24hr], default 180 (30 minutes)
    if (excessTemperatureSignalingInterval && excessTemperatureSignalingInterval != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 31, size: 2, scaledConfigurationValue: timeOptionValueMap[excessTemperatureSignalingInterval] ?: 180)
    } else { //send SmartThings default configuration
        request += zwave.configurationV1.configurationSet(parameterNumber: 31, size: 2, scaledConfigurationValue: 180)
    }
    //10. Lack of Z-Wave range indication interval: 1-8640 (multiply by 10 secs) [10s-24hr], default 2160 (6 hours)
    if (lackOfZwaveRangeIndicationInterval && lackOfZwaveRangeIndicationInterval != "null") {
        request += zwave.configurationV1.configurationSet(parameterNumber: 32, size: 2, scaledConfigurationValue: timeOptionValueMap[lackOfZwaveRangeIndicationInterval] ?: 2160)
    } else {
        request += zwave.configurationV1.configurationSet(parameterNumber: 32, size: 2, scaledConfigurationValue: 2160)
    }
    //11. get battery level when device is paired
    request += zwave.batteryV1.batteryGet()

    //12. get temperature reading from device
    request += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)

    commands(request) + ["delay 10000", zwave.wakeUpV1.wakeUpNoMoreInformation().format()]

}


//used to add "test" button for simulation of user changes to parameters
def test() {
	def params = [paramNumber:12,value:4,size:1]
	updateZwaveParam(params)
}

 /**
 * This method will allow the user to update device parameters (behavior) from an app.
 * A "Zwave Tweaker" app will be developed as an interface to do this. Or the user can
 * write his/her own app to envoke this method. No type or value checking is done to
 * compare to what device capability or reaction. It is up to user to read OEM
 * documentation prio to envoking this method.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param List[paramNumber:80,value:10,size:1]
 *
 *
 * @return none
 */
def updateZwaveParam(params) {
	if ( params ) {     
        def pNumber = params.paramNumber
        def pSize	= params.size
        def pValue	= [params.value]
        log.debug "Make sure device is awake and in recieve mode (triple-click?)"
        log.debug "Updating ${device.displayName} parameter number '${pNumber}' with value '${pValue}' with size of '${pSize}'"

		def cmds = []
        cmds << zwave.configurationV1.configurationSet(configurationValue: pValue, parameterNumber: pNumber, size: pSize).format()
        cmds << zwave.configurationV1.configurationGet(parameterNumber: pNumber).format()
        delayBetween(cmds, 1000)        
    }
}

 /**
 * Sets all of available Fibaro parameters back to the device defaults except for what
 * SmartThings needs to support the stock functionality as released. This will be
 * called from the "Fibaro Tweaker" or user's app.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param none
 *
 * @return none
 */
def resetParams2StDefaults() {
	log.debug "Resetting ${device.displayName} parameters to SmartThings compatible defaults"
	def cmds = []
	cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], 			parameterNumber: 1,  size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3], 			parameterNumber: 2,  size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], 			parameterNumber: 5,  size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], 			parameterNumber: 7,  size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], 			parameterNumber: 9,  size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,60*60], 		parameterNumber: 10, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,50], 			parameterNumber: 12, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0],				parameterNumber: 13, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [5,220], 		parameterNumber: 50, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [13,172],		parameterNumber: 51, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0,0,225],		parameterNumber: 61, size: 4).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,255,0,0], 	parameterNumber: 62, size: 4).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], 			parameterNumber: 63, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], 			parameterNumber: 73, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], 			parameterNumber: 74, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], 			parameterNumber: 75, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], 			parameterNumber: 76, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], 			parameterNumber: 77, size: 1).format()
    
    delayBetween(cmds, 1200)
}

 /**
 * Lists all of available Fibaro parameters and thier current settings out to the 
 * logging window in the IDE This will be called from the "Fibaro Tweaker" or 
 * user's own app.
 *
 * <p>THIS IS AN ADVANCED OPERATION. USE AT YOUR OWN RISK! READ OEM DOCUMENTATION!
 *
 * @param none
 *
 * @return none
 */
def listCurrentParams() {
	log.debug "Listing of current parameter settings of ${device.displayName}"
	def cmds = []
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format() 
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format() 
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 5).format() 
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 13).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 50).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 51).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 61).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 62).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 63).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 73).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 74).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 75).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 76).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 77).format()
    
	delayBetween(cmds, 1200)
}

