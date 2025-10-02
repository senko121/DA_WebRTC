package server.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import server.entity.RoomMember;

import java.util.List;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    List<RoomMember> findByRoom_Code(String code);
}
