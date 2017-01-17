Name:
Stephen Hu

Student Number:
31580129

Secret Code:
6F0331421A70DFF04D896A12E042D066

# Running the code with sample student number:
`java -jar A3.jar protoc -ip 137.82.252.191 -port 5628 -snum 1381632`

# Running the code with my student number:
`java -jar A3.jar protoc -ip 137.82.252.191 -port 5628 -snum 31580129`

Output:
Sending ID: 31580129
Secret code length: 16
Secret: 6F0331421A70DFF04D896A12E042D066

# Usage
Argument:
'protoc': Send student number protocol buffer request

Required Flags:
'-ip': Destination IP address of request
'-port': Destination port of request
'-snum': Student number to send

# Building package:
1. `mvn package`
2. `java -jar target/CPEN431-1.0.jar protoc -ip 137.82.252.191 -port 5628 -snum 1381632`
