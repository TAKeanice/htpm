package de.dbvis.htpm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UniqueIDConverter {

    /**
     * A map holding all mappings from string ID to integer ID. This limits number of event types to 4 billion.
     */
    private static Map<String, Integer> integerIDMap = new HashMap<>(100);
    private static List<String> stringIDList = new ArrayList<>(100);

    public static int getIntegerId(String eventId) {
        if (!integerIDMap.containsKey(eventId)) {

            if (!eventIDValid(eventId)) {
                throw new IllegalArgumentException("EventID must not be null, empty or contain <,=,+,-");
            }

            integerIDMap.put(eventId, stringIDList.size());
            stringIDList.add(eventId);
        }
        return integerIDMap.get(eventId);
    }

    public static boolean eventIDValid(String eventId) {
        return !(eventId == null
                || eventId.equals("")
                || eventId.contains("[\\+\\-<=]"));
    }

    public static String getStringId(int id) {
        return stringIDList.size() > id ? stringIDList.get(id) : null;
    }
}
