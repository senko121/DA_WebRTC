/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
/**
 *
 * @author thach
 */
public class TurnClient {
     public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName("localhost"); // đổi thành IP server thật nếu khác máy
            int serverPort = 10000; // port TURN server

            Scanner scanner = new Scanner(System.in);

            System.out.print("Nhap roomId: ");
            String roomId = scanner.nextLine();

            // Gửi đăng ký vào phòng
            String registerMsg = "REGISTER:" + roomId;
            byte[] registerData = registerMsg.getBytes();
            DatagramPacket registerPacket = new DatagramPacket(registerData, registerData.length, serverAddr, serverPort);
            socket.send(registerPacket);
            System.out.println("da gui: " + registerMsg);

            // Thread nhận dữ liệu từ server
            new Thread(() -> {
                try {
                    byte[] buffer = new byte[2048];
                    while (true) {
                        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                        socket.receive(response);
                        String msg = new String(response.getData(), 0, response.getLength());
                        System.out.println("\n[Nhan] " + msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Vòng lặp chat
            while (true) {
                String text = scanner.nextLine();
                String dataMsg = "DATA:" + roomId + ":" + text;
                byte[] sendData = dataMsg.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, serverPort);
                socket.send(sendPacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
