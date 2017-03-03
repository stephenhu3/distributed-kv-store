Names and Student Numbers:

Stephen Hu : 31580129

Emmett Tan : 37087129

Alan Hu : 31580111

GROUP ID: 4

A4 Verification Code: A13A3FE
#Deliverables
Proof of Shutdown:
Shutdown is triggered via the SHUTDOWN_NODE static variable located in the DistributedSystemConfiguration class. 

ProtocolBufferKeyValueStoreResponse.java Line 122: SHUTDOWN_NODE is set to true

KVOperationThread.java Line 30: KVOperationThread is killed

RequestHandlerThread.java Line 19: RequestHandlerThread is killed

ResponseHandlerThread.java Line 23: ResponseHandlerThread is killed

UDPServerThread.java Line 39: UDPServerThread is killed



#Extra Notes

Refactored single threaded model into thread pool + queues, reduced unneeded ByteString and Msg type conversions

# Starting the KV store server
`java -jar -Xmx64m target/CPEN431-1.0.jar spawn -name test -port 10129`

or use pre-packaged JAR:

`java -jar -Xmx64m a4.jar spawn -name test -port 10129`

# Usage
Argument:
'spawn': Run server that listens, performs operation, and responds to KV requests

Required Flags:

'-name': Name of host server

'-port': Port number to host server

# Sending client KV request
`java -jar target/CPEN431-1.0.jar kv -ip 127.0.0.1 -port 10129 -cmd put -key 270F -value 270F`

* Note, this application's specific client and server need to be run on separate machines, as they share same ports since they are a single Dropwizard application
* Evaluation tests can be run locally with no issues

# Usage
Argument:
'kv': Send key value store protocol buffer request

Required Flags:
'-ip': Destination IP address of request

'-port': Destination port of request

'-cmd': Student number to send

available commands include the following:

"put"

"get"

"remove"

"shutdown"

"deleteAll"

"isAlive"

"getPID"

'-key': Key to send (as a HEX string)

'-value': Value to send (as a HEX string)

# Building package:
`mvn package`
