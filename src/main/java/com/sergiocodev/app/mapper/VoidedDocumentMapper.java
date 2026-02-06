package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.voideddocument.VoidedDocumentItemResponse;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentRequest;
import com.sergiocodev.app.dto.voideddocument.VoidedDocumentResponse;
import com.sergiocodev.app.model.VoidedDocument;
import com.sergiocodev.app.model.VoidedDocumentItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VoidedDocumentMapper {

    @Mapping(target = "establishmentName", source = "establishment.name")
    @Mapping(target = "userName", source = "user.username")
    VoidedDocumentResponse toResponse(VoidedDocument entity);

    @Mapping(target = "saleId", source = "sale.id")
    @Mapping(target = "saleDocument", expression = "java(item.getSale() != null ? item.getSale().getSeries() + \"-\" + item.getSale().getNumber() : null)")
    VoidedDocumentItemResponse toItemResponse(VoidedDocumentItem item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ticketSunat", ignore = true)
    @Mapping(target = "xmlUrl", ignore = true)
    @Mapping(target = "cdrUrl", ignore = true)
    @Mapping(target = "sunatStatus", ignore = true)
    @Mapping(target = "sunatDescription", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    VoidedDocument toEntity(VoidedDocumentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "establishment", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "ticketSunat", ignore = true)
    @Mapping(target = "xmlUrl", ignore = true)
    @Mapping(target = "cdrUrl", ignore = true)
    @Mapping(target = "sunatStatus", ignore = true)
    @Mapping(target = "sunatDescription", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntity(VoidedDocumentRequest request, @MappingTarget VoidedDocument entity);
}
