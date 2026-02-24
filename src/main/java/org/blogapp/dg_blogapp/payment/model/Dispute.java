package org.blogapp.dg_blogapp.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.blogapp.dg_blogapp.model.BaseEntity;
import org.blogapp.dg_blogapp.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Dispute extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "donate_id")
    private Donation donation;

    @ManyToOne
    @JoinColumn(name="raised_by_id")
    private User raisedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    private LocalDateTime reslovedAt;

    @Positive
    @Digits(integer = 10, fraction = 2)
    private BigDecimal refundAmount;
}
