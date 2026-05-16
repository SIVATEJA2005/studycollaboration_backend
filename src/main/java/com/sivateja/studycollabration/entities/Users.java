package com.sivateja.studycollabration.entities;
import com.sivateja.studycollabration.model.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true,nullable = false)
    private String userName;
    @Column(nullable = false)
    private String displayName;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable=false)
    private String password;
    @Column(nullable = false)
    private boolean isActive = false;
    @Column(unique = true)
    private String activationToken;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @OneToMany(mappedBy="user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RoomMembers> roomMemberShips;
    @OneToMany(mappedBy = "createdBy")
    private List<Room> rooms;
    @OneToMany(mappedBy = "createdBy")
    private List<Notes> notes;

    @OneToMany(mappedBy = "sender")
    private List<Messages> messages;

    @OneToMany(mappedBy = "createdBy")
    private List<ToDos> todos;


}
