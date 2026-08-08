package com.event.otg_backend.services;

import com.event.otg_backend.dtos.admin.TicketTypeConfigDto;
import com.event.otg_backend.dtos.admin.TicketTypeUpdateDto;

import java.util.List;

public interface AdminTicketTypeService {

    List<TicketTypeConfigDto> listTicketTypes();

    TicketTypeConfigDto updateTicketType(String code, TicketTypeUpdateDto dto);
}
