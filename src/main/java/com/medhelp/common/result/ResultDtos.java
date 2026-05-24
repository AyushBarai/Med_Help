package com.medhelp.common.result;

import com.medhelp.common.result.TestResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class ResultDtos {

    // One row in the results table (one parameter like "Hemoglobin = 13.5 g/dL")
    public record ParameterRequest(
            @NotNull UUID orderItemId,
            @NotNull String parameterName,
            String value,
            String unit,
            String referenceRange
    ) {}

    // Bulk save all parameters for an order at once
    public record BulkResultRequest(
            @NotEmpty @Valid List<ParameterRequest> results
    ) {}

    public record ResultResponse(
            UUID id,
            UUID orderItemId,
            String parameterName,
            String value,
            String unit,
            String referenceRange,
            String flag
    ) {
        public static ResultResponse from(TestResult r) {
            return new ResultResponse(
                    r.getId(), r.getOrderItemId(), r.getParameterName(),
                    r.getValue(), r.getUnit(), r.getReferenceRange(),
                    r.getFlag().name()
            );
        }
    }
}