package com.group42.client.network;


import com.group42.client.controllers.fx.RootManager;
import com.group42.client.controllers.fx.Controller;
import com.group42.client.network.protocol.IncomingServerMessage;
import com.group42.client.network.protocol.ProtocolClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.group42.client.controllers.StringCrypter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class NetworkController {

    /**
     * Instance of NetworkController.
     */
    private static final NetworkController INSTANCE = new NetworkController();

    /**
     * Instance of class for encrypting and decrypting strings.
     */
    private StringCrypter crypter = new StringCrypter(new byte[]{1,4,5,6,8,9,7,8});

    /**
     * Logging for exception trace.
     */
    private final Logger logger = LogManager.getLogger(NetworkController.class);

    /**
     * Instance of inner class to listen connection.
     */
    private ConnectionThread connectionThread = new ConnectionThread();

    /**
     * Instance of protocol class
     */
    private ProtocolClient protocol;


    /**
     * Returned instance of class.
     *
     */
    public static NetworkController getInstance() {
        return INSTANCE;
    }

    private NetworkController() {
    }

    /**
     * Connect to server.
     */
    public void openConnection() {
        protocol = new ProtocolClient();
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    /**
     * Send message to server
     *
     * @param request is object of output message
     */
    public void send(Object request) {
        if (connectionThread.outputStream == null){
            RootManager.getInstance().connectionFailed();
        } else connectionThread.outputStream.println(protocol.transform(request));
    }

    /**
     * Disconnect from server
     */
    public void closeConnection() {
        connectionThread.interrupt();
    }

    /**
     * Inner class create second thread to listen responses from server.
     */
    private class ConnectionThread extends Thread {
        private BufferedReader inputStream;
        private PrintWriter outputStream;
        private final int socketPort = 3000;

        @Override
        public void run() {
            try(Socket socket = new Socket(InetAddress.getByName("PC17760"), socketPort);
                BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);) {
                this.inputStream = inputStream;
                this.outputStream = outputStream;
                socket.setTcpNoDelay(true);
                logger.info("Connection success!");
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String response = crypter.decrypt(this.inputStream.readLine());
                        if (!response.equals("")) {
                            logger.info("SERVER RESPONSE: " + response);
                            IncomingServerMessage inData = protocol.transformOut(response);
                            Controller.onReceiveCallback.accept(inData);
                        }
                    } catch (IOException e) {
                        logger.error("Listener Thread interrupted ", e);
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException e) {
                logger.error("Connection failed!");
                RootManager.getInstance().connectionFailed();
            }
        }
    }
}
