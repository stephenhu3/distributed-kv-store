Name:
Stephen Hu

Student Number:
31580129

Verification Code:

# Starting the KV store server
`java -jar -Xmx64m target/CPEN431-1.0.jar spawn -name test -port 10129`

# Usage
Argument:
'spawn': Run server that listens, performs operation, and responds to KV requests

Required Flags:
'-name': Name of host server
'-port': Port number to host server

# Sending client KV request
`java -jar target/CPEN431-1.0.jar kv -ip 127.0.0.1 -port 10128 -cmd put -key 270F -value 270F`

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
"getPID
'-key': Key to send (as a HEX string)
'-value': Value to send (as a HEX string)

# Building package:
`mvn package`
