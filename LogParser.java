import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class LogParser {
    private static final Map<String, String> protocolMap = new HashMap<>();
    // tag count map
    private static final Map<String, Integer> tagCountMap = new HashMap<>();
    // port protocol combination count map
    private static final Map<String, Integer> portProtocolCountMap = new HashMap<>();
    // Title for tag count file
    private static final String TagCountTitle = "Tag Counts: ";
    // Headers for tag count file
    private static final String[] TagCountHeaders = {"Tag", "Count"};
    // Title for port protocol count file
    private static final String PortProtocolCountTitle = "Port/Protocol Combination Counts: ";
    // Headers for port protocol count file
    private static final String[] PortProtocolCountHeaders = {"Port", "Protocol", "Count"};

    public static void main(String[] args) {
        // 1st argument corresponds to flow log data as a text 
        // 2nd argument corresponds to look up table as a csv file
        // 3rd argument corresponds to iana protocol data as a csv file
        if (args.length < 3) {
            System.out.println("Please provide the path to the flow log file as the first command-line argument and the path to the lookup table CSV file as the second command-line argument.");
            return;
        }
        String flowLogFilePath = args[0];
        String lookupTablePath = args[1];
        String ianaProtocolFilePath = args[2];
        
        Map<String, String> lookupTable = new HashMap<>();

        readFile(ianaProtocolFilePath, "iana", null);
        
        readFile(lookupTablePath, "lookup", lookupTable);

        readFile(flowLogFilePath, "flowlog", lookupTable);
        


        // Write tag count map to output file
        writeFile("tag_count.txt", tagCountMap);
        // Write port protocol count map to output file
        writeFile("port_protocol_count.txt", portProtocolCountMap);

    }

    public static void writeFile(String filePath, Map<String, Integer> map) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the title
            writer.write(filePath.equals("tag_count.txt") ? TagCountTitle : PortProtocolCountTitle);
            writer.newLine();
            // Write the headers
            writer.write(String.join(", ", filePath.equals("tag_count.txt") ? TagCountHeaders : PortProtocolCountHeaders));
            writer.newLine();
            // Write the data
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile(String filePath, String fileType, Map<String, String> lookupTable) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            switch (fileType) {
                // Case to handle parsing lookup table file
                case "lookup":
                    String[] headers = null;
                    int dstportIndex = -1;
                    int protocolIndex = -1;
                    int tagIndex = -1;

                    while ((line = reader.readLine()) != null) {
                        // if headers is null, then we are reading the header row
                        if (headers == null) {
                            // Read the header row
                            headers = line.split(",");
                            for (int i = 0; i < headers.length; i++) {
                                if (headers[i].equalsIgnoreCase("dstport")) {
                                    dstportIndex = i;
                                } else if (headers[i].equalsIgnoreCase("protocol")) {
                                    protocolIndex = i;
                                } else if (headers[i].equalsIgnoreCase("tag")) {
                                    tagIndex = i;
                                }
                            }
                            continue;
                        }
                        // Reading the data rows
                        String[] parts = line.split(",");
                        if (dstportIndex != -1 && protocolIndex != -1 && tagIndex != -1 &&
                                parts.length > Math.max(dstportIndex, Math.max(protocolIndex, tagIndex))) {
                            String dstport = parts[dstportIndex].trim();
                            String protocol = parts[protocolIndex].trim();
                            String tag = parts[tagIndex].trim();
                            String key = dstport + "," + protocol;
                            lookupTable.put(key, tag);
                        }
                    }
                    break;
                // Case to handle parsing iana file
                case "iana":
                    reader.readLine(); // Skip the header line
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",", 3);
                        if (parts.length >= 2) {
                            String number = parts[0].trim();
                            String name = parts[1].trim().toLowerCase();
                            //System.out.println("Protocol: " + name + ", Number: " + number);
                            protocolMap.put(number, name);
                        }
                    }
                    break;
                // Case to handle parsing flowlog file
                case "flowlog":
                    while ((line = reader.readLine()) != null) {
                        String[] fields = line.trim().split("\\s+");
                        // Ensure it's a valid flow log entry
                        if (fields.length == 14) {  
                            String dstport = fields[6];
                            String protocolNumber = fields[7];
                            String protocolName = protocolMap.getOrDefault(protocolNumber, "Unknown");
                            String key = dstport + "," + protocolName;
                            System.out.println("dstport: " + dstport);
                            System.out.println("protocolName: " + protocolName);
                            String tag = lookupTable.getOrDefault(key, "Untagged");
                            tagCountMap.put(tag, tagCountMap.getOrDefault(tag, 0) + 1);
                            portProtocolCountMap.put(key, portProtocolCountMap.getOrDefault(key, 0) + 1);
                            System.out.println(line + " -> Protocol: " + protocolName + ", Tag: " + tag);
                        } else {
                            System.out.println("Invalid flow log entry: " + line);
                        }
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  
}