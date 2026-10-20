package com.muvix.infraestructure.entrypoints.rest;

import com.muvix.domain.model.Publication;
import com.muvix.domain.port.inbound.CreatePublicationUseCase;
import com.muvix.infraestructure.entrypoints.rest.dto.CreatePublicationRequest;

@RestController
@RequestMapping("/api/v1/publication")
public class PublicationController {
    private final CreatePublicationUseCase createPublicationUseCase;

    public PublicationController(CreatePublicationUseCase createPublicationUseCase) {
        this.createPublicationUseCase = createPublicationUseCase;
    }

    @PostMapping()
    public ResponseEntity<Publication> create(@RequestBody CreatePublicationRequest request){
        
    }

}
