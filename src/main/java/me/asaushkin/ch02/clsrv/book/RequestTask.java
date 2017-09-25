package me.asaushkin.ch02.clsrv.book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Task that executes a request to the concurrent server
 * @author author
 *
 */
public class RequestTask implements Runnable {

	/**
	 * Socket to communicate with the client
	 */
	private Socket clientSocket;

	/**
	 * Constructor of the class
	 * @param clientSocket socket to communicate
	 */
	public RequestTask(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	/**
	 * Method that executes the request
	 */
	public void run() {

		try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
				true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));) {

			String line = in.readLine();
			
			Logger.sendMessage(line);
			ParallelCache cache = ConcurrentServer.getCache();
			String ret = cache.get(line);
			
			if (ret == null) {
				Command command;
				

				String[] commandData = line.split(";");
				System.err.println("Command: " + commandData[0]);
				if (commandData[0].equals("q")) {
					System.err.println("Query");
					command = new Command.ConcurrentQueryCommand(commandData);
				} else if (commandData[0].equals("r")) {
					System.err.println("Report");
					command = new Command.ConcurrentReportCommand(commandData);
				} else if (commandData[0].equals("s")) {
					System.err.println("Status");
					command = new Command.ConcurrentStatusCommand(commandData);
				} else if (commandData[0].equals("z")) {
					System.err.println("Stop");
					command = new Command.ConcurrentStopCommand(commandData);
				
				} else {
					System.err.println("Error");
					command = new Command.ConcurrentErrorCommand(commandData);
				}
				ret = command.execute();
				if (command.isCacheable()) {
					cache.put(line, ret);
				}
			} else {
				Logger.sendMessage("Command "+line+" was found in the cache");
			}

			System.err.println(ret);
			out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
