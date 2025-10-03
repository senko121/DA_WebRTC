package server.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.entity.User;
import server.entity.Room;
import server.entity.RoomMember;

import java.util.Optional;
import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    List<RoomMember> findByRoom_Code(String code);

    List<RoomMember> findByUser(User user);

    void deleteByUserAndRoom(User user, Room room);
}
