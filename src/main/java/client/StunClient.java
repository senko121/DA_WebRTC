/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
/**
 *
 * @author thach
 */
public class StunClient {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();

            // Địa chỉ server (chạy local thì để localhost, còn nếu server chạy ở máy khác thì dùng IP thật)
            InetAddress serverAddress = InetAddress.getByName("localhost");
            int serverPort = 3478;

            // Gửi message "PING"
            String msg = "PING";
            byte[] data = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(packet);

            System.out.println("da gui: " + msg);

            // Nhận phản hồi từ server
            byte[] buffer = new byte[1024];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            String responseStr = new String(response.getData(), 0, response.getLength());
            System.out.println("Server tra ve: " + responseStr);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
