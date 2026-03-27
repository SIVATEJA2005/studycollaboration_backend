package com.sivateja.studycollabration.entities;

import com.sivateja.studycollabration.entities.Room;
import com.sivateja.studycollabration.entities.Users;
import com.sivateja.studycollabration.model.TopicStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // "Chapter 3 - Binary Trees"

    private String description; // optional details

    @Enumerated(EnumType.STRING)
    private TopicStatus status; // NOT_STARTED, IN_PROGRESS, DONE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // who claimed this topic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by_id")
    private Users claimedBy;    // null = unclaimed

    // who added this topic to the room
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Users createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}