package org.blogapp.dg_blogapp.mapper;

import org.blogapp.dg_blogapp.config.GlobalMapperConfig;
import org.blogapp.dg_blogapp.dto.PayoutMethodRequestDto;
import org.blogapp.dg_blogapp.payment.model.PayoutMethod;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = GlobalMapperConfig.class)
public interface PayoutMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    PayoutMethod toEntity(PayoutMethodRequestDto payoutMethodRequestDto);

    PayoutMethodRequestDto toDto(PayoutMethod payoutMethod);

}
