package de.dbvis.htpm.io.deserializer;

import java.io.IOException;
import java.text.ParseException;

public interface Deserializer<T> {

	public T deserialize() throws IOException, ParseException;
	
}
