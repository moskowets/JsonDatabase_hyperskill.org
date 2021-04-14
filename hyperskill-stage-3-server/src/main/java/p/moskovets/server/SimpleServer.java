package p.moskovets.server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@FunctionalInterface
interface Procedure {
    void execute();
}

class SimpleServer {

    private JSONDatabase database;
    private Gson gson;

    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    Procedure closeSocketServer;

    private void forceShutDown() {
        synchronized (this) {
            executor.shutdownNow();
            closeSocketServer.execute();
        }
    }

    public SimpleServer() {
        gson = new Gson();
        database = new JSONDatabase(gson);
    }

    void start() {
        String address = "127.0.0.1";
        int port = 23456;
        try {
            try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
                System.out.println("Server started!");
                closeSocketServer = () -> {
                    try { server.close(); } catch (Exception e) {}
                };
                do {
                    Socket socket = server.accept();
                    executor.submit(() -> {
                        try (DataInputStream input = new DataInputStream(socket.getInputStream());
                             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                            output.writeUTF(gson.toJson(database.process(input.readUTF(), () -> forceShutDown())));

                        } catch (Exception e) {
                        }
                        try {
                            socket.close();
                        } catch (Exception e) {
                        }
                    });
                } while (!(executor.isShutdown() || executor.isTerminated()));
            }
        } catch (Exception e) {
            return;
        }
    }
}