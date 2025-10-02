package server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 6)
    private String code; // mã phòng 6 ký tự

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host; // ai tạo phòng

    @Column(nullable = false)
    private String accessMode = "OPEN"; // OPEN / REQUEST / CLOSED

    private boolean active = true;
}
