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
    @Column(name = "CS_ID")
    private Long csId;

    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "QUESTION", columnDefinition = "TEXT")
    private String question;

    @Lob
    @Column(name = "ANSWER")
    private String answer;

    @Column(name = "STATUS")
    private String status;
    
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    
    @Column(name = "ANSWERED_AT")
    private LocalDateTime answeredAt;

}
