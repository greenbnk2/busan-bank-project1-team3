package kr.co.bnk.bnk_project.dto.admin;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class InfoAttachmentDTO {

    private int attachmentId;
    private int postId;
    private String fileName;
    private String filePath;
    private int sortOrder;

}
