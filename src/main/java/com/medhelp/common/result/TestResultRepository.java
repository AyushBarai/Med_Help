package com.medhelp.common.result;

import com.medhelp.common.result.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TestResultRepository extends JpaRepository<TestResult, UUID> {
    List<TestResult> findAllByOrderItemId(UUID orderItemId);
    void deleteAllByOrderItemId(UUID orderItemId);
}