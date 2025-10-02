/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;
import utils.Config;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 *
 * @author thach
 */
public class TurnRelayServer {
      // Map lưu danh sách viewer theo roomId
    private static final Map<String, List<InetSocketAddress>> rooms = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            int port = Config.getInt("server.turn.port");
            DatagramSocket socket = new DatagramSocket(port);

            System.out.println("TURN Relay Server chay tren cong " + port);

            byte[] buffer = new byte[2048];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String msg = new String(packet.getData(), 0, packet.getLength());
                InetAddress clientAddr = packet.getAddress();
                int clientPort = packet.getPort();

                System.out.println("Nhan tu " + clientAddr.getHostAddress() + ":" + clientPort + " → " + msg);

                // Giả sử message có dạng: "REGISTER:room123" hoặc "DATA:room123:hello"
                if (msg.startsWith("REGISTER")) {
                    String roomId = msg.split(":")[1];
                    rooms.putIfAbsent(roomId, new ArrayList<>());
                    rooms.get(roomId).add(new InetSocketAddress(clientAddr, clientPort));
                    System.out.println("Client dang ky vao phong " + roomId);
                } else if (msg.startsWith("DATA")) {
                    String[] parts = msg.split(":", 3);
                    String roomId = parts[1];
                    String data = parts[2];

                    // Forward tới tất cả viewer trong phòng
                    if (rooms.containsKey(roomId)) {
                        for (InetSocketAddress addr : rooms.get(roomId)) {
                            if (!(addr.getAddress().equals(clientAddr) && addr.getPort() == clientPort)) {
                                byte[] forwardData = data.getBytes();
                                DatagramPacket forwardPacket =
                                        new DatagramPacket(forwardData, forwardData.length, addr.getAddress(), addr.getPort());
                                socket.send(forwardPacket);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
