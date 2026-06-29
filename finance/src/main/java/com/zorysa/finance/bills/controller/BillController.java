package com.zorysa.finance.bills.controller;

import com.zorysa.finance.auth.security.CurrentUser;
import com.zorysa.finance.bills.dto.BillResponse;
import com.zorysa.finance.bills.dto.CreateBillRequest;
import com.zorysa.finance.bills.dto.PayBillRequest;
import com.zorysa.finance.bills.dto.UpdateBillRequest;
import com.zorysa.finance.bills.entity.BillStatus;
import com.zorysa.finance.bills.service.BillService;
import com.zorysa.finance.shared.dto.PageResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final CurrentUser currentUser;

    public BillController(BillService billService, CurrentUser currentUser) {
        this.billService = billService;
        this.currentUser = currentUser;
    }

    @GetMapping
    PageResponse<BillResponse> listBills(
            @RequestParam(required = false) BillStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDueDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) Boolean overdue,
            Pageable pageable
    ) {
        return PageResponse.from(billService.listBills(
                currentUser.id(),
                status,
                startDueDate,
                endDueDate,
                categoryId,
                accountId,
                overdue,
                pageable
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BillResponse createBill(@Valid @RequestBody CreateBillRequest request) {
        return billService.createBill(currentUser.id(), request);
    }

    @GetMapping("/{id}")
    BillResponse getBill(@PathVariable UUID id) {
        return billService.getBill(currentUser.id(), id);
    }

    @PutMapping("/{id}")
    BillResponse updateBill(@PathVariable UUID id, @Valid @RequestBody UpdateBillRequest request) {
        return billService.updateBill(currentUser.id(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteBill(@PathVariable UUID id) {
        billService.deleteBill(currentUser.id(), id);
    }

    @PatchMapping("/{id}/pay")
    BillResponse payBill(@PathVariable UUID id, @Valid @RequestBody PayBillRequest request) {
        return billService.payBill(currentUser.id(), id, request);
    }
}
