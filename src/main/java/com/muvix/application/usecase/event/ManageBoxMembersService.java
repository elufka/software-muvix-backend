package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.ManageBoxMembersUseCase;
import com.muvix.application.port.out.persistence.BoxRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.Box;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManageBoxMembersService implements ManageBoxMembersUseCase {

    private final BoxRepositoryPort boxRepositoryPort;

    @Override
    public void inviteMember(InviteMemberCommand command) {
        Box box = boxRepositoryPort.findById(command.boxId())
                .orElseThrow(() -> new DomainException("Box not found with id: " + command.boxId()));

        box.inviteMember(command.actorId(), command.targetUserId());

        boxRepositoryPort.save(box);
    }
}
