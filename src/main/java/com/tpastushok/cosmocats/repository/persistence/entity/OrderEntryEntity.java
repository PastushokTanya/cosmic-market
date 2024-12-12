package com.tpastushok.cosmocats.repository.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_entry_id_seq")
    @SequenceGenerator(name = "order_entry_id_seq", sequenceName = "order_entry_id_seq")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "order_id", nullable = false)
    OrderEntity order;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Double price;
}
