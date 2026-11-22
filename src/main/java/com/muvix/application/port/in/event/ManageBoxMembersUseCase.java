package com.muvix.application.port.in.event;

public interface ManageBoxMembersUseCase {
    record InviteMemberCommand(
            Long actorId,
            Long boxId,
            Long targetUserId
    ){}

    void inviteMember(InviteMemberCommand command);
}
