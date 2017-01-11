Name:
Stephen Hu

Student Number:
31580129

Secret Code:
096A883072EAC51641E8CDB33F174E18

# Running the code with sample student number:
`java -jar A1.jar request -ip 137.82.252.191 -port 5627 -snum 1381632`

# Usage
Argument:
'request': Send student number UDP request

Required Flags:
'-ip': Destination IP address of request
'-port': Destination port of request
'-snum': Student number to send

# Building package:
1. `mvn package`
2. `java -jar target/CPEN431-1.0.jar request -ip 137.82.252.191 -port 5627 -snum 1381632`

