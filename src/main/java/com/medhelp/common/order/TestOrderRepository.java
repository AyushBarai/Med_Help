package com.medhelp.common.order;

import com.medhelp.common.order.TestOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestOrderRepository extends JpaRepository<TestOrder, UUID> {
    Optional<TestOrder> findByIdAndLabId(UUID id, UUID labId);
    Page<TestOrder> findAllByLabId(UUID labId, Pageable pageable);
    Page<TestOrder> findAllByLabIdAndStatus(UUID labId, TestOrder.OrderStatus status, Pageable pageable);
    List<TestOrder> findAllByPatientIdAndLabId(UUID patientId, UUID labId);

    // Count orders to generate sequential order number per lab
    @Query("SELECT COUNT(o) FROM TestOrder o WHERE o.labId = :labId")
    long countByLabId(UUID labId);
}