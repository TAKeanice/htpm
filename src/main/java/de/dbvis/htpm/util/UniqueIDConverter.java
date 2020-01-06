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
    private static final String forbiddenCharacters = "[\"]";
    private static final String forbiddenUnquotedCharacters = "[-+<=]";

    public static int getIntegerId(String eventId) {

        String unquotedId = !isQuotedQuickTest(eventId) ? eventId : eventId.substring(1, eventId.length() - 1);

        if (integerIDMap.containsKey(unquotedId)) {
            return integerIDMap.get(unquotedId);
        } else {
            if (!eventIDValid(eventId)) {
                throw new IllegalArgumentException("Problem with event name \"" + eventId + "\":\n" +
                        "Event name must not be null, empty or contain <,=,+,- (unless it is quoted). " +
                        "Quotes are never allowed.");
            }
            return addNewId(unquotedId);
        }
    }

    private static int addNewId(String eventId) {
        int newIntegerId = stringIDList.size();
        integerIDMap.put(eventId, newIntegerId);
        stringIDList.add(eventId);
        return newIntegerId;
    }

    private static boolean eventIDValid(String eventId) {
        if (eventId == null || eventId.equals("")) {
            //must not be null or empty
            return false;
        } else if (isQuotedProperly(eventId)) {
            //must not contain any forbidden characters if quoted
            return !containsForbiddenChars(eventId.substring(1, eventId.length() - 1));
        } else {
            //must not contain forbidden characters or characters forbidden for unquoted ids if not quoted
            return !(containsForbiddenChars(eventId) || containsForbiddenCharsIfUnquoted(eventId));
        }
    }

    private static boolean isQuotedQuickTest(String eventId) {
        return eventId.charAt(0) == '"' && eventId.charAt(eventId.length() - 1) == '"';
    }

    private static boolean isQuotedProperly(String eventId) {
        return eventId.matches("\"[^\"]*\"");
    }

    private static boolean containsForbiddenChars(String eventIdQuotesRemoved) {
        return eventIdQuotesRemoved.matches(".*" + forbiddenCharacters + ".*");
    }

    private static boolean containsForbiddenCharsIfUnquoted(String eventId) {
        return eventId.matches(".*" + forbiddenUnquotedCharacters + ".*");
    }

    private static String quote(String rawId) {
        return "\"" + rawId + "\"";
    }

    public static String getStringId(int id) {
        if (stringIDList.size() > id) {
            String eventId = stringIDList.get(id);
            return containsForbiddenCharsIfUnquoted(eventId) ? quote(eventId) : eventId;
        } else return null;
    }
}
