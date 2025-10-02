package server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_members")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // OWNER / PARTICIPANT
}
