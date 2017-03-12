Names and Student Numbers:

Stephen Hu : 31580129

Emmett Tan : 37087129

Alan Chang : 31580111

GROUP ID: 4a

A4 Verification Code: 33B5046
#Deliverables
Proof of Shutdown:
Shutdown is triggered via the SHUTDOWN_NODE static variable located in the DistributedSystemConfiguration class. 

ProtocolBufferKeyValueStoreResponse.java Line 122: SHUTDOWN_NODE is set to true

KVOperationThread.java Line 30: KVOperationThread is killed

RequestHandlerThread.java Line 19: RequestHandlerThread is killed

ResponseHandlerThread.java Line 23: ResponseHandlerThread is killed

UDPServerThread.java Line 39: UDPServerThread is killed



#Extra Notes

<b>GossipThreads

This application uses an epidemic/gossip based protocol in order to keep an updated list 
of which nodes are alive in the network. This list consists of entries that keep track of 
hosts and how recent their entry has been refreshed. Every half a second, each node picks
two random nodes from the full list of nodes and broadcasts its own list. The destination
node compares its own list to the received list. If two entries have the same host, the 
 one with the lower number of hops is taken.

<b>ConsistentHashRing

The ConsistentHashRing class uses a concurrent skip list in order to distribute keys and
values evenly throughout several nodes. Nodes are added or removed based on the list of
alive nodes which is inside of the NodesList java class. Whenever a request is made, the 
key is hashed and used to find its next successor. This successor contains information 
relating to the next node to search. If the successor is null, that means that the current
node holds data necessary to serve that particular request. Basically, the hashring will
continue iterating through successors until the address and port are null.

On average, skip lists provide O(log n) time complexity for searching, insertion, and deletion and O(n) for space complexity.
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
