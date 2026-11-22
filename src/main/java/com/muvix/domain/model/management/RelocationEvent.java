package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.BoxAccessDeniedException;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.enums.BoxPermission;
import com.muvix.domain.model.management.enums.BoxRole;

import java.util.*;

public abstract sealed class RelocationEvent permits SaleEvent, PurchaseEvent, FullRelocationEvent {

    public enum Status { DRAFT, ACTIVE, COMPLETED, CANCELLED }

    protected final Long id;
    protected final Long userId; // Principal owner
    protected final Map<Long, BoxRole> participants = new HashMap<>();// Co-owners / shared users
    protected String title;
    protected final String uniqueShareToken;
    protected Status status;

    protected RelocationEvent(Long id, Long userId, String title, String uniqueShareToken, Status status, Map<Long, BoxRole> participants) {
        if (userId == null) throw new DomainException("User ID is required");
        if (title == null || title.isBlank()) throw new DomainException("Event title is required");

        this.id = id;
        this.userId = userId;
        this.title = title;
        this.uniqueShareToken = (uniqueShareToken != null && !uniqueShareToken.isBlank())
                ? uniqueShareToken
                : UUID.randomUUID().toString().substring(0, 8);
        this.status = status == null ? Status.DRAFT : status;

        if (participants != null) {
            this.participants.putAll(participants);
            this.participants.remove(userId);
        }
    }

    public boolean isMember(Long userId) {
        if (userId == null) return false;
        return this.userId.equals(userId) || this.participants.containsKey(userId);
    }

// --- Motor de evaluación de permisos ---

    public BoxRole getRoleOf(Long checkUserId) {
        if (checkUserId == null) return null;
        if (userId.equals(checkUserId)) return BoxRole.OWNER;
        return participants.get(checkUserId);
    }

    public boolean hasPermission(Long checkUserId, BoxPermission permission) {
        BoxRole role = getRoleOf(checkUserId);
        return role != null && role.hasPermission(permission);
    }

    protected void requirePermission(Long actorId, BoxPermission permission) {
        if (!hasPermission(actorId, permission)) {
            throw new BoxAccessDeniedException(actorId, permission);
        }
    }

    // --- Métodos de Negocio con Permisos  ---

    public void inviteMember(Long actorId, Long targetUserId) {
        requirePermission(actorId, BoxPermission.INVITE_MEMBERS);
        if (targetUserId == null || targetUserId.equals(userId)) {
            throw new DomainException("Invalid user for box sharing");
        }
        participants.putIfAbsent(targetUserId, BoxRole.MEMBER);
    }

    public void promoteToAdmin(Long actorId, Long targetUserId) {
        requirePermission(actorId, BoxPermission.PROMOTE_TO_ADMIN);
        if (targetUserId == null || targetUserId.equals(userId)) {
            throw new DomainException("Cannot modify owner role");
        }
        if (!participants.containsKey(targetUserId)) {
            throw new DomainException("User must be a member of the box first");
        }
        participants.put(targetUserId, BoxRole.ADMIN);
    }

    public void revokeAdmin(Long actorId, Long targetUserId) {
        requirePermission(actorId, BoxPermission.REVOKE_ADMIN); // Solo OWNER pasa esta línea
        if (participants.get(targetUserId) != BoxRole.ADMIN) {
            throw new DomainException("Target user is not an admin");
        }
        participants.put(targetUserId, BoxRole.MEMBER);
    }

    public void cancel(Long actorId) {
        requirePermission(actorId, BoxPermission.CANCEL_BOX); // Solo OWNER pasa esta línea
        if (status == Status.COMPLETED || status == Status.CANCELLED) {
            throw new DomainException("Event cannot be cancelled in its current state");
        }
        this.status = Status.CANCELLED;
    }

    public boolean isActive() { return status == Status.ACTIVE; }

    // --- Getters ---
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getUniqueShareToken() { return uniqueShareToken; }
    public Status getStatus() { return status; }
    public Map<Long, BoxRole> getParticipants() { return Map.copyOf(participants); }

}
