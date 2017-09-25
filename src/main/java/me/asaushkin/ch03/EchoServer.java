package me.asaushkin.ch03;

import me.asaushkin.ProxyExecutorService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;

public class EchoServer {

    static class Worker implements Runnable {
        Socket socket;
        ApplicationContext ctx;

        Worker(Socket socket, ApplicationContext ctx) {
            this.socket = socket;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String line;
                READLINE_LOOP: while ((line = in.readLine()) != null) {

                    switch (line) {
                        case "quit":
                            break READLINE_LOOP;
                        case "shutdown":
                            System.out.println("Shutting down the service...");

                            ExecutorService srv = (ExecutorService) ctx.get("mainExecutorService");
                            srv.shutdown();

                            ((ServerSocket)ctx.get("serverSocket")).close();
                            break READLINE_LOOP;
                        default:
                            out.println(line.toUpperCase());
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static class ApplicationContext extends ConcurrentHashMap<String, Object> {
    }

    public static void main(String[] args) throws IOException, InterruptedException {

        ApplicationContext ctx = new ApplicationContext();

        ExecutorService service =
                new ProxyExecutorService(
                    Executors.newFixedThreadPool(1) // Runtime.getRuntime().availableProcessors()
        );

        ctx.put("mainExecutorService", service);

        try (ServerSocket s = new ServerSocket(4444)) {
            ctx.put("serverSocket", s);
            do {
                System.out.print("Awaiting in accept ...");
                Socket socket = s.accept();
                System.out.println(" accepted!");

                service.submit(new Worker(socket, ctx));
            } while (!service.isShutdown());
        } catch (SocketException e) {
            System.out.println("Server close accept");
        }

        service.awaitTermination(1, TimeUnit.DAYS);
    }
}
