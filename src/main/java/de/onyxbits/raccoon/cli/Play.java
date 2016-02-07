/*
 * Copyright 2016 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import com.akdeniz.googleplaycrawler.GooglePlay.BulkDetailsResponse;
import com.akdeniz.googleplaycrawler.GooglePlayAPI;

import de.onyxbits.raccoon.gplay.PlayManager;
import de.onyxbits.weave.Globals;

/**
 * Google Play related functions
 * 
 * @author patrick
 * 
 */
class Play {

	/**
	 * Perform a details query, print the raw results to stdout
	 * 
	 * @param name
	 *          packagename
	 */
	public static void details(String name) {
		Globals globals = GlobalsProvider.getGlobals();
		PlayManager playManager = globals.get(PlayManager.class);
		GooglePlayAPI api = playManager.createConnection();
		try {
			System.out.println(api.details(name));
		}
		catch (Exception e) {
			Router.fail("play.exception", e.getMessage());
		}
	}

	/**
	 * Perform a details query, print to individual files
	 * 
	 * @param input
	 *          file to read packagenames from
	 */
	public static void details(File input) {
		Globals globals = GlobalsProvider.getGlobals();
		PlayManager playManager = globals.get(PlayManager.class);
		GooglePlayAPI api = playManager.createConnection();
		File parent = input.getParentFile();
		Vector<String> tmp = new Vector<String>();
		readPackages(input, tmp);
		for (String name : tmp) {

			File output = new File(parent, name);
			String res = "";
			try {
				res = api.details(name).toString();
			}
			catch (Exception e) {
				// Not found -> empty file
			}
			try {
				FileWriter fw = new FileWriter(output, false);
				fw.write(res);
				fw.close();
			}
			catch (Exception e) {
				Router.fail("play.exception", e.getMessage());
			}
		}
	}

	/**
	 * Perform a bulk details query, write result to individual files
	 * 
	 * @param input
	 *          file containing a list of packagenames.
	 */
	public static void bulkDetails(File input) {
		Vector<String> tmp = new Vector<String>();
		File parent = input.getParentFile();
		readPackages(input, tmp);

		Globals globals = GlobalsProvider.getGlobals();
		PlayManager playManager = globals.get(PlayManager.class);
		GooglePlayAPI api = playManager.createConnection();
		BulkDetailsResponse res = null;
		try {
			res = api.bulkDetails(tmp);
		}
		catch (IOException e) {
			Router.fail("play.exception", e.getMessage());
		}

		try {
			for (int i = 0; i < tmp.size(); i++) {
				File output = new File(parent, tmp.get(i));
				FileWriter fw = new FileWriter(output, false);
				fw.write(res.getEntry(i).toString());
				fw.close();
			}
		}
		catch (Exception e) {
			Router.fail("play.exception", e.getMessage());
		}
	}

	private static void readPackages(File input, Vector<String> collect) {
		try {
			BufferedReader bi = new BufferedReader(new FileReader(input));
			String line = null;
			while ((line = bi.readLine()) != null) {
				if (line.trim().length() > 0 && !line.startsWith("#")) {
					collect.add(line);
				}
			}
			bi.close();
		}
		catch (Exception e) {
			Router.fail("play.inputfile", input.getAbsolutePath());
		}
	}
}
