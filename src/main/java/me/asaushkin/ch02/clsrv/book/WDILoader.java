package me.asaushkin.ch02.clsrv.book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that Load the World Development Indicator  data into memory
 * @author author
 *
 */
public class WDILoader {

	public List<WDI> load(String route) throws URISyntaxException {

		URL url = ClassLoader.getSystemResource(route);

		Path file=Paths.get(url.toURI());
		List<WDI> dataSet=new ArrayList<>();
		int lineNumber=1;
		try (InputStream in = Files.newInputStream(file);
			    BufferedReader reader =
			      new BufferedReader(new InputStreamReader(in))) {
			    String line = null;
			    //First line are headers
			    line = reader.readLine();
			    while ((line = reader.readLine()) != null) {
			    	String data[]=parse(line);
			    	WDI dataObject=new WDI();
			    	dataObject.setData(data);
			    	dataSet.add(dataObject);
			    }
			} catch (Exception e) {
			  e.printStackTrace();
			}
		return dataSet;
		
	}

	private String[] parse(String line) {
		String [] ret=new String[59];
		int index=0;
		StringBuffer buffer=new StringBuffer();
		boolean enComillas=false;
		for (int i=0; i<line.length(); i++) {
			char letra=line.charAt(i);
			if (letra=='"') {
				enComillas=!enComillas;
			} else if ((letra==',')&&(!enComillas)) {
				ret[index]=buffer.toString();
				index++;
				buffer=new StringBuffer();
			} else {
				buffer.append(letra);
			}
		}
		ret[index]=buffer.toString();
		index++;
		return ret;
	}
}
