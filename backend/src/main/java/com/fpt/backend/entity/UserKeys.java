package com.fpt.backend.entity;

import com.fpt.backend.enums.KeyAlgorithm;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "user_keys")
public class UserKeys extends BaseEntity{
    @Column(name = "keys_size")
    private long keySize;
    @Column(name = "create_at")
    private LocalDateTime createAt;
<<<<<<< HEAD
    @Column(name = "public_key", columnDefinition = "nvarchar(max)")
    private String publicKey;
    @Column(name = "key_fingerprint", length = 64)
    private String keyFingerprint;
=======
    @Lob
    @Column(name = "public_key", columnDefinition = "nvarchar(max)")
    private String publicKey;
    @Lob
    @Column(name = "private_key", columnDefinition = "nvarchar(max)")
    private String privateKey;
>>>>>>> 7d6eb51fe9c660b46d1a1bc0200bcbbc73cf5f51
    @Column(name = "key_algorithm")
    @Enumerated(EnumType.STRING)
    private KeyAlgorithm keyAlgorithm;
    /// relation
    //User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;
    //signature
    @OneToMany(mappedBy = "userKey")
    private List<Signature> signatures;
    @OneToMany(mappedBy = "userKey")
    private List<ElectronicSignatures> electronicSignatures;
}
