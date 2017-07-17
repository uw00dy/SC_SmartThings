/**
 *  Sonoff S20 - Smart App
 *  Type: Smart App
 *
 *  Copyright 2017 (C) chancsc@gmail.com
 *  
 *  v1.0 - 
 */
definition(
    name: "Sonoff Device Creator",
    namespace: "sc",
    author: "CSC",
    description: "Creates Sonoff Device on the fly!",
    category: "Convenience",
    iconUrl: "https://github.com/chancsc/icon/raw/master/standard-tile%401x.png",
    iconX2Url: "https://github.com/chancsc/icon/raw/master/standard-tile@2x.png",
    iconX3Url: "https://github.com/chancsc/icon/raw/master/standard-tile@3x.png")


preferences {
	section("Create Sonoff Device") {
		input "deviceLabel", "text", title: "Device Label", required: true
	}
    section("on this hub...") {
        input "theHub", "hub", multiple: false, required: true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    def deviceId = app.id
    log.debug(deviceId)
    def existing = getChildDevice(deviceId)
    if (!existing) {
        def childDevice = addChildDevice("csc", "Sonoff", deviceId, theHub.id, [label: deviceLabel])
    }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}
