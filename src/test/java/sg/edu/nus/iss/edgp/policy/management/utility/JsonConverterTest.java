package sg.edu.nus.iss.edgp.policy.management.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonConverterTest {

	private JsonConverter converter;

	@BeforeEach
	public void setUp() {
		converter = new JsonConverter();
	}

	@Test
	public void testConvertToDatabaseColumn_Success() {
		Map<String, Object> input = new HashMap<>();
		input.put("key1", "value1");
		input.put("key2", 123);

		String json = converter.convertToDatabaseColumn(input);

		assertNotNull(json);
		assertTrue(json.contains("\"key1\":\"value1\""));
		assertTrue(json.contains("\"key2\":123"));
	}

	@Test
	public void testConvertToEntityAttribute_Success() {
		String json = "{\"key1\":\"value1\",\"key2\":123}";

		Map<String, Object> result = converter.convertToEntityAttribute(json);

		assertNotNull(result);
		assertEquals("value1", result.get("key1"));
		assertEquals(123, result.get("key2"));
	}

	@Test
	public void testConvertToDatabaseColumn_Exception() {
		assertThrows(IllegalArgumentException.class, () -> {
			Map<String, Object> invalidMap = new HashMap<>();
			invalidMap.put("invalid", new Object() {
				// Jackson can't serialize this
			});
			converter.convertToDatabaseColumn(invalidMap);
		});
	}

	@Test
	public void testConvertToEntityAttribute_Exception() {
		String malformedJson = "{invalid json}";

		Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			converter.convertToEntityAttribute(malformedJson);
		});

		assertTrue(exception.getMessage().contains("Error converting JSON to Map"));
	}
}
