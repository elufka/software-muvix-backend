package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.ManageBoxMembersUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.RelocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageBoxMembersService implements ManageBoxMembersUseCase {
    private final RelocationEventRepositoryPort eventRepositoryPort;

    @Override
    public void inviteMember(InviteMemberCommand command) {
        RelocationEvent event = eventRepositoryPort.findById(command.boxId())
                .orElseThrow(() -> new DomainException("Box not found with id: " + command.boxId()));

        event.inviteMember(command.actorId(), command.targetUserId());

        eventRepositoryPort.save(event);
    }
}
