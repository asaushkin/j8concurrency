package me.asaushkin.ch02.clsrv.book;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Class that implements the operations the server will do with the data of the
 * World Development Indicators
 * @author author
 *
 */
public class WDIDAO {

	private static WDIDAO dao;
	private List<WDI> data;
	
	/**
	 * Constructor of the class
	 * @param route Path to the file where the data is stored
	 */
	private WDIDAO (String route) throws URISyntaxException {
		WDILoader loader=new WDILoader();
		data=loader.load(route);
	}

	/**
	 * Method that returns an instance to get access to the data
	 * @return
	 */
	public static WDIDAO getDAO() throws URISyntaxException {
		if (dao==null) {
			dao=new WDIDAO(Constants.DATA_ROUTE);
		}
		return dao;
	}
	
	/**
	 * Method that implements a query to the data
	 * @param codCountry Cod of the country
	 * @param codIndicator Cod of the indicator
	 * @return The values of the indicator in the country for all the years
	 */
	public String query (String codCountry, String codIndicator) {

		WDI wdi=null;
		for (int i=0; i<data.size(); i++) {
			wdi=data.get(i);
			if ((wdi.getCountryCode().equals(codCountry))&&(wdi.getIndicatorCode().equals(codIndicator))) {
				break;
			}
		}
		
		StringWriter writer=new StringWriter();
		writer.write(codCountry);
		writer.write(";");
		writer.write(codIndicator);
		writer.write(";");
		Double[] years=wdi.getValues();
		for (int i=0; i< years.length; i++) {
			writer.write(years[i].toString());
			if (i<years.length-1) {
				writer.write(";");
			}
		}
		return writer.toString();
	}

	/**
	 * Method that implements a query to the data
	 * @param codCountry Cod of the country
	 * @param codIndicator Cod of the indicator
	 * @param year Year of the request
	 * @return The value of the indicator for that country in that year
	 * @throws Exception Exception if something goes wrong
	 */
	public String query (String codCountry, String codIndicator, short year) throws Exception {

        System.err.println("Query: " + codCountry + ", " + codIndicator);

//        return data.stream().
//                filter(e -> e.getCountryCode().equals(codCountry)).
//                filter(e -> e.getIndicatorCode().equals(codIndicator)).
//                findFirst().
//                map(s -> String.format("%s;%s;%s;%s", codCountry, codIndicator, year, s.getValue(year))).
//                orElse("No WDI found!");

		WDI wdi=null;
		for (int i=0; i<data.size(); i++) {
			wdi=data.get(i);
			if ((wdi.getCountryCode().equals(codCountry))&&(wdi.getIndicatorCode().equals(codIndicator))) {
				break;
			}
		}


        StringWriter writer=new StringWriter();
		writer.write(codCountry);
		writer.write(";");
		writer.write(codIndicator);
		writer.write(";");
		writer.write(""+year);
		writer.write(";");
		writer.write(wdi.getValue(year).toString());
		return writer.toString();
	}
	
	/**
	 * Method that makes a report of data
	 * @param codIndicator Indicator
	 * @return The medium value of that indicator per country
	 */
	public String report (String codIndicator) {
		
		StringWriter writer=new StringWriter();
		writer.write(codIndicator);
		writer.write(";");
		for (int i=0; i<data.size(); i++) {
			WDI wdi=data.get(i);
			if (wdi.getIndicatorCode().equals(codIndicator)) {
				Double[] years=wdi.getValues();
				double mean=0.0;
				for (int j=0; j<years.length; j++) {
					mean+=years[j];
				}
				mean/=years.length;
				writer.write(wdi.getCountryCode());
				writer.write(";");
				writer.write(""+mean);
				writer.write(";");
			}
		}
		
		return writer.toString();

	}

	public List<WDI> getData() {
		return data;
	}

	
}
