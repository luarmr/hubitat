/*
 *  Virtual Pixela driver
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
 *    2022-11-08  luarmr         Created basic version for pixela driver
 */


metadata {
	definition (name: "Pixela driver", namespace: "luarmr", author: "Raul Martin") {

        command	"increment"
        command	"decrement"
        capability "Actuator"
        attribute "lastFailureTime", "String"
		attribute "lastCheckinTime", "String"
		attribute "svgBase", "String"
		attribute "svgLine", "String"
		attribute "svgBadge", "String"
		attribute "htmlLink", "String"
        attribute "todayRetina", "String"

	}
	preferences {
		input name: "user", type: "string", title: "User", defaultValue: '', required: true
		input name: "token", type: "string", title: "Token", defaultValue: '', required: true
		input name: "graphId", type: "string", title: "Graph Id", defaultValue: '', required: true
		input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
	}
}



def installed() {
	log.warn "installed..."
	dirty()
}

def updated() {
	log.warn "updated..."
	log.warn "description logging is: ${txtEnable == true}"

    unschedule()
    setProperties();
    schedule("0 0 0 * * ?", setProperties)
}

def setProperties() {
    if(user == '' || graphId == '' || name == '') {
        log.error "Driver without required parameters"
        return
    }
    sendEvent(name: "svgBase", value: "https://pixe.la/v1/users/${user}/graphs/${graphId}")
    sendEvent(name: "svgLine", value: "https://pixe.la/v1/users/${user}/graphs/${graphId}?mode=line")
    sendEvent(name: "svgBadge", value: "https://pixe.la/v1/users/${user}/graphs/${graphId}?mode=badge")
    sendEvent(name: "htmlLink", value: "https://pixe.la/v1/users/${user}/graphs/${graphId}.html")
    date = new Date().format('yyyyMMdd')
    sendEvent(name: "todayRetina", value: "https://pixe.la/v1/users/${user}/graphs/${graphId}/${date}/retina.svg")
}

def parse(String description) {
}

def _action(name, num_try) {
    if(user == '' || graphId == '' || name == '') {
        log.error "Driver without required parameters"
        return
    }

    if (num_try == 0) {
        log.error "Pixela reply incorrectly too many times"
        return
    }

    retrying_left = num_try - 1

    params = [
        uri: "https://pixe.la/v1/users/${user}/graphs/${graphId}/${name}",
        headers: ['X-USER-TOKEN': "${token}"],
    ]
    Closure $parseResponse = { response ->
        if (txtEnable) {
            log.info "Pixela reply correctly"
        }
        sendEvent(name: "lastFailureTime", value: new Date().toLocaleString())
    }
     try {
        httpPut(params, $parseResponse)
     } catch (e) {
        if (txtEnable) {
            log.info "Pixela reply incorrectly but expected - retrying left (${retrying_left})"
        }
        _action(name, retrying_left)
    }
}

def increment() {
	sendEvent(name: "lastCheckinTime", value: new Date().toLocaleString())
    _action('increment', 20)
}

def decrement() {
	sendEvent(name: "lastCheckinTime", value: new Date().toLocaleString())
    _action('decrement', 20)
}
