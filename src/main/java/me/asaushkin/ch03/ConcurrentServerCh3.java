package me.asaushkin.ch03;

import me.asaushkin.ch02.clsrv.book.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class that implements the concurrent server
 * 
 * @author author
 *
 */
public class ConcurrentServerCh3 {

	/**
	 * Executor to execute the commands of the server
	 */
	private static ThreadPoolExecutor executor;

	/**
	 * Cache to get a better performance
	 */
	private static ParallelCache cache;
	
	/**
	 * Socket to read the requests of the clients
	 */
	private static ServerSocket serverSocket;

	/**
	 * Attribute to control the status of the server
	 */
	private static volatile boolean stopped = false;

	/**
	 * Main method that implements the core functionality of the server
	 * 
	 * @param args
	 *            Arguments
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

		//WDIDAO dao = WDIDAO.getDAO();
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		cache = new ParallelCache();
		Logger.initializeLog();

		System.out.println("Initialization completed.");

		try (ServerSocket serverSocket = new ServerSocket(Constants.CONCURRENT_PORT)) {

			do {
				try {
					Socket clientSocket = serverSocket.accept();
					RequestTask task = new RequestTask(clientSocket, cache);
					executor.execute(task);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (!stopped);

			executor.awaitTermination(1, TimeUnit.DAYS);
			System.out.println("Shutting down cache");
			cache.shutdown();
			System.out.println("Cache ok");

			System.out.println("Main server thread ended");
		}
	}

	/**
	 * Method that returns the executor of the server
	 * 
	 * @return The executor of the server
	 */
	public static ThreadPoolExecutor getExecutor() {
		return executor;
	}

	/**
	 * Method that returns the cache
	 * 
	 * @return The cache
	 */
//	public static ParallelCache getCache() {
//		return cache;
//	}

	/**
	 * Methods that finish the execution of the server
	 */
	public static void shutdown() {
		stopped = true;
		System.out.println("Shutting down the server...");
		System.out.println("Shutting down executor");
		executor.shutdown();
		System.out.println("Executor ok");
		/**
		System.out.println("Closing socket");
		try {
			serverSocket.close();
			System.out.println("Socket ok");
		} catch (IOException e) {
			e.printStackTrace();
		}**/
		System.out.println("Shutting down logger");
		Logger.sendMessage("Shuttingdown the logger");
		Logger.shutdown();
		System.out.println("Logger ok");
	}

}
