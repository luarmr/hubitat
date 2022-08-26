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
 *    2022-01-17  luarmr         Remove tracking to not always relevant attributes, the sensor became too chatty
 *    2022-08-26  luarmr         Remove tracking to not always relevant attributes, the sensor became too chatty
 *                               Change schedule to avoid error when frequency is higher than a minute
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
    input("frequency", "number", title: "Seconds between pings\nNumbers higher than 1\n 0 will pause the pings", defaultValue: 600, required: true, range: "0..60")

    input("trackPercentLoss", "bool", title: "Track percentLoss?", defaultValue: false)
    input("trackPacketsTransmitted", "bool", title: "Track packetsTransmitted?", defaultValue: false)
    input("trackPacketsReceived", "bool", title: "Track packetsReceived?", defaultValue: false)
    input("trackMin", "bool", title: "Track min?", defaultValue: false)
    input("trackAvg", "bool", title: "Track avg?", defaultValue: false)
    input("trackMax", "bool", title: "Track max?", defaultValue: false)

    input("debugEnable", "bool", title: "Enable debug logging?")
}

def installed() {
    debugEnable && log.trace("installed()")
}

def sendEventProxy(name, value, unit = '') {
    if (device.currentValue(name) != value) {
        sendEvent(name: name, value: value, unit: unit)
    }
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

    sendEventProxy("currentIPAddress", currentIPAddress)
    sendEventProxy("presence", (new_values.packetLoss < 100 ? "present" : "not present"))

    trackPercentLoss && sendEvent(name: "percentLoss", value: new_values.packetLoss, unit: "%")
    trackPacketsTransmitted && sendEvent(name: "packetsTransmitted", value: new_values.packetsTransmitted )
    trackPacketsReceived && sendEvent(name: "packetsReceived", value: new_values.packetsReceived)
    trackMax && sendEvent(name: "max", value: new_values.rttMax, unit: "ms")
    trackAvg && sendEvent(name: "avg", value: new_values.rttAvg, unit: "ms")
    trackMin && sendEvent(name: "min", value: new_values.rttMin, unit: "ms")

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

    if (frequency == 60) {
        schedule("0 * * ? * *", ping)
    } else if (frequency > 0) {
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
    NOT_TRACKING = '<span style="color:grey">Not tracking</span>'

    debugEnable && log.trace("updated()")

    schedulle();

    if(!trackPercentLoss) {
        sendEvent(name: "percentLoss", value: NOT_TRACKING)
    }

    if(!trackPacketsTransmitted) {
        sendEvent(name: "packetsTransmitted", value: NOT_TRACKING)
    }

    if(!trackPacketsReceived) {
        sendEvent(name: "packetsReceived", value: NOT_TRACKING)
    }

    if(!trackMax) {
        sendEvent(name: "min", value: NOT_TRACKING)
    }

    if(!trackAvg) {
        sendEvent(name: "avg", value: NOT_TRACKING)
    }

    if(!trackMin) {
        sendEvent(name: "max", value: NOT_TRACKING)
    }
}
