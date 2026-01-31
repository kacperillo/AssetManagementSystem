package com.assetmanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Asset {

  @Id
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asset_seq")
  // @SequenceGenerator(name = "asset_seq", sequenceName = "asset_sequence", initialValue = 1000, allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.IDENTITY) 
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssetType assetType;

  @Column(nullable = false)
  private String vendor;

  @Column(nullable = false)
  private String model;

  @Column(nullable = false, unique = true)
  private String seriesNumber;

  @Column(nullable = false)
  private boolean isActive = true;

  @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Assignment> assignments = new ArrayList<>();

}
