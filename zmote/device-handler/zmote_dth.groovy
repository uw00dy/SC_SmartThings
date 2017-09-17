/*
*  ZMote Button
*  Category: Device Handler
* 
*  Source: Http button creator, Zmote DTH
*  Copyright 2017 (C)chancsc@gmail.com
*  Credit: CSC, Ben Roy
*/

metadata {
	definition (name: "ZMote Button", namespace: "csc", author: "Soon Chye") {
        capability "Actuator"
	capability "Switch"
	capability "Momentary"
        capability "Sensor"
	}

    preferences {
        input("zmote_ip", "string", title: "zmote IP address", description: "IP address of your zmote", required: true, displayDuringSetup: true)
        input("zmote_uuid", "string", title: "zmote UUID", description: "UUID of your zmote", required: true, displayDuringSetup: true)
        input("irCommand", "string", title:"Command to be send", description: "zmote command, e.g. sendir,1:1,....", displayDuringSetup: true)
    }

	// simulator metadata
	simulator {}

	// UI tile definitions
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Push', action: "momentary.push", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

def on() {
	push()
}

def off() {
	push()
}

def push() {
	log.debug "---Sending IR command to zmote---"
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)
	send(irCommand)
}

def send(String zmote_command) {
	def host = zmote_ip
    log.debug "PUT http://$zmote_ip:80/v2/$zmote_uuid"
	log.debug "${zmote_command}"

	def headers = [:] 
	headers.put("HOST", "$host:80")
	headers.put("Content-Type", "application/json")
	log.debug "The Header is $headers"
    def hubAction = new physicalgraph.device.HubAction(
        [
            method: "PUT",
            headers: headers,
            path: "/v2/$zmote_uuid",
            body: "${zmote_command}"
        ],
        null,
        [callback: calledBackHandler]
    )
    hubAction
}

// handle any response from the zmote here
void calledBackHandler(physicalgraph.device.HubResponse hubResponse) {
    log.debug "Entered calledBackHandler()..."
    def body = hubResponse.xml
    log.debug "${hubResponse}"
    log.debug "body in calledBackHandler() is: ${body}"
}
