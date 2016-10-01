/*
 *  Copyright 2016 SoonChye
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

metadata {
	// Automatically generated. Make future change here.
	definition (name: "Zigbee SmartPower Outlet (Heiman)", namespace: "sc", author: "SoonChye", category: "C1") {
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Configuration"
		capability "Refresh"

        fingerprint profileId: "0104", inClusters: "0000,0003,0006,0009,0702,0B04", outClusters: "0003", manufacturer: "Heiman", model: "SmartPlug", deviceJoinName: "Heiman Smart Plug"
	}

	// simulator metadata
	simulator {
		// status messages
		status "on": "on/off: 1"
		status "off": "on/off: 0"

		// reply messages
		reply "zcl on-off on": "on/off: 1"
		reply "zcl on-off off": "on/off: 0"
	}

	preferences {
		section {
			image(name: 'educationalcontent', multiple: true, images: [
				"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS1.jpg",
				"http://cdn.device-gse.smartthings.com/Outlet/US/OutletUS2.jpg"
				])
		}
	}

	// UI tile definitions
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "device.switch", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
				attributeState "turningOn", label: 'Turning On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "turningOff"
				attributeState "turningOff", label: 'Turning Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn"
			}
			tileAttribute ("power", key: "SECONDARY_CONTROL") {
				attributeState "power", label:'${currentValue} W'
			}
		}

		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main (["switch", "power"])
		details(["switch", "power","refresh"])
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	log.debug "description is $description"

	def finalResult = zigbee.getKnownDescription(description)

	//TODO: Remove this after getKnownDescription can parse it automatically
	if (!finalResult && description!="updated")
		finalResult = getPowerDescription(zigbee.parseDescriptionAsMap(description))

	if (finalResult) {
		log.info "final result = $finalResult"
        log.info "finalResult.type = $finalResult.type"
		if (finalResult.type == "update") {
			log.info "$device updates: ${finalResult.value}"
		}
		else if (finalResult.type == "power") {
			def powerValue = (finalResult.value as Integer)/10
			sendEvent(name: "power", value: powerValue, descriptionText: '{{ device.displayName }} power is {{ value }} Watts', translatable: true )
		}
		else {
			def descriptionText = finalResult.value == "on" ? '{{ device.displayName }} is On' : '{{ device.displayName }} is Off'
			sendEvent(name: finalResult.type, value: finalResult.value, descriptionText: descriptionText, translatable: true)
		}
	}
	else {
		log.warn "DID NOT PARSE MESSAGE for description : $description"
		log.debug zigbee.parseDescriptionAsMap(description)
	}
}

// Parse incoming device messages to generate events
def parse1(String description) {

	log.debug "Parse Method Called"
	log.trace "description: $description"
    
	if (description?.startsWith("catchall:")) {
		def msg = zigbee.parse(description)
		log.trace "data: $msg.data"

	} else if (description?.startsWith("read attr -")) {
		def descMap = parseDescriptionAsMap(description)
        log.debug "descMap: $descMap"
        log.debug "descMap.cluster: $descMap.cluster"
        log.debug "descMap.attrId: $descMap.attrId"
        log.debug "descMap.value: $descMap.value"
		
        if (descMap.cluster == "0006" && descMap.attrId == "0000") {
                name = "switch"
                value = descMap.value.endsWith("01") ? "on" : "off"
                def result = createEvent(name: name, value: value)
                log.debug "Parse returned1 ${result?.descriptionText}"
                return result
        } else if(descMap.cluster =="0702" && descMap.attrId == "0400") {            
            def value = convertHexToInt(descMap.value[-4..-1])/10 
            // Reading the last 4 characters of the string...Maybe 4 are needed. Needs further test.
            // Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10
            log.debug value
            def name = "power"
            def result = createEvent(name: name, value: value)
            log.debug "Parse returned2 ${result?.descriptionText}"
            return result 
        } else if(descMap.cluster =="0B04" && descMap.attrId == "050b") {            
            def value = convertHexToInt(descMap.value[-4..-1])/10 
            // Reading the last 4 characters of the string...Maybe 4 are needed. Needs further test.
            // Dividing by 10 as the Divisor is 10000 and unit is kW for the device. AttrId: 0302 and 0300. Simplifying to 10
            log.debug value
            def name = "power"
            def result = createEvent(name: name, value: value)
            log.debug "Parse returned3 ${result?.descriptionText}"
            return result 
        }
    } else {
		def name = description?.startsWith("on/off: ") ? "switch" : null
		def value = name == "switch" ? (description?.endsWith(" 1") ? "on" : "off") : null
		def result = createEvent(name: name, value: value)
		log.debug "Parse returned4 ${result?.descriptionText}"
		return result
	}
}

def off() {
	zigbee.off()
}

def on() {
	zigbee.on()
}
/**
 * PING is used by Device-Watch in attempt to reach the Device
 * */
def ping() {
	return zigbee.onOffRefresh()
}

def refresh1() {
  	log.debug "Refresh Method called";	
    [
		"st rattr 0x${device.deviceNetworkId} 1 6 0", "delay 500",    
        "st rattr 0x${device.deviceNetworkId} 1 0x0702 1024", , "delay 500"
	]
}

def refresh() {
	sendEvent(name: "heartbeat", value: "alive", displayed:false)
	//return zigbee.onOffRefresh() + zigbee.refreshData("0x0702", "0x0400")
    return zigbee.onOffRefresh() + zigbee.refreshData("0x0B04", "0x050B")
}

def configure() {
	log.debug "Configure1 Method called"
	// Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee"])
	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	zigbee.onOffConfig + powerConfig() + refresh()
}

//power config for devices with min reporting interval as 1 seconds and reporting interval if no activity as 10min (600s)
//min change in value is 01
def powerConfig() {
	log.debug "powerConfig send..."
	[
        "zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0B04 {${device.zigbeeId}} {}", "delay 200",
        "zdo bind 0x${device.deviceNetworkId} 1 ${endpointId} 0x0702 {${device.zigbeeId}} {}", "delay 200",

        //"zcl global send-me-a-report $cluster $attributeId $dataType $minReportTime $maxReportTime {$reportableChange}", "delay 200",

        "zcl global send-me-a-report 0x0702 0x0400 0x2A 1 60 {05 00}",
        "zcl global send-me-a-report 0x0B04 0x050b 0x29 1 60 {05 00}",
		"send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 500"
	]
}

def configure_working() {
	log.debug "Configure Method called"
	
    // Device-Watch allows 2 check-in misses from device
	sendEvent(name: "checkInterval", value: 60 * 12, displayed: false, data: [protocol: "zigbee"])
    
	String zigbeeId = swapEndianHex(device.hub.zigbeeId)
	
	def configCmds = [	
  
        //Switch Reporting
     ///"zcl global send-me-a-report 6 0 0x10 0 3600 {01}", "delay 500",
        "zcl global send-me-a-report 0x0B04 0x29 1 600 {05 00}",
        "send 0x${device.deviceNetworkId} 1 ${endpointId}", "delay 1000",
        
        //bing to cluster 0x006. Switch On-Off
        "zdo bind 0x${device.deviceNetworkId} 1 1 6 {${device.zigbeeId}} {}", "delay 1000",
        
        //bind to cluster 0x702. Power Consumption
        "zdo bind 0x${device.deviceNetworkId} 1 1 0x0702 {${device.zigbeeId}} {}", "delay 500",
        
        //read attribute 1024Dec/400Hex.
        //"st rattr 0x${device.deviceNetworkId} 1 0x0702 1024"
        
	]
    ///return configCmds + refresh() // send refresh cmds as part of config
    
	// OnOff minReportTime 0 seconds, maxReportTime 5 min. Reporting interval if no activity
	zigbee.onOffConfig(0, 300) + configCmds + refresh()
}

def parseDescriptionAsMap(description) {
	(description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}

private hex(value, width=2) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}


private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}

private getEndpointId() {
	new BigInteger(device.endpointId, 16).toString()
}

//TODO: Remove this after getKnownDescription can parse it automatically
def getPowerDescription(descMap) {
	def powerValue = "undefined"
    log.debug "testing"
    log.debug "descMap = $descMap"
	if (descMap.cluster == "0B04") {
		if (descMap.attrId == "050b") {
			if(descMap.value!="ffff")
				powerValue = zigbee.convertHexToInt(descMap.value)
		}
	}
	else if (descMap.clusterId == "0B04") {
    	log.debug "descMap.command = $descMap.command"
		///if(descMap.command=="07"){
        if(descMap.command=="06"){
			return	[type: "update", value : "power (0B04) capability configured successfully"]
		}
	}
	
    log.debug "powerValue = $powerValue"
	if (powerValue != "undefined"){
		return	[type: "power", value : powerValue]
	}
	else {
		return [:]
	}
}
