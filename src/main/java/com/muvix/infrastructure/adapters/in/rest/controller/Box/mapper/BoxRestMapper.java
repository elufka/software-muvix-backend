package com.muvix.infrastructure.adapters.in.rest.controller.Box.mapper;

import com.muvix.domain.model.management.Box;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.ReplaceBoxRequest;
import com.muvix.application.port.in.event.ManageRelocationEventUseCase.ReplaceCommand;
import com.muvix.application.port.in.event.ManageBoxMembersUseCase.InviteMemberCommand;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.InviteMemberRequest;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.BoxResponse;

import org.springframework.stereotype.Component;

@Component
public class BoxRestMapper {
    public ReplaceCommand toCommand(Long actorId, Long boxId, ReplaceBoxRequest r) {
        return new ReplaceCommand(
                boxId,
                actorId,
                r.title(),
                r.originLocation(),
                r.destinationLocation(),
                r.saleDeadline(),
                r.arrivalDeliveryDate(),
                r.takeItAllPrice(),
                r.discountRules()
        );
    }

    public InviteMemberCommand toCommand(Long actorId, Long boxId, InviteMemberRequest r) {
        return new InviteMemberCommand(
                actorId,
                boxId,
                r.targetUserId()
        );
    }

    public BoxResponse toResponse(Box box) {
        return new BoxResponse(
                box.getId(),
                box.getUserId(),
                box.getTitle(),
                box.getUniqueShareToken(),
                box.getStatus()
        );
    }
}
