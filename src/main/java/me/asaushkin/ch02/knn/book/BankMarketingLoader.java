package me.asaushkin.ch02.knn.book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that loads the examples of the Bank Marketing data set from a file
 * @author author
 *
 */
public class BankMarketingLoader {

    public static List<BankMarketing> load (String dataPath) throws URISyntaxException {
        return load(dataPath, Integer.MAX_VALUE);
    }


	/**
	 * Method that loads the examples of the Bank Marketing data set from a file
	 * @param dataPath Path to the file where the data items are stored
	 * @return List of BankMarketing examples
	 */
	public static List<BankMarketing> load (String dataPath, int maxLines) throws URISyntaxException {

		//Path file=Paths.get(dataPath);

		Path file = Paths.get(ClassLoader.getSystemResource(dataPath).toURI());

		List<BankMarketing> dataSet=new ArrayList<>();
		try (
				InputStream in = Files.newInputStream(file);
			    BufferedReader reader = new BufferedReader(new InputStreamReader(in))
		) {

			    String line = null;
			    int lineCount = 0;
			    while ((line = reader.readLine()) != null) {

			        if (++lineCount > maxLines)
			            break;

			    	String data[]=line.split(";");
			    	BankMarketing dataObject=new BankMarketing();
			    	dataObject.setData(data);
			    	dataSet.add(dataObject);
			    }
		} catch (IOException x) {
		  x.printStackTrace();
		} catch (Exception e) {
		  e.printStackTrace();
		}
		return dataSet;
	}
}
