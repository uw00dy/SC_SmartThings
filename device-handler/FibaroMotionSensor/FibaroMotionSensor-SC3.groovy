/**
 *  Device Type Definition File
 *
 *  Device Type:		Fibaro Motion Sensor v3.2
 *  File Name:			fibarMotion.groovy
 *	Initial Release:	2014-12-10
 *	Author:				Todd Wackford
 *  Modified by:        Ronald Gouldner
 *  Email:				todd@wackford.net
 *
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Ver1.0 - ZW5 + Cyril + Todd Wackford + Ronald Gouldner
 *
 */

 preferences {
 
    input description: "When changing these values make sure you triple click the sensor b-button (inside) to wake the device (blue light displays) and then select the \"configure\" tile after clicking done on this page.   Note: Param Settings indicated in parentheses (p-80 p1)=Parameter 80 part 1, this helps you lookup possible values in manual.", 
	      displayDuringSetup: false, type: "paragraph", element: "paragraph"	
	input("sensitivity", "number", title: "Sensitivity (p-1) 8-255, 10=(default)", defaultValue:10)
	input("blindTime", "number", title: "Blind Time (p-2) 0-15, 15=(default)", defaultValue:15)	
	input("pirOperatingMode", "enum", title: "PIR Operating Mode (p-8)?", default: "Both", options: ["Both","Day","Night"])
	input("nightDayThreshold", "number", title: "Night/Day Threshold (p-9) 1-65535 (default=200)", defaultValue:200)
	input("illumReportThresh", "number", title: "Illum Report Threshold in Lux (p-40) 0-65535 0=no reports sent, 200=(default)",
		description: "Illumination reports when lux changes by this amount", defaultValue:200)
	input("illumReportInt", "number",
		title: "Illum Report Interval in Seconds (p-42) 0-65535 0=none (default), <5 may block temp readings, too low will waste battery",
		description: "Time interval in seconds to report illum regardless of change", defaultValue:0)     	
	input("ledOnOff", "enum", title: "LED On/Off (p-80 p1)?", default:"On", options: ["On","Off"])
	input("ledModeFrequency", "enum", title: "LED Frequency (p-80 p2)?", default: "Once", options: ["Once","Long-Short","Long-Two Short"])
	input("ledModeColor", "enum", title: "LED Color ? (p-80 p3)", default:"Temp", options: ["Temp","Flashlight","White","Red","Green","Blue","Yellow","Cyan","Magenta"])
	input("ledBrightness", "number",
		title: "LED Brightness 1-100% 0=ambient ? (p-81)",
		description: "LED Brightness Level Percent (1-100) 0=ambient based", defaultValue:50)
	input("tamperLedOnOff", "enum", title: "Tamper LED ? (p-89)", default:"On", options: ["On","Off"])
	
		 
 }
 
 /**
 * Sets up metadata, simulator info and tile definition.
 *
 * @param none
 *
 * @return none
 */
metadata {
	definition (name: "Fibaro Motion Sensor (SC)", namespace: "soonchye", author: "Soon Chye") {
		
		//attribute   "needUpdate", "string"
		
		capability "Battery"
		capability "Configuration"
		capability "Illuminance Measurement"
		capability "Motion Sensor"
		capability "Sensor"
		capability "Tamper Alert"
		capability "Temperature Measurement"
        
        command		"resetParams2StDefaults"
        command		"listCurrentParams"
        command		"updateZwaveParam"
        command		"test"
        command		"configure"
        
        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x86, 0x72, 0x59, 0x80, 0x73, 0x56, 0x22, 0x31, 0x98, 0x7A", outClusters: ""
	}

	simulator {
		// messages the device returns in response to commands it receives
		status "motion (basic)"     : "command: 2001, payload: FF"
		status "no motion (basic)"  : "command: 2001, payload: 00"
		status "motion (binary)"    : "command: 3003, payload: FF"
		status "no motion (binary)" : "command: 3003, payload: 00"

		for (int i = 0; i <= 100; i += 20) {
			status "temperature ${i}F": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 1, sensorType: 1, scale: 1).incomingMessage()
		}

		for (int i = 200; i <= 1000; i += 200) {
			status "luminance ${i} lux": new physicalgraph.zwave.Zwave().sensorMultilevelV2.sensorMultilevelReport(
				scaledSensorValue: i, precision: 0, sensorType: 3).incomingMessage()
		}

		for (int i = 0; i <= 100; i += 20) {
			status "battery ${i}%": new physicalgraph.zwave.Zwave().batteryV1.batteryReport(
				batteryLevel: i).incomingMessage()
		}
	}

    tiles {
        standardTile("motion", "device.motion", width: 2, height: 2) {
            state "active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0"
            state "inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        }
        valueTile("temperature", "device.temperature", inactiveLabel: false) {
            state "temperature", label:'${currentValue}°',
            backgroundColors:
            [
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("illuminance", "device.illuminance", inactiveLabel: false) {
            state "luminosity", label:'${currentValue} lux', unit:"lux"
        }
        valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat") {
            state "battery", label:'${currentValue}% battery', unit:""
        }
        standardTile("acceleration", "device.tamper") {
            state("active", label:'vibration', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
            state("inactive", label:'still', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
        }
        /*
        standardTile("configure", "device.needUpdate", inactiveLabel: false) {
            state "NO" , label:'Synced', action:"configuration.configure", icon:"st.secondary.refresh-icon", backgroundColor:"#99CC33"
            state "YES", label:'Pending', action:"configuration.configure", icon:"st.secondary.refresh-icon", backgroundColor:"#CCCC33"
        }
        */
		standardTile("configure", "device.configure", inactiveLabel: false, decoration: "flat") {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}
		main(["motion", "temperature", "illuminance", "acceleration"])
        details(["motion", "temperature", "illuminance", "acceleration", "configure", "battery", "listCurrentParams"])
    } 
}
 
/*for testing*/
def configure() {
	listCurrentParams()
}


 /**
 * Configures the device to settings needed by SmarthThings at device discovery time.
 *
 * @param none
 *
 * @return none
 */
def configure1() {
	log.debug "Configuring Device For SmartThings Use"
	def cmds = []
    
	// send associate to group 3 to get sensor data reported only to hub
	//cmds << zwave.associationV2.associationSet(groupingIdentifier:3, nodeId:[zwaveHubNodeId]).format()
    cmds << zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
	
	//  Set Sensitivity (1) Values 8-255 default 10
	log.debug "Setting sensitivity $sensitivity"
	def senseValue = sensitivity as int
	if (senseValue < 8 || senseValue > 255) {
		log.warn "Unknown sensitivity-Setting to default of 10"
		senseValue = 10
	}
	log.debug "Setting Param 1 to $senseValue"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [senseValue], parameterNumber: 1, size: 1).format()
        
	//  Set Blind Time (3) Value 0-15 default 15
	log.debug "Setting blind time $blindTime"
	def blindValue = blindTime as int
	if (blindValue < 0 || blindValue > 15) {
		log.warn "Blind time outside allowed values-Setting to default of 15"
		blindValue = 15
	}
	log.debug "Setting Param 2 to $blindValue"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [blindValue], parameterNumber: 2, size: 1).format()
    
	log.debug "Setting pir operating mode $pirOperatingMode"
	def pirOperatingModeParamValue=0 // Both (default)
	if (pirOperatingMode == "Day") {
		pirOperatingModeParamValue=1
	} else if (pirOperatingMode == "Night") {
	    pirOperatingModeParamValue=2
	}
	log.debug "Setting Param 8 to $pirOperatingModeParamValue"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [pirOperatingModeParamValue], parameterNumber: 8, size: 1).format()
	
	log.debug "Setting Night/Day threshold $nightDayThreshold"
	def nightDayThresholdAsInt = nightDayThreshold.toInteger()
	if (nightDayThresholdAsInt >= 1 && nightDayThresholdAsInt <= 65535 ) {
		def short nightDayThresholdLow = nightDayThresholdAsInt & 0xFF
		def short nightDayThresholdHigh = (nightDayThresholdAsInt >> 8) & 0xFF
		def nightDayThresholdBytes = [nightDayThresholdHigh, nightDayThresholdLow]
		log.debug "Setting Param 9 to $nightDayThresholdBytes"
		cmds << zwave.configurationV2.configurationSet(configurationValue: nightDayThresholdBytes, parameterNumber: 9, size: 2).format()
	} else {
		log.warn "Setting Param 9 out of rang changing to default of 200"
	    cmds << zwave.configurationV2.configurationSet(configurationValue: [0,200], parameterNumber: 9, size: 2).format()
	}
	
	
	// turn on tamper sensor with active/inactive reports (use it as an acceleration sensor) default is 0, or off
	log.debug "Setting Param 24 to value of 4 - HARD CODED"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [4], parameterNumber: 24, size: 1).format()
	
	log.debug "Illum Report Threshole Preference illumReportThresh=$illumReportThresh"
	def illumReportThreshAsInt = illumReportThresh.toInteger()
	if (illumReportThreshAsInt >= 0 && illumReportThreshAsInt <= 65535 ) {
		def short illumReportThreshLow = illumReportThreshAsInt & 0xFF
		def short illumReportThreshHigh = (illumReportThreshAsInt >> 8) & 0xFF
		def illumReportThreshBytes = [illumReportThreshHigh, illumReportThreshLow]
		log.debug "Setting Param 40 to $illumReportThreshBytes"
		cmds << zwave.configurationV2.configurationSet(configurationValue: illumReportThreshBytes, parameterNumber: 40, size: 2).format()
	}
	
	log.debug "Illum Interval Preference illumReportInt=$illumReportInt"
	def illumReportIntAsInt = illumReportInt.toInteger()
	if (illumReportIntAsInt >= 0 && illumReportIntAsInt <= 65535 ) {
		def short illumReportIntLow = illumReportIntAsInt & 0xFF
		def short illumReportIntHigh = (illumReportIntAsInt >> 8) & 0xFF
		def illumReportBytes = [illumReportIntHigh, illumReportIntLow]
		log.debug "Setting Param 42 to $illumReportBytes"
		cmds << zwave.configurationV2.configurationSet(configurationValue: illumReportBytes, parameterNumber: 42, size: 2).format()
	}
	
	// temperature change report threshold (0-255 = 0.1 to 25.5C) default is 1.0 Celcius, setting to .5 Celcius
	log.debug "Setting Param 60 to value of 5 - HARD CODED"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [5], parameterNumber: 60, size: 1).format()
	  
	if (ledOnOff == "Off") {
		log.debug "Setting LED off"
		// 0 = LED Off signal mode
		log.debug "Setting Param 80 to 0"
	    cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 80, size: 1).format()
	} else {
	    log.debug "Setting LED on"
 	    // ToDo Add preference for other available Led Signal Modes
		def ledModeConfigValue=0
		log.debug "ledModeFrequency=$ledModeFrequency"
		log.debug "ledModeColor=$ledModeColor"
		if (ledModeFrequency == "Once") {
			if (ledModeColor == "Temp") {
					ledModeConfigValue=1
			} else if (ledModeColor == "Flashlight") {
					ledModeConfigValue=2
			} else if (ledModeColor == "White") {
					ledModeConfigValue=3
			}else if (ledModeColor == "Red") {
					ledModeConfigValue=4
			}else if (ledModeColor == "Green") {
					ledModeConfigValue=5
			}else if (ledModeColor == "Blue") {
					ledModeConfigValue=6
			}else if (ledModeColor == "Yellow") {
					ledModeConfigValue=7
			}else if (ledModeColor == "Cyan") {
					ledModeConfigValue=8
			}else if (ledModeColor == "Magenta") {
					ledModeConfigValue=9
			} else {
				log.warn "Unknown LED Color-Setting LED Mode to default of 10"
					ledModeConfigValue=10
			}
		} else if (ledModeFrequency == "Long-Short") {
			if (ledModeColor == "Temp") {
				ledModeConfigValue=10
		    } else if (ledModeColor == "Flashlight") {
				ledModeConfigValue=11
		    } else if (ledModeColor == "White") {
				ledModeConfigValue=12
		    } else if (ledModeColor == "Red") {
				ledModeConfigValue=13
		    } else if (ledModeColor == "Green") {
				ledModeConfigValue=14
		    } else if (ledModeColor == "Blue") {
				ledModeConfigValue=15
		    } else if (ledModeColor == "Yellow") {
				ledModeConfigValue=16
		    } else if (ledModeColor == "Cyan") {
				ledModeConfigValue=17
		    } else if (ledModeColor == "Magenta") {
				ledModeConfigValue=18
		    } else {
				log.warn "Unknown LED Color-Setting LED Mode to default of 10"
				ledModeConfigValue=10
			}
		} else if (ledModeFrequency =="Long-Two Short") {
			if (ledModeColor == "Temp") {
				ledModeConfigValue=19
			} else if (ledModeColor == "Flashlight") {
				log.info "Flashlight Mode selected with Frequency Long-Two Short setting ledMode to 11-flashlight mode"
				ledModeConfigValue=11
			} else if (ledModeColor == "White") {
				ledModeConfigValue=20
			} else if (ledModeColor == "Red") {
				ledModeConfigValue=21
			} else if (ledModeColor == "Green") {
				ledModeConfigValue=22
			} else if (ledModeColor == "Blue") {
				ledModeConfigValue=23
			} else if (ledModeColor == "Yellow") {
				ledModeConfigValue=24
			} else if (ledModeColor == "Cyan") {
				ledModeConfigValue=25
			} else if (ledModeColor == "Magenta") {
				ledModeConfigValue=26
			} else {
				log.warn "Unknown LED Color-Setting LED Mode to default of 10"
				ledModeConfigValue=10
			}
		} else {
			log.warn "Unknown LED Frequencey-Setting LED Mode to default of 10"
			ledModeConfigValue=10
		}
		log.debug "Setting Param 80 to $ledModeConfigValue"
		cmds << zwave.configurationV2.configurationSet(configurationValue: [ledModeConfigValue], parameterNumber: 80, size: 1).format()
	}
	
	//  Set Brightness Parameter (81) Percentage 0-100
	log.debug "LED Brightness $ledBrightness"
	def brightness = ledBrightness as int
	if (brightness<0) {
		log.warn "LED Brightness less than 0, setting to 1"
		brightness=1
	}
	if (brightness>100) {
		log.warn "LED Brightness greater than 100, setting to 100"
		brightness=100
	}
	log.debug "Setting Param 81 to $brightness"
	cmds << zwave.configurationV2.configurationSet(configurationValue: [brightness], parameterNumber: 81, size: 1).format()
	
	if (tamperLedOnOff == "Off") {
		log.debug "Setting Tamper LED off"
		log.debug "Setting Param 89 to 0"
		cmds << zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 89, size: 1).format()
	} else {
		log.debug "Setting Tamper LED on"
		log.debug "Setting Param 89 to 1"
		cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 89, size: 1).format()
	}

	delayBetween(cmds, 500)
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"        
    def result = []
    
    if (description.startsWith("Err 106")) {
		if (state.sec) {
			result = createEvent(descriptionText:description, displayed:false)
		} else {
			result = createEvent(
				descriptionText: "FGK failed to complete the network security key exchange. If you are unable to receive data from it, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				displayed: true,
			)
		}
	} else if (description == "updated") {
		return null
	} else {
    	def cmd = zwave.parse(description, [0x31: 5, 0x56: 1, 0x71: 3, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])
    
    	if (cmd) {
    		log.debug "Parsed '${cmd}'"
        	zwaveEvent(cmd)
    	}
    }
}

//security
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x71: 3, 0x84: 2, 0x85: 2, 0x86: 1, 0x98: 1])
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

//crc16
def zwaveEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd)
{
    def versions = [0x31: 5, 0x71: 3, 0x72: 2, 0x80: 1, 0x84: 2, 0x85: 2, 0x86: 1]
	def version = versions[cmd.commandClass as Integer]
	def ccObj = version ? zwave.commandClass(cmd.commandClass, version) : zwave.commandClass(cmd.commandClass)
	def encapsulatedCommand = ccObj?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug "Could not extract command from $cmd"
	} else {
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [ displayed: true ]
    switch (cmd.sensorType) {
    	case 1:
        	map.name = "temperature"
            map.unit = cmd.scale == 1 ? "F" : "C"
            map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, map.unit, cmd.precision)
            break
    	case 3:
        	map.name = "illuminance"
            map.value = cmd.scaledSensorValue.toInteger().toString()
            map.unit = "lux"
            break
    }

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	def map = [:]
    if (cmd.notificationType == 7) {
    	switch (cmd.event) {
        	case 0:
            	if (cmd.eventParameter[0] == 3) {
            		map.name = "tamper"
                    map.value = "inactive"
                    map.descriptionText = "${device.displayName}: tamper alarm has been deactivated"
            	}
            	if (cmd.eventParameter[0] == 8) {
                	map.name = "motion"
                    map.value = "inactive"
                    map.descriptionText = "${device.displayName}: motion has stopped"
                }
        		break
                
        	case 3:
            	map.name = "tamper"
                map.value = "active"
                map.descriptionText = "${device.displayName}: tamper alarm activated"
            	break
                
            case 8:
                map.name = "motion"
                map.value = "active"
                map.descriptionText = "${device.displayName}: motion detected"
                break
        }
    }
    
    createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [:]
	map.name = "battery"
	map.value = cmd.batteryLevel == 255 ? 1 : cmd.batteryLevel.toString()
	map.unit = "%"
	map.displayed = true
	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	def event = createEvent(descriptionText: "${device.displayName} woke up", displayed: false)
    def cmds = []
    cmds << encap(zwave.batteryV1.batteryGet())
    cmds << "delay 500"
    cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0))
    cmds << "delay 500"
    cmds << encap(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1))     
    cmds << "delay 1200"
    cmds << encap(zwave.wakeUpV1.wakeUpNoMoreInformation())
    [event, response(cmds)]
    
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) { 
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
    log.debug "manufacturerName: ${cmd.manufacturerName}"
    log.debug "productId:        ${cmd.productId}"
    log.debug "productTypeId:    ${cmd.productTypeId}"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.DeviceSpecificReport cmd) { 
	log.debug "deviceIdData:                ${cmd.deviceIdData}"
    log.debug "deviceIdDataFormat:          ${cmd.deviceIdDataFormat}"
    log.debug "deviceIdDataLengthIndicator: ${cmd.deviceIdDataLengthIndicator}"
    log.debug "deviceIdType:                ${cmd.deviceIdType}"
    
    if (cmd.deviceIdType == 1 && cmd.deviceIdDataFormat == 1) {//serial number in binary format
		String serialNumber = "h'"
        
        cmd.deviceIdData.each{ data ->
        	serialNumber += "${String.format("%02X", data)}"
        }
        
        updateDataValue("serialNumber", serialNumber)
        log.debug "${device.displayName} - serial number: ${serialNumber}"
    }
}

def zwaveEvent(physicalgraph.zwave.commands.versionv1.VersionReport cmd) {	
    updateDataValue("version", "${cmd.applicationVersion}.${cmd.applicationSubVersion}")
    log.debug "applicationVersion:      ${cmd.applicationVersion}"
    log.debug "applicationSubVersion:   ${cmd.applicationSubVersion}"
    log.debug "zWaveLibraryType:        ${cmd.zWaveLibraryType}"
    log.debug "zWaveProtocolVersion:    ${cmd.zWaveProtocolVersion}"
    log.debug "zWaveProtocolSubVersion: ${cmd.zWaveProtocolSubVersion}"
}

def zwaveEvent(physicalgraph.zwave.commands.deviceresetlocallyv1.DeviceResetLocallyNotification cmd) {
	log.info "${device.displayName}: received command: $cmd - device has reset itself"
}

//used to add "test" button for simulation of user changes to parameters
def test() {
	def params = [paramNumber:80,value:10,size:1]
	updateZwaveParam(params)
}

 /**
 * This method will allow the user to update device parameters (behavior) from an app.
 * A "Zwave Tweaker" app will be developed as an interface to do this. Or the user can
 * write his/her own app to envoke this method. No type or value checking is done to
 * compare to what device capability or reaction. It is up to user to read OEM
 * documentation prior to envoking this method.
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
        log.debug "Make sure device is awake and in recieve mode"
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
 *
 * based on this post, only parameter 1 - 3 are using configurationV2
 * https://community.smartthings.com/t/deprecated-fibaro-motion-detector-v3-2-alpha-release/41106/109
 */
def resetParams2StDefaults() {
	log.debug "Resetting Sensor Parameters to SmartThings Compatible Defaults"
	def cmds = []
	// Sensitivity 8-255 default 10 (lower value more sensitive)
	cmds << zwave.configurationV2.configurationSet(configurationValue: [10], parameterNumber: 1, size: 1).format()
	// Blind Time 0-15 default 15 (8 seconds) seconds = .5 * (setting + 1)
	// Longer Blind = Longer Battery Life
    cmds << zwave.configurationV2.configurationSet(configurationValue: [15], parameterNumber: 2, size: 1).format()
	
    cmds << zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 4, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,30], parameterNumber: 6, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 8, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,200], parameterNumber: 9, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 12, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 16, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [15], parameterNumber: 20, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,30], parameterNumber: 22, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 24, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 26, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,200], parameterNumber: 40, size: 2).format()
    // Illum Report Interval 0=none, 1-5 may cause temp report fail, low values waste battery
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 42, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [5], parameterNumber: 60, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3,132], parameterNumber: 62, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 64, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,0], parameterNumber: 66, size: 2).format()
    // Led Signal Mode Default Default 10  0=Inactive
    cmds << zwave.configurationV1.configurationSet(configurationValue: [10], parameterNumber: 80, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [50], parameterNumber: 81, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0,100], parameterNumber: 82, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3,232], parameterNumber: 83, size: 2).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [18], parameterNumber: 86, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [28], parameterNumber: 87, size: 1).format()
    // Tamper LED Flashing (White/REd/Blue) 0=Off 1=On	
    cmds << zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 89, size: 1).format()
    
    delayBetween(cmds, 500)
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

    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 2).format()
/*    
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 6).format()
*/
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
/*
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 14).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 16).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 20).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 22).format()
*/
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 24).format()
/*
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 26).format()
*/
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 40).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 42).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 60).format()
/*
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 62).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 64).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 66).format()
*/
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 80).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 81).format()
/*
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 82).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 83).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 86).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 87).format()
*/
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 89).format()
    
    //SC trying
    
	// get some device info
	//cmds << response(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
    //cmds << response(zwave.manufacturerSpecificV2.deviceSpecificGet())
    //cmds << response(zwave.versionV1.versionGet())
    //cmds << response(zwave.batteryV1.batteryGet())
    
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
    //cmds << zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1)
 
 	// turn on tamper sensor with active/inactive reports (use it as an acceleration sensor) default is 0, or off
	//cmds << zwave.configurationV1.configurationSet(configurationValue: [4], parameterNumber: 24, size: 1).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 24).format()
 
    // temperature change report threshold (0-255 = 0.1 to 25.5C) default is 1.0 Celcius, setting to .5 Celcius
    //cmds << zwave.configurationV1.configurationSet(configurationValue: [5], parameterNumber: 60, size: 1).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 60).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 20).format()
    //cmds << zwave.configurationV1.configurationGet(parameterNumber: 40).format()
    
    cmds += zwave.wakeUpV2.wakeUpIntervalSet(seconds: 7200, nodeid: zwaveHubNodeId)//FGMS' default wake up interval
    cmds += zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    cmds += zwave.manufacturerSpecificV2.deviceSpecificGet()
    cmds += zwave.versionV1.versionGet()
    cmds += zwave.associationV2.associationSet(groupingIdentifier:1, nodeId:[zwaveHubNodeId])
    cmds += response(zwave.batteryV1.batteryGet().format())
    cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 1, scale: 0)
    cmds += zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 3, scale: 1)
	
    //issue a WakeUpNoMoreInformation command to tell the device it can go back to sleep to save battery
    cmds += zwave.wakeUpV2.wakeUpNoMoreInformation()
    
    // Tamper LED Flashing (White/REd/Blue) 0=Off 1=On	
    cmds += zwave.configurationV2.configurationSet(parameterNumber: 89, size: 1, configurationValue: [0]).format()
    
	delayBetween(cmds, 500)
}
