package server.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.entity.Room;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCode(String code);
}
