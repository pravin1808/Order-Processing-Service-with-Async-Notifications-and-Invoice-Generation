package com.pravinmali.orderprocessing.repository;

import com.pravinmali.orderprocessing.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    List<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByRecipientEmailAndReadFalse(String email);

    long countByReadFalse();

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientEmail = :email AND n.read = false")
    int markAllAsReadForRecipient(@Param("email") String email);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.read = false")
    int markAllAsRead();
}
