package com.nexstar.portal.employee.entity;

import com.nexstar.portal.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum DocumentType {
        OFFER_LETTER, RESUME, ID_PROOF, ADDRESS_PROOF,
        EDUCATION_CERTIFICATE, EXPERIENCE_LETTER, CONTRACT,
        NDA, POLICY_ACKNOWLEDGMENT, OTHER
    }
}
