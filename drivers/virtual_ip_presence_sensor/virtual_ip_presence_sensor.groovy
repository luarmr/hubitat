/*
 *  Virtual IP presence sensor
 *
 *  Copyright 2021 Raul Martin Rodriguez
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
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2021-10-31  luarmr         Created basic version for a presence ip virtual sensor
 */

import hubitat.helper.NetworkUtils.PingData

metadata {
    definition(
        name: "Virtual IP presence sensor",
        namespace: "luarmr",
        author: "Raul Martin Rodriguez",
        importUrl: "https://raw.githubusercontent.com/luarmr/hubitat/main/drivers/virtual_ip_presence_sensor/virtual_ip_presence_sensor.groovy",
    ) {
        capability "PresenceSensor"
        capability "Initialize"
        capability "Refresh"

        attribute "percentLoss", "number"
        attribute "packetsReceived", "number"
        attribute "packetsTransmitted", "number"
        attribute "max", "number"
        attribute "avg", "number"
        attribute "min", "number"
        attribute "currentIPAddress", "string"
    }
}

preferences {
    input("IPAddress", "string", title: "The IP Address to ping (127.0.0.1)", defaultValue: '127.0.0.1', required: true)
    input("count", "number", title: "The number of ping requests to send\nNumber between 1 and 5", defaultValue: 3, range: "1..5")
    input("frequency", "number", title: "Seconds between pings\nNumbers higher than 1", defaultValue: 0, required: true)
    input("debugEnable", "bool", title: "Enable debug logging?")
}

def installed() {
    debugEnable && log.trace("installed()")
}

def updateStates(currentIPAddress = "No valid IPAddress", pingData = null) {
    new_values = pingData != null ? pingData : [
        packetLoss: 100,
        packetsTransmitted: 0,
        packetsReceived: 0,
        rttMin: 0,
        rttAvg: 0,
        rttMax: 0,
    ]

    sendEvent(name: "currentIPAddress", value: currentIPAddress)
    sendEvent(name: "presence", value: (new_values.packetLoss < 100 ? "present" : "not present"))
    sendEvent(name: "percentLoss", value: (new_values.packetLoss), unit: "%")
    sendEvent(name: "packetsTransmitted", value: new_values.packetsTransmitted)
    sendEvent(name: "packetsReceived", value: new_values.packetsReceived)
    sendEvent(name: "min", value: new_values.rttMin, unit: "ms")
    sendEvent(name: "avg", value: new_values.rttAvg, unit: "ms")
    sendEvent(name: "max", value: new_values.rttMax, unit: "ms")

}

def ping() {
    debugEnable && log.trace("pinging $IPAddress")
    if (!isIpValid(IPAddress)) {
        updateStates()
        return
    }
    PingData pingData = hubitat.helper.NetworkUtils.ping(IPAddress, count.toInteger())
    updateStates(IPAddress, pingData)
}

def isIpValid(ip) {
    if (!ip) {
        return false;
    }
    String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
    String regex = "$zeroTo255\\.$zeroTo255\\.$zeroTo255\\.$zeroTo255";
    boolean match = ip ==~ regex
    return match
}

def schedulle() {
    unschedule()
    if (frequency > 0) {
        schedule("0/$frequency * * * * ? *", ping)
    }
    ping()
}

def initialize() {
    debugEnable && log.trace("initialize()")
    schedulle()
}

def refresh() {
    debugEnable && log.trace("refresh()")
    schedulle()
}

def updated() {
    debugEnable && log.trace("updated()")
    schedulle();
}
