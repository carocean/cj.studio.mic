
# mic

- Microservices Gateway Node Monitoring and Management Center
  ```
  MIC is the microservice monitoring center in the ECM distributed microservice architecture, similar to Eureka in the Spring distributed microservice architecture. It is used for registering, discovering, and managing OpenPorts microservice nodes within the distributed architecture.
  ```

## Features

	•	Nodes register with the mic.
	•	Monitor the operating status of each node.
	•	Manage the start and stop of servers within each node through the mic.
	•	View the backward connections between nodes.
	•	Support remote application deployment to nodes.
	•	View the node’s stub API (if supported by the node application).

## Dependencies

	•	Dependent on the User Center (UC).
	•	MongoDB.

## Dependent Projects:

	•	ECM
	•	Gateway
	•	OpenPorts

## Usage

Configuration for Gateway Node Registration to MIC:

Location: conf/mic-registry.json

{
	"guid": "8E8710A9-11EB-4F38-B901-657538057BA1",
	"title": "Source Code Gateway",
	"enabled": true,
	"desc": "Source Code Gateway registers with MIC",
	"mic": {
		"location": "/rr/mm/",
		"host": "tcp://localhost:7080?heartbeat=5000&initialWireSize=1&workThreadCount=2&acceptErrorPath=/website/error/",
		"reconnDelay": 6000,
		"reconnPeriod": 15000,
		"cjtoken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaiIsInBhc3N3b3JkIjoiMTEiLCJleHAiOjExNzQ2OTMwMzIwNjUsInVzZXIiOiJjaiIsImlhdCI6MTU1MzgzMjA2NSwianRpIjoiY2Q2MWQwYmEtNDhjMy00NzNkLTlkYTMtMDBlZGFlYzFkNDc2In0.1Yq4mMYTYO0nzeWSpg2wJhgUtK-e2tdKf1f3fM3mh0Q"
	}
}

![Main Page](https://github.com/carocean/cj.studio.mic/blob/master/documents/img/mic.png)

![mic图2](https://github.com/carocean/cj.studio.mic/blob/master/documents/img/mic2.png)
