package orders_parser;

import java.io.*;

import org.json.simple.*;
import org.json.simple.parser.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

@SpringBootApplication
public class OrdersParserApplication {

	public static int count = 1;
	public static void main(String[] args) {
		OrdersParserApplication ordersParserApplication = new OrdersParserApplication();
		if (1 < args.length) {
			String firstFileString = args[0];
			String secondFileString = args[1];

			Thread thread1 = new Thread(new Runnable() {
				@Override
				public void run() {
					ordersParserApplication.readAndPrintFile(firstFileString);
				}
			});
			Thread thread2 = new Thread(new Runnable() {
				@Override
				public void run() {
					ordersParserApplication.readAndPrintFile(secondFileString);
				}
			});
			thread1.start();

			try
			{
				thread1.join();
			} catch(Exception ex)
			{
				System.out.println("Exception has " +
						"been caught" + ex);
			}
			thread2.start();

			SpringApplication.run(OrdersParserApplication.class, args);
		}else{
			System.out.println("Invalid input");
		}
	}

	private void readAndPrintFile(String fileString) {
		try{
			String fileType = Files.probeContentType(Paths.get(fileString));
			if(fileType.equalsIgnoreCase("text/csv")) {
				printCSVData(fileString);
			} else if(fileType.equalsIgnoreCase("application/json")) {
				printJSONData(fileString);
			}else{
				System.out.println("Invalid File Type");
			}
		}catch(IOException ex){
			System.out.println("Exception : "+ex.getMessage());
		}
	}
	private void printJSONData(String fileString) {

		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(fileString))
		{
			JSONArray OrderList = (JSONArray)jsonParser.parse(reader);


			Iterator<JSONObject> iterator = OrderList.iterator();
			int index = 1;
			while (iterator.hasNext()) {
				parseOrderObject( iterator.next() , index++, fileString);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Exception : "+e.getMessage());
		} catch (IOException e) {
			System.out.println("Exception : "+e.getMessage());
		} catch (ParseException e) {
			System.out.println("Exception : "+e.getMessage());
		}
	}

	private void parseOrderObject(JSONObject order, int lineCount, String fileString)
	{
		if(validateJsonData(order, fileString, lineCount)){
			System.out.println("{ \"id\":"+ count++ +",\"orderId\":" + order.get("orderId") + ",\"amount\":" +order.get("amount") + ",\"comment\":\"" + order.get("comment")+"\",\"filename\":\""+ fileString + "\",\"line\":"+ lineCount +",\"result\":\"OK\"}");
		}
	}

	private boolean validateJsonData(JSONObject order, String fileString, int lineCount) {
		try{
			Long orderId = (Long) order.get("orderId");
		}catch (ClassCastException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"orderId is of invalid type\"}");
			return false;
		}
		try{
			Double amount = (Double) order.get("amount");
		}catch (ClassCastException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"amount is of invalid type\"}");
			return false;
		}
		try{
			if(!StringUtils.hasText((String) order.get("currency"))){
				System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"currency is Required\"}");
			}
		}catch (ClassCastException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"currency is Required\"}");
			return false;
		}
		try{
			if(!StringUtils.hasText((String) order.get("comment"))){
				System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"comment is Required\"}");
			}
		}catch (ClassCastException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"comment is Required\"}");
			return false;
		}
		return true;
	}

	private void printCSVData(String  fileString) throws IOException {
		String line = "";
		String splitBy = ",";
		BufferedReader br = new BufferedReader(new FileReader(fileString));
		int lineCount = 1;
		while ((line = br.readLine()) != null){
			String[] orders = line.split(splitBy);
			if(validateCSVLine(orders, lineCount, fileString)){
				System.out.println("{ \"id\":"+ count++ +",\"orderId\":" + orders[0] + ",\"amount\":" + orders[1] + ",\"comment\":\"" + orders[2]+"\",\"filename\":\""+ fileString + "\",\"line\":"+ lineCount++ +",\"result\":\"OK\"}");
			}
		}
	}

	private boolean validateCSVLine(String[] orders, int lineCount, String fileString) {
		if( orders == null ){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"Empty Order Data\"}");
			return false;
		}else if( orders.length != 4){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"Order data is missing\"}");
			return false;
		}
		try{
			Long.valueOf(orders[0]);
		}catch (NumberFormatException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"orderId is of invalid type\"}");
			return false;
		}
		try{
			Double.valueOf(orders[1]);
		}catch (NumberFormatException exception){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"amount is of invalid type\"}");
			return false;
		}
		if(!StringUtils.hasText(orders[2])){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"currency is Required\"}");
			return false;
		}
		if(!StringUtils.hasText(orders[3])){
			System.out.println("{ \"filename\":"+ fileString + ",\"line\":"+ lineCount +",\"result\":\"comment is Required\"}");
			return false;
		}

		return true;
	}


}
