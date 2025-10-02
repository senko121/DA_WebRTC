/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;
import utils.Config;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 *
 * @author thach
 */
public class StunServer {
       public static void main(String[] args) {
        try {
            int port = Config.getInt("server.stun.port");
            DatagramSocket socket = new DatagramSocket(port);

            System.out.println("STUN Server dang chay tren cong " + port);

            byte[] buffer = new byte[1024];

            while (true) {
                // Nhận gói từ client
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();

                System.out.println("Nhan ping tu client: " + clientAddress.getHostAddress() + ":" + clientPort);

                // Trả về JSON chứa IP/Port public
                String responseStr = "{ \"publicIP\": \"" + clientAddress.getHostAddress() +
                                     "\", \"publicPort\": " + clientPort + " }";

                byte[] responseData = responseStr.getBytes();
                DatagramPacket response = new DatagramPacket(
                        responseData,
                        responseData.length,
                        clientAddress,
                        clientPort
                );

                socket.send(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
