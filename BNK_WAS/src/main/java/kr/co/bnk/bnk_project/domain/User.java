package kr.co.bnk.bnk_project.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BNK_USER")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bnk_user_seq")
    @SequenceGenerator(
            name = "bnk_user_seq",
            sequenceName = "BNK_USER_SEQ",
            allocationSize = 1
    )
    @Column(name = "CUST_NO")
    private Long custNo;

    @Column(name = "CUST_ID", nullable = false, unique = true)
    private String custId;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "CUST_NAME")
    private String name;

    @Column(name = "CUST_HP")
    private String phone;

    @Column(name = "CUST_EMAIL")
    private String email;


    @Column(name = "ZIP_CODE")
    private String zipCode;

    @Column(name = "ADDR1")
    private String addr1;

    @Column(name = "ADDR2")
    private String addr2;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "JOIN_DATE", nullable = false)
    private LocalDateTime joinDate;

    public String getRole() {
        return "ROLE_USER";

    }

    @PrePersist
    public void prePersist() {
        if (this.joinDate == null) {
            this.joinDate = LocalDateTime.now();
        }
    }
}
