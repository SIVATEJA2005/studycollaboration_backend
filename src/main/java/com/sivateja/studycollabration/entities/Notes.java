package com.sivateja.studycollabration.entities;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.naming.Context;
import java.time.LocalDateTime;


@Entity
@Table(name = "notes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Notes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by")
    private Users createdBy;

    @CreationTimestamp
    private LocalDateTime createAt;
}