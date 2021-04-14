package p.moskovets.client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;
import p.moskovets.exchange.Command;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

public static String readFileAsString(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src\\client\\data\\" + fileName)));
    }

    public static void main(String[] args) {
        try {
            String address = "127.0.0.1";
            int port = 23456;
            try (Socket socket = new Socket(InetAddress.getByName(address), port);
                 DataInputStream input = new DataInputStream(socket.getInputStream());
                 DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {

                System.out.println("Client started!");

                Command command = new Command();
                JCommander.newBuilder()
                        .addObject(command)
                        .build()
                        .parse(args);

                String message;
                if (command.getFileName().isEmpty()) {
                    Gson gson = new Gson();
                    message = gson.toJson(command.getMap());
                } else {
                    message = readFileAsString(command.getFileName());
                }

                output.writeUTF(message);

                System.out.println("Sent: " + message);
                System.out.println("Received: " + input.readUTF());

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
