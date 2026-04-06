package com.solocuerdas.solocuerdas_backend.repository;

import com.solocuerdas.solocuerdas_backend.model.Inquiry;
import com.solocuerdas.solocuerdas_backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByInquiryOrderBySentAtAsc(Inquiry inquiry);

    long countByInquiryIdAndReadAtIsNullAndSenderIdNot(Long inquiryId, Long senderId);
}
