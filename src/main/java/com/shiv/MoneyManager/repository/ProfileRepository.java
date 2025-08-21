package com.shiv.MoneyManager.repository;

import com.shiv.MoneyManager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity,Long> {

    // The jpa is going to execute an sql query i.e "Select * from tbl_profiles where email = ?;"
    Optional<ProfileEntity> findByEmail(String email);

    // The jpa is going to execute an sql query i.e "Select * from tbl_profiles where activation_token = ?;"
    Optional<ProfileEntity> findByActivationToken(String activationToken);
}
