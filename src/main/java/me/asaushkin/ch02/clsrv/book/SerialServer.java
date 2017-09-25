package me.asaushkin.ch02.clsrv.book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

/**
 * Class that implements the serial server.
 * 
 * @author author
 *
 */
public class SerialServer {

	public static void main(String[] args) throws IOException, URISyntaxException {
		WDIDAO dao = WDIDAO.getDAO();
		boolean stopServer = false;
		System.out.println("Initialization completed.");

		try (ServerSocket serverSocket = new ServerSocket(Constants.SERIAL_PORT)) {

			do {
				try (Socket clientSocket = serverSocket.accept();
						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));) {
					String line = in.readLine();
					Command command;

					String[] commandData = line.split(";");
					System.err.println("Command: " + commandData[0]);
					switch (commandData[0]) {
					case "q": 
						System.err.println("Query");
						command = new Command.QueryCommand(commandData);
						break;
					case "r":
						System.err.println("Report");
						command = new Command.ReportCommand(commandData);
						break;
					case "z":
						System.err.println("Stop");
						command = new Command.StopCommand(commandData);
						stopServer = true;
						break;
					default:
						System.err.println("Error");
						command = new Command.ErrorCommand(commandData);
					}
					String response = command.execute();
					System.err.println(response);
					out.println(response);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!stopServer);
		}
	}
}
