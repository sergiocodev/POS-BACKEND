package com.sergiocodev.app.mapper;

import com.sergiocodev.app.dto.accountpayable.AccountPayableResponse;
import com.sergiocodev.app.model.AccountPayable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountPayableMapper {

    @Mapping(target = "purchaseId", source = "purchase.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "purchaseIdentifier", expression = "java(entity.getPurchase().getSeries() + \"-\" + entity.getPurchase().getNumber())")
    @Mapping(target = "daysUntilDue", expression = "java((entity.getStatus() != com.sergiocodev.app.model.AccountPayable.PayableStatus.PAID && entity.getStatus() != com.sergiocodev.app.model.AccountPayable.PayableStatus.CANCELED) ? java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), entity.getDueDate()) : null)")
    AccountPayableResponse toResponse(AccountPayable entity);
}
