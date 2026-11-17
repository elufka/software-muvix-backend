package com.muvix.application.port.out.persistence;
import com.muvix.domain.model.negotiation.Offer;
import java.util.Optional;
public interface OfferRepositoryPort { Offer save(Offer offer); Optional<Offer> findById(Long id); }
