package com.epam.interfaces.web.client.response;

import java.util.List;

public record YearSummaryDTO(int year, List<MonthSummaryDTO> months) {

}
