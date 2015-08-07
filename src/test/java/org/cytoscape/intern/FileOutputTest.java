package org.cytoscape.intern;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class FileOutputTest {

	@Test
	public void testOutput() {
		InputStream expectedFile = ClassLoader.getSystemResourceAsStream("expected_dot_files/karate.gv");
		InputStream actualFile = ClassLoader.getSystemResourceAsStream("actual_dot_files/karate.gv");
		BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expectedFile));
		BufferedReader actualReader = new BufferedReader(new InputStreamReader(actualFile));
		String expectedRead;
		String actualRead;
		try {
			while ((expectedRead = expectedReader.readLine()) != null && ( actualRead = actualReader.readLine()) != null) {
				assertEquals("Actual output didn't match expected", expectedRead, actualRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
