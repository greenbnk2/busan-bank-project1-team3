package kr.co.bnk.bnk_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "CS")
@Getter @Setter
@NoArgsConstructor
public class Cs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long csId;

    private Long categoryId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Lob
    private String answer;

    private String status;
    private String userId;

    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

}
