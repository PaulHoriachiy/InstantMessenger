
package clientSide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


public class ProtocolClient {
    static final int PORT = 3000;

    Socket s1 = null;
    String line = null;
    BufferedReader br = null;
    BufferedReader is = null;
    PrintWriter os = null;
    IncomingServerMessage message;
    InetAddress address;

    Gson gson = new Gson();
    ViewClient viewClient = new ViewClient();

    /*method of converting objects to json format
    * */
    String transform(Object OutputClientMessage) {
        Gson out = new GsonBuilder().create();
        String json = out.toJson(OutputClientMessage);
        return json;
    }

    public void start() throws IOException {

            address = InetAddress.getByName("10.112.32.9");
        try {
            s1 = new Socket(address, PORT);
            br = new BufferedReader(new InputStreamReader(System.in));
            is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
            os = new PrintWriter(s1.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("IO Exception");
        }
        /*
        outputs the address to the console
         */
        viewClient.address(address);
    }

    public void inputOutput(String inputString) throws IOException {
        String response = null;
        boolean a = false;
        try {
            line = inputString;

            /* send and receive message cycle
            * */
            while (line.compareTo("QUIT") != 0) {  // in the end, there will be another value to exit the loop
                os.println(line);
                response = is.readLine();
                message = gson.fromJson(line, IncomingServerMessage.class);
                os.println(line);
                    /*
                    * response from the server output to the console in json format string*/
                viewClient.serverResp(response);
                if (message.getMessageAll() != null) {
                    a = true;
                }
                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Socket read Error");

        } finally {
            is.close();
            os.close();
            br.close();
            s1.close();

            /*outputs a message about the closing of the connection to the console
            * */
            viewClient.closed();

        }
    }
}
