package com.luxon.assignment.repository;

import com.luxon.assignment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateRepository extends JpaRepository<Account,Integer> {}
