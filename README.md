# Flow Log Parser

## Problem Statement

The Flow Log Parser is a Java program designed to parse a file containing flow log data and map each row to a tag based on a lookup table. The lookup table is defined as a CSV file with three columns: `dstport`, `protocol`, and `tag`. The combination of `dstport` and `protocol` determines the tag to be applied.

The program should generate an output file containing the following:
- Count of matches for each tag
- Count of matches for each port/protocol combination

### Sample Flow Logs
2 123456789012 eni-0a1b2c3d 10.0.1.201 198.51.100.2 443 49153 6 25 20000 1620140761 1620140821 ACCEPT OK <br />
2 123456789012 eni-4d3c2b1a 192.168.1.100 203.0.113.101 23 49154 6 15 12000 1620140761 1620140821 REJECT OK <br />
2 123456789012 eni-5e6f7g8h 192.168.1.101 198.51.100.3 25 49155 6 10 8000 1620140761 1620140821 ACCEPT OK <br />

### Sample Lookup Table
dstport,protocol,tag
25,tcp,sv_P1
68,udp,sv_P2
23,tcp,sv_P1

## Requirements to Run

- Java Development Kit (JDK) 8 or above
- Command-line interface (CLI) or terminal
- Flow log file (text file, up to 10 MB)
- Lookup table file (CSV file, up to 10,000 mappings)
- IANA protocol file (CSV file named "iana-protocol-numbers.csv", located in the same directory as the Java source code)

## Usage

To compile the program:
`javac LogParser.java`

To run the program:
`java LogParser <flowLogFilePath> <lookupTablePath>`


Replace `<flowLogFilePath>` with the path to the flow log file and `<lookupTablePath>` with the path to the lookup table CSV file.

## Output

The program generates two output files:

1. `tag_count.txt`: Contains the count of matches for each tag.

### Sample output:<br />
Tag Counts: <br />
Tag,Count <br />
sv_P2,1<br />
sv_P1,2<br />
...

2. `port_protocol_count.txt`: Contains the count of matches for each port/protocol combination.
   
### Sample output:<br />
Port/Protocol Combination Counts:<br />
Port,Protocol,Count<br />
22,tcp,1<br />
23,tcp,1<br />
...

## Assumptions

1. Flow log records are all version 2 and in default format.
2. The IANA protocol numbers CSV file ("iana-protocol-numbers.csv") exists in the same directory as the Java source code.
3. Input files (flow log and lookup table) are well-formatted and follow the specified structure.
4. The input files are provided as the command-line arguments with flow log file being the first argument and the lookup table being the second argument.
5. The flow log file contains valid entries with 14 fields each.
6. The lookup table CSV file has a header row with "dstport", "protocol", and "tag" columns.
7. Protocol names in the lookup table match those in the IANA protocol numbers file.

## Error Handling

- The program catches and prints the stack trace of any `IOException` that may occur during file reading or writing.
- If the required command-line arguments (flow log file path and lookup table path) are not provided, the program will display an appropriate error message and terminate.

## Tests

I ran several tests to make sure the program works correctly:

- Checked if it handles missing or incorrect input files properly
- Tested with empty files and files containing thousands of entries
- Made sure it correctly maps protocol numbers to names
- Verified that tags are assigned correctly based on the lookup table
- Tested with unusual port numbers and protocols
- Checked if the counting of tags and port/protocol combinations is accurate
- Made sure the output files are formatted correctly
- Tested with large input files (close to 10 MB) to check performance
- Tried various error scenarios to ensure proper error handling
I also paid special attention to edge cases involving extreme entries in the flow log files.

## Overall Analysis

Time and Space Complexity:
Currently, the program's time complexity is O(n), where n is the number of entries in the flow log file. This is because we're processing each entry once. The space complexity is O(m), where m is the number of unique tags or port/protocol combinations, whichever is larger. We're using HashMaps to store the counts, which gives us constant-time lookups and insertions.

Potential Improvements:

We could potentially improve the program's performance by:
1. Using a more efficient data structure for the lookup table, like a Trie, which could speed up tag lookups. But the use of trie would be an overkill for this.
2. Implementing multi-threading to process large files faster, especially if we're dealing with files larger than 10MB.

Meeting Requirements:
The program meets the given requirements pretty well. It handles files up to 10MB and can process up to 10,000 lookup table entries. The case-insensitive matching and the ability to handle multiple port/protocol combinations for a single tag are also implemented as required.

Scaling Beyond Current Requirements:

If we need to handle larger files or more complex scenarios, we might want to consider:

1. Using a database instead of in-memory data structures. This would allow us to handle much larger datasets without running out of memory.

2. Implementing a streaming approach to process the flow log file. This would let us handle files of any size without loading the entire file into memory.

3. Using more advanced Java features like parallel streams or the Fork/Join framework for better performance on multi-core systems.

These changes would make the program more scalable and flexible, but they'd also make it more complex. For the current requirements, our simple approach works well and is easier to understand and maintain.



