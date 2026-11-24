package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="chat_message_templates")
public class ChatMessageTemplateJpaEntity {
    public enum Type { DATETIME, PRICE }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=20)
    private Type type;

    @Column(nullable=false,length=160)
    private String title;

    @Column(nullable=false,length=2000)
    private String message;

    @Column(name="created_by",nullable=false)
    private Long createdBy;

    @Column(name="created_at",nullable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private boolean active=true;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Type getType(){return type;} public void setType(Type v){type=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public Long getCreatedBy(){return createdBy;} public void setCreatedBy(Long v){createdBy=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}