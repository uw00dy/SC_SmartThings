/**
 *  TKBHOME Z-Wave Plug - TZ68E
 *
 *  Copyright 2016 Soon Chye
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  By default the LED is set to 'Night-Light' mode, which means the LED is ON when the device is OFF. 
 *  To change this so the LED is OFF when the device is OFF, set parameter 1 to 1.
 *
 *  Ver1.0 - First release
 * 
 */

metadata {
        definition (name: "TKB Z-Wave Plug (TZ68E)", namespace: "sc", author: "SoonChye") {
                capability "Switch"
                capability "Refresh"
                capability "Indicator"
                
                command		"led"
                command		"configure"
                command		"reset"
                
                fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x86, 0x72, 0x5A, 0x73, 0x85, 0x59, 0x25, 0x20, 0x27, 0x70, 0x7A", outClusters: ""
        }

        simulator {
                // These show up in the IDE simulator "messages" drop-down to test sending event messages to your device handler
                status "basic report on"                 : zwave.basicV1.basicReport(value:0xFF).incomingMessage()
                status "basic report off"                : zwave.basicV1.basicReport(value:0).incomingMessage()
                status "basic set on"                    : zwave.basicV1.basicSet(value:0xFF).incomingMessage()

                // turn on            
                reply "2001FF,delay 5000,2002": "command: 2503, payload: FF"

                // turn off
                reply "200100,delay 5000,2002": "command: 2503, payload: 00"                
        }

        tiles {
        
                standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
                        state "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#79b821"
                        state "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
                }
                standardTile("refresh", "command.refresh", decoration: "flat") {
                        state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
                }
                
                standardTile("indicator", "device.indicatorStatus", inactiveLabel: false, decoration: "flat") {
                        state "when off", action:"indicator.indicatorWhenOff", icon:"st.indicators.lit-when-on"
                        state "when on", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
                }

                main (["switch"])
                //important - to show the control on screen
                details (["switch", "refresh", "indicator"])
        }
}

def parse(String description) {
        def result = null
        def cmd = zwave.parse(description, [0x60: 3])
        if (cmd) {
                result = zwaveEvent(cmd)
                log.debug "Parsed ${cmd} to ${result.inspect()}"
        } else {
                log.debug "Non-parsed event: ${description}"
        }
        result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
        def result = []
        result << createEvent(name:"switch", value: cmd.value ? "on" : "off")

        // For a multilevel switch, cmd.value can be from 1-99 to represent dimming levels
        result << createEvent(name:"level", value: cmd.value, unit:"%", descriptionText:"${device.displayName} dimmed ${cmd.value==255 ? 100 : cmd.value}%")

        result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
        createEvent(name:"switch", value: cmd.value ? "on" : "off")
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log "here"
	def value = "when off"
	if (cmd.configurationValue[1] == 0) {value = "when on"}
	if (cmd.configurationValue[1] == 1) {value = "when off"}
	[name: "indicatorStatus", value: value, display: false]
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd) {
        def result = []
        if (cmd.nodeId.any { it == zwaveHubNodeId }) {
                result << createEvent(descriptionText: "$device.displayName is associated in group ${cmd.groupingIdentifier}")
        } else if (cmd.groupingIdentifier == 1) {
                // We're not associated properly to group 1, set association
                result << createEvent(descriptionText: "Associating $device.displayName in group ${cmd.groupingIdentifier}")
                result << response(zwave.associationV1.associationSet(groupingIdentifier:cmd.groupingIdentifier, nodeId:zwaveHubNodeId))
        }
        result
}

// Devices that support the Security command class can send messages in an encrypted form;
// they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x98: 1, 0x20: 1]) // can specify command class versions here like in zwave.parse
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

// MultiChannelCmdEncap and MultiInstanceCmdEncap are ways that devices can indicate that a message
// is coming from one of multiple subdevices or "endpoints" that would otherwise be indistinguishable
def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // can specify command class versions here like in zwave.parse
        log.debug ("Command from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiInstanceCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x30: 1, 0x31: 1]) // can specify command class versions here like in zwave.parse
        log.debug ("Command from instance ${cmd.instance}: ${encapsulatedCommand}")
        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand)
        }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
        createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def on() {
        delayBetween([
                zwave.basicV1.basicSet(value: 0xFF).format(),
                zwave.basicV1.basicGet().format()
        ])
}

def off() {
        delayBetween([
                zwave.basicV1.basicSet(value: 0x00).format(),
                zwave.basicV1.basicGet().format()
        ])
}

def refresh() {
		log.debug "Executing 'refresh'"
        // Some examples of Get commands
        delayBetween([
                zwave.switchBinaryV1.switchBinaryGet().format(),
                zwave.switchMultilevelV1.switchMultilevelGet().format(),
                zwave.meterV2.meterGet(scale: 0).format(),      // get kWh
                zwave.meterV2.meterGet(scale: 2).format(),      // get Watts
                zwave.sensorMultilevelV1.sensorMultilevelGet().format(),
                zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType:1, scale:1).format(),  // get temp in Fahrenheit
                zwave.batteryV1.batteryGet().format(),
                zwave.basicV1.basicGet().format(),
        ], 1200)
}

// If you add the Polling capability to your device type, this command will be called approximately
// every 5 minutes to check the device's state
def poll() {
        zwave.basicV1.basicGet().format()
}

def indicatorWhenOff() {
	log.debug "indicatorWhenOff"
	//value 1, when outlet on - blue LED light on, when outlet off - blue LED light off
	sendEvent(name: "indicatorStatus", value: "when on", display: true)
	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 1, size: 1).format()
}

def indicatorWhenOn() {
	log.debug "indicatorWhenOn'"
	//value 0, when outlet on - blue LED light off, when outlet off - blue LED light on
	sendEvent(name: "indicatorStatus", value: "when off", display: true)
	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 1, size: 1).format()
}
